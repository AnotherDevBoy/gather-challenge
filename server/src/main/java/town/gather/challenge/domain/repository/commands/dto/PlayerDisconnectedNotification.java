package town.gather.challenge.domain.repository.commands.dto;

import java.util.UUID;
import lombok.Getter;
import town.gather.challenge.domain.commands.CommandType;

@Getter
public class PlayerDisconnectedNotification
    extends town.gather.challenge.domain.repository.commands.dto.GameCommandNotification {
  private final UUID player;

  public PlayerDisconnectedNotification(UUID player) {
    super(CommandType.DC);
    this.player = player;
  }

  @Override
  public String toString() {
    return String.format("%s;%s", CommandType.DC, this.player.toString());
  }
}
