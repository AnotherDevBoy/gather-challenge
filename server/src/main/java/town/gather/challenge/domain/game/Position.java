package town.gather.challenge.domain.game;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.With;

@Getter
@With
@AllArgsConstructor
public class Position {
  private final int x;
  private final int y;

  @Setter private UUID player;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
    this.player = null;
  }
}
