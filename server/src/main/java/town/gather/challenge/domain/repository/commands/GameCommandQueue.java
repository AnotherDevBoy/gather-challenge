package town.gather.challenge.domain.repository.commands;

import java.util.Optional;
import java.util.UUID;
import town.gather.challenge.domain.commands.MoveDirection;
import town.gather.challenge.domain.repository.commands.dto.GameCommandNotification;

public interface GameCommandQueue {
  void notifyPlayerDisconnected(UUID player);

  void notifyPlayerJoined(UUID player);

  void notifyPlayerMovement(UUID playerAttached, MoveDirection direction);

  Optional<GameCommandNotification> poll();
}
