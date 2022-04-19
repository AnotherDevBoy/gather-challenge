package town.gather.challenge.worker;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RedissonClient;
import town.gather.challenge.domain.commands.DisconnectCommand;
import town.gather.challenge.domain.commands.PositionCommand;
import town.gather.challenge.domain.game.GameState;
import town.gather.challenge.domain.game.GameStateManager;
import town.gather.challenge.domain.repository.GameCommandQueue;

import java.util.UUID;

public class GameStateWorker implements Runnable {
  private final GameCommandQueue queue;
  private final GameState state;

  public GameStateWorker(GameCommandQueue queue) {
    this.queue = queue;

    // TODO: Fill state from Redis
    this.state = new GameState();
  }

  @Override
  public void run() {
    while (true) {
      var command = this.queue.poll();

      switch (command.getType()) {
        case MOVE:
          // Execute move command
          // Persist new state
          break;
        case JOIN:
          // Execute move command
          // Persist new state
          break;
        case DC:
          // Execute move command
          // Persist new state
          break;
      }
    }
  }
}
