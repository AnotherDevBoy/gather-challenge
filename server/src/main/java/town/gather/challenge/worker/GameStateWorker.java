package town.gather.challenge.worker;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import town.gather.challenge.domain.game.GameState;
import town.gather.challenge.domain.game.Position;
import town.gather.challenge.domain.repository.commands.GameCommandQueue;
import town.gather.challenge.domain.repository.commands.dto.PlayerDisconnectedNotification;
import town.gather.challenge.domain.repository.commands.dto.PlayerJoinedNotification;
import town.gather.challenge.domain.repository.commands.dto.PlayerMovementNotification;
import town.gather.challenge.domain.repository.gamestate.PlayerPositionRepository;

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
    while (true) {
      var maybeCommand = this.queue.poll();

      if (maybeCommand.isEmpty()) {
        continue;
      }

      var commandNotification = maybeCommand.get();

      List<Position> playerPositions = null;

      switch (commandNotification.getType()) {
        case JOIN:
          PlayerJoinedNotification joined = (PlayerJoinedNotification) commandNotification;
          var emptyPosition = this.gameState.moveToEmptyPosition(joined.getPlayer());

          if (emptyPosition.isEmpty()) {
            log.warn("Could not add player to game because the map is full");
            break;
          }

          playerPositions = this.gameState.getAllPlayerPositions();
          break;
        case MOVE:
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
    }
  }
}
