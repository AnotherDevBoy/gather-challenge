package town.gather.challenge;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import town.gather.challenge.domain.commands.Command;
import town.gather.challenge.domain.commands.DisconnectCommand;
import town.gather.challenge.domain.commands.JoinCommand;
import town.gather.challenge.domain.commands.MoveCommand;
import town.gather.challenge.domain.commands.PositionCommand;
import town.gather.challenge.domain.game.GameState;
import town.gather.challenge.domain.game.Position;

@Slf4j
public class GameServer extends WebSocketServer {
  private final GameState gameState;
  private final ReentrantLock lock;
  private List<WebSocketClient> otherGameServers;

  public GameServer(InetSocketAddress address, List<URI> otherGameServerURIs) {
    super(address);
    this.gameState = new GameState();
    this.lock = new ReentrantLock();

    this.otherGameServers = new LinkedList<>();

    for (URI uri : otherGameServerURIs) {
      var client = new GameServerInternalClient(uri);
      this.otherGameServers.add(client);
    }
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

    this.lock.lock();
    this.disconnectPlayerLocally(playerAttached);
    this.notifyPlayerDisconnect(playerAttached);
    this.lock.unlock();
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
        this.lock.lock();
        var joinCommand = (JoinCommand) command;
        var player = joinCommand.getPlayer();

        connection.setAttachment(player);

        var allPlayerPositions = this.gameState.getAllPlayerPositions();
        var maybePosition = this.gameState.moveToEmptyPosition(player);

        if (maybePosition.isEmpty()) {
          log.warn("Could not add player to game because the map is full");
          this.lock.unlock();
          return;
        }

        var playerPosition = maybePosition.get();

        var newPlayerPositionCommand =
            new PositionCommand(player, playerPosition.getX(), playerPosition.getY());

        log.info("Broadcast player join");
        broadcast(newPlayerPositionCommand.toString());

        this.notifyPlayerPosition(player, playerPosition);

        log.info("Notify new player of other player positions");
        for (var position : allPlayerPositions) {
          var otherPlayerPositionCommand =
              new PositionCommand(position.getPlayer(), position.getX(), position.getY());
          connection.send(otherPlayerPositionCommand.toString());
        }

        this.lock.unlock();
        break;
      case MOVE:
        this.lock.lock();
        var moveCommand = (MoveCommand) command;

        var playerAttached = (UUID) connection.getAttachment();

        if (playerAttached == null) {
          log.info(
              "Player should attempt to join before they send move commands");
          this.lock.unlock();
          return;
        }

        var maybeNextPosition =
            this.gameState.movePlayerInDirection(playerAttached, moveCommand.getDirection());

        if (maybeNextPosition.isPresent()) {
          var nextPosition = maybeNextPosition.get();

          var positionCommandToBroadcast =
              new PositionCommand(playerAttached, nextPosition.getX(), nextPosition.getY());

          log.info("Broadcast player move");
          broadcast(positionCommandToBroadcast.toString());

          this.notifyPlayerPosition(playerAttached, nextPosition);
        }

        this.lock.unlock();

        break;
      case POS:
        var positionCommand = (PositionCommand) command;
        this.forcePosition(
            positionCommand.getPlayer(), positionCommand.getX(), positionCommand.getY());

        broadcast(positionCommand.toString());
        break;
      case DC:
        var disconnectCommand = (DisconnectCommand) command;
        this.playerDisconnected(disconnectCommand.getPlayer());
        broadcast(disconnectCommand.toString());
        break;
    }

    log.info("Processed command: {}", command);
  }

  @Override
  public void onError(WebSocket connection, Exception ex) {
    log.error("An error occurred on connection: {}", connection.getRemoteSocketAddress(), ex);
  }

  @Override
  public void onStart() {
    log.info("Server started successfully");
  }

  private void forcePosition(UUID player, int x, int y) {
    log.info("Updating player to a new position");
    this.lock.lock();

    this.gameState.forcePosition(player, x, y);

    var positionCommandToBroadcast = new PositionCommand(player, x, y);

    log.info("Broadcast player move");
    broadcast(positionCommandToBroadcast.toString());

    this.lock.unlock();
  }

  private void playerDisconnected(UUID player) {
    log.info("Updating player to a new position");
    this.lock.lock();
    this.disconnectPlayerLocally(player);
    this.lock.unlock();
  }

  private void disconnectPlayerLocally(UUID player) {
    if (this.gameState.removePlayer(player)) {
      log.info("Broadcasting player disconnect");
      var dcCommand = new DisconnectCommand(player);
      broadcast(dcCommand.toString());
    }
  }

  private void notifyPlayerPosition(UUID player, Position position) {
    this.verifyServerPoolConnection();

    for (var server : this.otherGameServers) {
      PositionCommand positionCommand =
          new PositionCommand(player, position.getX(), position.getY());
      server.send(positionCommand.toString());
    }
  }

  private void notifyPlayerDisconnect(UUID player) {
    this.verifyServerPoolConnection();

    for (var server : this.otherGameServers) {
      DisconnectCommand disconnectCommand = new DisconnectCommand(player);
      server.send(disconnectCommand.toString());
    }
  }

  @SneakyThrows
  private void verifyServerPoolConnection() {
    for (var server : this.otherGameServers) {
      this.lock.lock();

      if (server.getReadyState() != ReadyState.OPEN) {
        log.info("Reconnect needed");
        server.connectBlocking();
      }

      this.lock.unlock();
    }
  }
}
