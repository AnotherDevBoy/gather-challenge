package town.gather.challenge;

import io.lettuce.core.RedisClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import town.gather.challenge.api.GameWebSocketApi;
import town.gather.challenge.domain.repository.RedisGameCommandQueue;
import town.gather.challenge.worker.GameStateWorker;

import java.net.InetSocketAddress;

@Slf4j
public class App {
  @SneakyThrows
  public static void main(String[] args) {
    var redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379);
    redis.start();

    String host = "localhost";


    RedisClient redisClient = RedisClient.create(String.format("redis://%s:%s", redis.getHost(), redis.getFirstMappedPort()));

    var commandQueue = new RedisGameCommandQueue(redisClient);

    Thread server1 =
        new Thread(new GameWebSocketApi(new InetSocketAddress(host, 31415), commandQueue));
    Thread server2 =
        new Thread(new GameWebSocketApi(new InetSocketAddress(host, 31416), commandQueue));
    Thread server3 =
        new Thread(new GameWebSocketApi(new InetSocketAddress(host, 31417), commandQueue));

    Thread worker = new Thread(new GameStateWorker(commandQueue));

    log.info("Starting servers");
    server1.start();
    server2.start();
    server3.start();
  }
}
