package town.gather.challenge.domain.commands;

import java.util.UUID;
import lombok.Getter;

@Getter
public class JoinCommand extends Command {
  private final UUID player;

  public JoinCommand(UUID player) {
    super(CommandType.JOIN);
    this.player = player;
  }

  @Override
  public String toString() {
    return String.format("join %s", this.player.toString());
  }
}
