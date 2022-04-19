package town.gather.challenge.domain.commands;

import lombok.Getter;

@Getter
public class MoveCommand extends Command {
  private final MoveDirection direction;

  public MoveCommand(MoveDirection direction) {
    super(CommandType.MOVE);
    this.direction = direction;
  }

  @Override
  public String toString() {
    return String.format("move %s", this.direction.toString().toLowerCase());
  }
}
