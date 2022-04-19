package town.gather.challenge.domain.repository;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;
import town.gather.challenge.domain.commands.MoveDirection;
import town.gather.challenge.domain.repository.dto.GameCommandNotification;

import java.util.UUID;

@RequiredArgsConstructor
public class RedisGameCommandQueue implements GameCommandQueue {
  private final RedisCommands redisCommands;

  public RedisGameCommandQueue(RedisClient redisClient) {
    this.redisCommands = redisClient.connect().sync();
  }

  @Override
  public void notifyPlayerDisconnected(UUID player) {

  }

  @Override
  public void notifyPlayerJoined(UUID player) {

  }

  @Override
  public void notifyPlayerMovement(UUID playerAttached, MoveDirection direction) {

  }

  @Override
  public GameCommandNotification poll() {
    KeyValue<String, String> commands = this.redisCommands.blpop(1, "commands");

    // Deserialize
  }
}
