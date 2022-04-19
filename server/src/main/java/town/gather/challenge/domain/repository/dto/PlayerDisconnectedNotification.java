package town.gather.challenge.domain.repository.dto;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerDisconnectedNotification extends GameCommandNotification {
  private final UUID player;
}
