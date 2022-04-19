package town.gather.challenge.worker;

import lombok.extern.slf4j.Slf4j;
import town.gather.challenge.domain.game.GameState;
import town.gather.challenge.domain.game.Position;
import town.gather.challenge.domain.repository.commands.GameCommandQueue;
import town.gather.challenge.domain.repository.commands.dto.PlayerDisconnectedNotification;
import town.gather.challenge.domain.repository.commands.dto.PlayerJoinedNotification;
import town.gather.challenge.domain.repository.commands.dto.PlayerMovementNotification;
import town.gather.challenge.domain.repository.gamestate.PlayerPositionRepository;

import java.util.List;

@Slf4j
public class GameStateWorker implements Runnable {
  private final GameState gameState;
  private final PlayerPositionRepository playerPositionRepository;
  private final GameCommandQueue queue;

  public GameStateWorker(
      PlayerPositionRepository playerPositionRepository, GameCommandQueue queue) {
    this.playerPositionRepository = playerPositionRepository;

    var players = this.playerPositionRepository.getPlayerPositions();
    this.gameState = new GameState(players);

    this.queue = queue;
  }

  @Override
  public void run() {
    log.info("Starting GameStateWorker");

    while (true) {
      try {
        var maybeCommand = this.queue.poll();

        if (maybeCommand.isEmpty()) {
          continue;
        }

        log.info("Received command");
        var commandNotification = maybeCommand.get();

        List<Position> playerPositions = null;

        switch (commandNotification.getType()) {
          case JOIN:
            log.info("Processing join command notification");
            PlayerJoinedNotification joined = (PlayerJoinedNotification) commandNotification;
            var emptyPosition = this.gameState.moveToEmptyPosition(joined.getPlayer());

            if (emptyPosition.isEmpty()) {
              log.warn("Could not add player to game because the map is full");
              break;
            }

            playerPositions = this.gameState.getAllPlayerPositions();
            break;
          case MOVE:
            log.info("Processing move command notification");
            PlayerMovementNotification move = (PlayerMovementNotification) commandNotification;

            var maybeNextPosition =
                    this.gameState.movePlayerInDirection(move.getPlayer(), move.getMoveDirection());

            if (maybeNextPosition.isPresent()) {
              playerPositions = this.gameState.getAllPlayerPositions();
            } else {
              log.info("Couldn't move player to the desired location");
            }

            break;
          case DC:
            log.info("Processing dc command notification");
            PlayerDisconnectedNotification dc = (PlayerDisconnectedNotification) commandNotification;

            if (this.gameState.removePlayer(dc.getPlayer())) {
              playerPositions = this.gameState.getAllPlayerPositions();
            }
            break;
        }

        // This means the game state has been modified and needs to be persisted
        if (playerPositions != null) {
          this.playerPositionRepository.updatePlayerPositions(playerPositions);
        }
      } catch (Exception e) {
        log.error("The worker almost crashed", e);
      }
    }
  }
}
