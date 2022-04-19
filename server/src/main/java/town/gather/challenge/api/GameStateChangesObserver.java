package town.gather.challenge.api;

import town.gather.challenge.domain.game.Position;

import java.util.List;
import java.util.UUID;

public interface GameStateChangesObserver {
  void onPlayerMoved(Position position);

  void onPlayerDisconnected(UUID player);
}
