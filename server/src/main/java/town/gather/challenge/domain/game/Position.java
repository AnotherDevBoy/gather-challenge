package town.gather.challenge.domain.game;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;

@Getter
@With
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Position {
  private int x;
  private int y;

  @EqualsAndHashCode.Exclude @Setter private UUID player;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
    this.player = null;
  }
}
