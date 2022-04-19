package town.gather.challenge.domain.repository.gamestate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.List;
import lombok.SneakyThrows;
import town.gather.challenge.domain.game.Position;

public class RedisPlayerPositionRepository implements PlayerPositionRepository {
  private static final String PLAYER_POSITION_KEY = "players";

  private final RedisCommands redisCommands;
  private final ObjectMapper objectMapper;

  public RedisPlayerPositionRepository(RedisClient redisClient) {
    this.redisCommands = redisClient.connect().sync();
    this.objectMapper = new ObjectMapper();
  }

  @Override
  @SneakyThrows
  public List<Position> getPlayerPositions() {
    String serialized = (String) this.redisCommands.get(PLAYER_POSITION_KEY);

    return this.objectMapper.readValue(serialized, new TypeReference<>() {});
  }

  @Override
  @SneakyThrows
  public void updatePlayerPositions(List<Position> playerPositions) {
    var serialized = this.objectMapper.writeValueAsString(playerPositions);
    this.redisCommands.set(PLAYER_POSITION_KEY, serialized);
  }
}
