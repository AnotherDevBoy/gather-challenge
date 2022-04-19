package town.gather.challenge.api;

import lombok.extern.slf4j.Slf4j;
import town.gather.challenge.domain.game.GameState;

@Slf4j
public class GameStateSyncer implements Runnable {
  private GameState gameState;
  private final GameStateChangesObserver observer;

  public GameStateSyncer(GameStateChangesObserver observer) {
    this.gameState = new GameState();
    this.observer = observer;
  }

  @Override
  public void run() {
    try {
      // TODO: Fill state from Redis
      // TODO: Check state diff

      // For each player that joined
      //observer.onPlayerJoined();

    } catch (Exception e) {
      log.warn("An error occurred while trying to sync the game state");
    }
  }
}
