package town.gather.challenge.domain.commands;

import java.util.UUID;
import lombok.Getter;

@Getter
public class DisconnectCommand extends Command {
  private final UUID player;

  public DisconnectCommand(UUID player) {
    super(CommandType.DC);
    this.player = player;
  }

  @Override
  public String toString() {
    return String.format("dc %s", this.player.toString());
  }
}
