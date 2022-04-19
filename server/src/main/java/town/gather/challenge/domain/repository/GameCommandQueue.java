package town.gather.challenge.domain.repository;

import town.gather.challenge.domain.commands.MoveDirection;
import town.gather.challenge.domain.repository.dto.GameCommandNotification;

import java.util.UUID;
import java.util.function.Consumer;

public interface GameCommandQueue {
  void notifyPlayerDisconnected(UUID player);

  void notifyPlayerJoined(UUID player);

  void notifyPlayerMovement(UUID playerAttached, MoveDirection direction);

  GameCommandNotification poll();
}
