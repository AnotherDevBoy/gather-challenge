package town.gather.challenge.api;

import java.util.UUID;
import town.gather.challenge.domain.game.Position;

public interface GameStateChangesObserver {
  void onPlayerMoved(Position position);

  void onPlayerDisconnected(UUID player);
}
