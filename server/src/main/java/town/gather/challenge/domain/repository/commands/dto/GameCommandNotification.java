package town.gather.challenge.domain.repository.commands.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import town.gather.challenge.domain.commands.CommandType;
import town.gather.challenge.domain.commands.MoveDirection;

import java.util.Optional;
import java.util.UUID;

@Getter
@RequiredArgsConstructor
public abstract class GameCommandNotification {
  protected static final String SEPARATOR = ";";
  private final CommandType type;

  public static Optional<GameCommandNotification> fromString(String serialized) {
    var tokens = serialized.split(SEPARATOR);

    var type = CommandType.valueOf(tokens[0]);

    switch (type) {
      case DC:
        return Optional.of(new PlayerDisconnectedNotification(UUID.fromString(tokens[1])));
      case JOIN:
        return Optional.of(new PlayerJoinedNotification(UUID.fromString(tokens[1])));
      case MOVE:
        return Optional.of(new PlayerMovementNotification(UUID.fromString(tokens[1]), MoveDirection.valueOf(tokens[2])));
    }

    return Optional.empty();
  }
}
