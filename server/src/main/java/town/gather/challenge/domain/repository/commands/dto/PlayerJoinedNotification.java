package town.gather.challenge.domain.repository.commands.dto;

import java.util.UUID;
import lombok.Getter;
import town.gather.challenge.domain.commands.CommandType;

@Getter
public class PlayerJoinedNotification
    extends town.gather.challenge.domain.repository.commands.dto.GameCommandNotification {
  private final UUID player;

  public PlayerJoinedNotification(UUID player) {
    super(CommandType.JOIN);
    this.player = player;
  }

  @Override
  public String toString() {
    return String.format("%s;%s", CommandType.JOIN, this.player.toString());
  }
}
