package town.gather.challenge;

import java.net.URI;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

@Slf4j
public class GameServerInternalClient extends WebSocketClient {
  public GameServerInternalClient(URI serverUri) {
    super(serverUri);
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    log.info("New connection opened");
  }

  @Override
  public void onMessage(String message) {
    log.info("Received message {}", message);
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    log.info("Closed with exit code {}. Additional info: {}", code,  reason);
  }

  @Override
  public void onError(Exception ex) {
    log.error("An error occurred", ex);
  }
}
