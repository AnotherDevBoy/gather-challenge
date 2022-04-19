package town.gather.challenge;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class App {
  public static void main(String[] args) throws URISyntaxException, InterruptedException {
    String host = "localhost";

    var uri1 = new URI("ws://localhost:31415");
    var uri2 = new URI("ws://localhost:31416");
    var uri3 = new URI("ws://localhost:31417");

    Thread server1 =
        new Thread(new GameServer(new InetSocketAddress(host, 31415), List.of(uri2, uri3)));
    Thread server2 =
        new Thread(new GameServer(new InetSocketAddress(host, 31416), List.of(uri1, uri3)));
    Thread server3 =
        new Thread(new GameServer(new InetSocketAddress(host, 31417), List.of(uri1, uri2)));

    server1.start();
    server2.start();
    server3.start();
  }
}
