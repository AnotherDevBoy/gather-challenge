package town.gather.challenge.api;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import town.gather.challenge.domain.commands.Command;
import town.gather.challenge.domain.commands.DisconnectCommand;
import town.gather.challenge.domain.commands.JoinCommand;
import town.gather.challenge.domain.commands.MoveCommand;
import town.gather.challenge.domain.commands.PositionCommand;
import town.gather.challenge.domain.game.Position;
import town.gather.challenge.domain.repository.GameCommandQueue;

@Slf4j
public class GameWebSocketApi extends WebSocketServer implements GameStateChangesObserver {
  private final GameCommandQueue gameCommandQueue;
  private final GameStateSyncer gameStateSyncer;

  public GameWebSocketApi(InetSocketAddress address, GameCommandQueue gameCommandQueue) {
    super(address);

    this.gameCommandQueue = gameCommandQueue;

    this.gameStateSyncer = new GameStateSyncer(this);

    var pool = Executors.newSingleThreadScheduledExecutor();
    pool.scheduleAtFixedRate(this.gameStateSyncer, 0, 100, TimeUnit.MILLISECONDS);
  }

  @Override
  public void onStart() {
    log.info("Server started successfully");
  }

  @Override
  public void onError(WebSocket connection, Exception ex) {
    log.error("An error occurred on connection: {}", connection.getRemoteSocketAddress(), ex);
  }

  @Override
  public void onOpen(WebSocket connection, ClientHandshake handshake) {
    log.info("New connection to {}", connection.getRemoteSocketAddress());
  }

  @Override
  public void onClose(WebSocket connection, int code, String reason, boolean remote) {
    log.info("Closed {} with exit code {} additional info {}", connection.getRemoteSocketAddress(), code, reason);

    var playerAttached = (UUID) connection.getAttachment();

    if (playerAttached == null) {
      log.warn("Cannot disconnect a player that never joined");
      return;
    }

    this.gameCommandQueue.notifyPlayerDisconnected(playerAttached);
  }

  @Override
  public void onMessage(WebSocket connection, String message) {
    log.info("Received message [{}] from {}", message, connection.getRemoteSocketAddress());
    var maybeCommand = Command.fromString(message);

    if (maybeCommand.isEmpty()) {
      log.error("Command could not be processed");
      return;
    }

    var command = maybeCommand.get();

    log.info("Received command {}", command);

    switch (command.getType()) {
      case JOIN:
        var joinCommand = (JoinCommand) command;
        var player = joinCommand.getPlayer();

        connection.setAttachment(player);

        // TODO: Get all player positions
        /*
        for (var position : allPlayerPositions) {
          var otherPlayerPositionCommand =
                  new PositionCommand(position.getPlayer(), position.getX(), position.getY());
          connection.send(otherPlayerPositionCommand.toString());
        }
         */

        this.gameCommandQueue.notifyPlayerJoined(player);
        break;
      case MOVE:
        var moveCommand = (MoveCommand) command;

        var playerAttached = (UUID) connection.getAttachment();

        this.gameCommandQueue.notifyPlayerMovement(playerAttached, moveCommand.getDirection());
        break;
    }

    log.info("Processed command: {}", command);
  }

  @Override
  public void onPlayerMoved(Position position) {
    PositionCommand positionCommand = new PositionCommand(position.getPlayer(), position.getX(), position.getY());
    broadcast(positionCommand.toString());
  }

  @Override
  public void onPlayerDisconnected(UUID player) {
    log.info("Broadcasting player disconnect");
    var dcCommand = new DisconnectCommand(player);
    broadcast(dcCommand.toString());
  }
}
