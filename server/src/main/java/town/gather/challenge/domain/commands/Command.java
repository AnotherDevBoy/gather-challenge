package town.gather.challenge.domain.commands;

import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Command {
  private final CommandType type;

  public static Optional<Command> fromString(String commandString) {
    var tokens = commandString.split(" ");

    if (tokens.length < 2) {
      return Optional.empty();
    }

    var commandType = CommandType.valueOf(tokens[0].toUpperCase());

    try {
      switch (commandType) {
        case JOIN:
          return Optional.of(new JoinCommand(UUID.fromString(tokens[1])));
        case MOVE:
          return Optional.of(new MoveCommand(MoveDirection.valueOf(tokens[1].toUpperCase())));
        case POS:
          return Optional.of(
              new PositionCommand(
                  UUID.fromString(tokens[1]),
                  Integer.parseInt(tokens[2]),
                  Integer.parseInt(tokens[3])));
        case DC:
          return Optional.of(new DisconnectCommand(UUID.fromString(tokens[1])));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }
}
