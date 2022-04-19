package town.gather.challenge;

import java.net.URI;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class GameServerInternalClient extends WebSocketClient {
  public GameServerInternalClient(URI serverUri, GameServer gameServer) {
    super(serverUri);
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    System.out.println("Client::New connection opened");
  }

  @Override
  public void onMessage(String message) {
    System.out.println("Client::Received message " + message);
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    System.out.println("Client::Closed with exit code " + code + " additional info: " + reason);
  }

  @Override
  public void onError(Exception ex) {
    System.err.println("Client::An error occurred:" + ex);
  }
}
