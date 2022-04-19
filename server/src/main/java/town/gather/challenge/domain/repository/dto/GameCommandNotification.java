package town.gather.challenge.domain.repository.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import town.gather.challenge.domain.commands.CommandType;

@Getter
@RequiredArgsConstructor
public abstract class GameCommandNotification {
  private final CommandType type;
}
