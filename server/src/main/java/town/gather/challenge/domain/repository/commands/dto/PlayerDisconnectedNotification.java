package town.gather.challenge.domain.repository.commands.dto;

import lombok.Getter;
import town.gather.challenge.domain.commands.CommandType;

import java.util.UUID;

@Getter
public class PlayerDisconnectedNotification
    extends GameCommandNotification {
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
