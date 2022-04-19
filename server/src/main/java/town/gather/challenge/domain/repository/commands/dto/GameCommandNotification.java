package town.gather.challenge.domain.repository.commands.dto;

import java.util.Optional;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import town.gather.challenge.domain.commands.CommandType;

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
    }

    return Optional.empty();
  }
}
