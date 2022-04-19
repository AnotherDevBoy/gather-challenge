package town.gather.challenge.domain.repository.gamestate;

import java.util.List;
import town.gather.challenge.domain.game.Position;

public interface PlayerPositionRepository {
  List<Position> getPlayerPositions();

  void updatePlayerPositions(List<Position> playerPositions);
}
