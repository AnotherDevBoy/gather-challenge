package town.gather.challenge.domain.repository.commands.dto;

import java.util.UUID;
import lombok.Getter;
import town.gather.challenge.domain.commands.CommandType;
import town.gather.challenge.domain.commands.MoveDirection;

@Getter
public class PlayerMovementNotification
    extends town.gather.challenge.domain.repository.commands.dto.GameCommandNotification {
  private final UUID player;
  private final MoveDirection moveDirection;

  public PlayerMovementNotification(UUID player, MoveDirection direction) {
    super(CommandType.MOVE);
    this.player = player;
    this.moveDirection = direction;
  }

  @Override
  public String toString() {
    return String.format("%s;%s;%s", CommandType.MOVE, this.player.toString(), this.moveDirection);
  }
}
