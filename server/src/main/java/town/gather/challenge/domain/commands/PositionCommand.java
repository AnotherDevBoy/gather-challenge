package town.gather.challenge.domain.commands;

import java.util.UUID;
import lombok.Getter;

@Getter
public class PositionCommand extends Command {
  private final UUID player;
  private final int x;
  private final int y;

  public PositionCommand(UUID player, int x, int y) {
    super(CommandType.POS);
    this.player = player;
    this.x = x;
    this.y = y;
  }

  @Override
  public String toString() {
    return String.format("pos %s %d %d", this.player.toString(), this.x, this.y);
  }
}
