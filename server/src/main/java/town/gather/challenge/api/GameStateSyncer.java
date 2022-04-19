package town.gather.challenge.api;

import lombok.extern.slf4j.Slf4j;
import town.gather.challenge.domain.game.GameState;
import town.gather.challenge.domain.repository.gamestate.PlayerPositionRepository;

@Slf4j
public class GameStateSyncer implements Runnable {
  private final PlayerPositionRepository playerPositionRepository;
  private final GameStateChangesObserver observer;
  private final GameState gameState;

  public GameStateSyncer(
      GameState gameState,
      PlayerPositionRepository playerPositionRepository,
      GameStateChangesObserver observer) {
    this.gameState = gameState;
    this.playerPositionRepository = playerPositionRepository;
    this.observer = observer;
  }

  @Override
  public void run() {
    try {
      var allPlayers = this.playerPositionRepository.getPlayerPositions();

      var newState = new GameState(allPlayers);

      var playersThatJoined = this.gameState.playersThatJoined(newState);

      for (var j : playersThatJoined) {
        observer.onPlayerMoved(j);
      }

      var playersThatDisconnected = this.gameState.playersThatDisconnected(newState);

      for (var dc : playersThatDisconnected) {
        this.observer.onPlayerDisconnected(dc.getPlayer());
      }

      var playersThatMoved = this.gameState.playersThatMoved(newState);

      for (var mv : playersThatMoved) {
        this.observer.onPlayerMoved(mv);
      }
    } catch (Exception e) {
      log.warn("An error occurred while trying to sync the game state");
    }
  }
}
