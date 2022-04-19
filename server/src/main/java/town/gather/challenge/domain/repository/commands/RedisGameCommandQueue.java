package town.gather.challenge.domain.repository.commands;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import town.gather.challenge.domain.commands.MoveDirection;
import town.gather.challenge.domain.repository.commands.dto.GameCommandNotification;
import town.gather.challenge.domain.repository.commands.dto.PlayerDisconnectedNotification;
import town.gather.challenge.domain.repository.commands.dto.PlayerJoinedNotification;
import town.gather.challenge.domain.repository.commands.dto.PlayerMovementNotification;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class RedisGameCommandQueue implements GameCommandQueue {
  private static final String COMMAND_LIST_NAME = "commands";
  private static final int SECONDS_TO_WAIT = 1;

  private final RedisCommands redisCommands;

  public RedisGameCommandQueue(RedisClient redisClient) {
    var connect = redisClient.connect();
    connect.setAutoFlushCommands(true);
    this.redisCommands = connect.sync();
  }

  @Override
  public void notifyPlayerDisconnected(UUID player) {
    this.redisCommands.rpush(
        COMMAND_LIST_NAME, new PlayerDisconnectedNotification(player).toString());
  }

  @Override
  public void notifyPlayerJoined(UUID player) {
    this.redisCommands.rpush(COMMAND_LIST_NAME, new PlayerJoinedNotification(player).toString());
  }

  @Override
  public void notifyPlayerMovement(UUID playerAttached, MoveDirection direction) {
    this.redisCommands.rpush(
        COMMAND_LIST_NAME, new PlayerMovementNotification(playerAttached, direction).toString());
  }

  @Override
  public Optional<GameCommandNotification> poll() {
    var commands =
        this.redisCommands.blpop(SECONDS_TO_WAIT, COMMAND_LIST_NAME);

    if (commands == null) {
      return Optional.empty();
    }

    return GameCommandNotification.fromString((String) commands.getValue());
  }
}
