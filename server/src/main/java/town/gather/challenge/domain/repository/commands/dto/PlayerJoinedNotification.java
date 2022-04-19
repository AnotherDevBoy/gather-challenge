package town.gather.challenge.domain.repository.commands.dto;

import lombok.Getter;
import town.gather.challenge.domain.commands.CommandType;

import java.util.UUID;

@Getter
public class PlayerJoinedNotification
    extends GameCommandNotification {
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
