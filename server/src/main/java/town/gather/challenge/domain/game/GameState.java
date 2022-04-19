package town.gather.challenge.domain.game;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import town.gather.challenge.domain.commands.MoveDirection;

public class GameState {
  private final Position[][] map;

  public GameState() {
    this.map = new Position[20][20];
    initializeMap();
  }

  private void initializeMap() {
    IntStream.range(0, 20)
        .forEach(
            i -> {
              IntStream.range(0, 20)
                  .forEach(
                      j -> {
                        this.map[i][j] = new Position(i, j);
                      });
            });
  }

  public List<Position> getAllPlayerPositions() {
    return Arrays.stream(map)
        .flatMap(Arrays::stream)
        .filter(p -> p.getPlayer() != null)
        .collect(Collectors.toList());
  }

  public boolean removePlayer(UUID player) {
    var maybePlayerPosition =
        Arrays.stream(map)
            .flatMap(Arrays::stream)
            .filter(p -> p.getPlayer() != null && p.getPlayer().equals(player))
            .findAny();

    if (maybePlayerPosition.isEmpty()) {
      return false;
    }

    var position = maybePlayerPosition.get();
    this.map[position.getX()][position.getY()].setPlayer(null);

    return true;
  }

  public Optional<Position> moveToEmptyPosition(UUID player) {
    var maybeEmptyPosition =
        Arrays.stream(map).flatMap(Arrays::stream).filter(p -> p.getPlayer() == null).findAny();

    if (maybeEmptyPosition.isEmpty()) {
      return Optional.empty();
    }

    var position = maybeEmptyPosition.get();

    this.map[position.getX()][position.getY()].setPlayer(player);

    return Optional.of(position);
  }

  public Optional<Position> movePlayerInDirection(UUID player, MoveDirection direction) {
    var maybePlayerPosition =
        Arrays.stream(map)
            .flatMap(Arrays::stream)
            .filter(p -> p.getPlayer() != null && p.getPlayer().equals(player))
            .findAny();

    if (maybePlayerPosition.isEmpty()) {
      return Optional.empty();
    }

    var playerPosition = maybePlayerPosition.get();
    var nextPosition = calculateNextPosition(playerPosition, direction);

    if (!isWithinBoundaries(nextPosition)) {
      return Optional.empty();
    }

    if (this.map[nextPosition.getX()][nextPosition.getY()].getPlayer() != null) {
      return Optional.empty();
    }

    this.map[playerPosition.getX()][playerPosition.getY()].setPlayer(null);
    this.map[nextPosition.getX()][nextPosition.getY()].setPlayer(player);

    return Optional.of(nextPosition);
  }

  private boolean isWithinBoundaries(Position nextPosition) {
    return nextPosition.getX() >= 0
        && nextPosition.getX() < 20
        && nextPosition.getY() >= 0
        && nextPosition.getY() < 20;
  }

  private static Position calculateNextPosition(Position position, MoveDirection direction) {
    switch (direction) {
      case UP:
        return position.withY(position.getY() - 1);
      case DOWN:
        return position.withY(position.getY() + 1);
      case LEFT:
        return position.withX(position.getX() - 1);
      case RIGHT:
        return position.withX(position.getX() + 1);
    }

    throw new IllegalArgumentException();
  }

  public void forcePosition(UUID player, int x, int y) {
    var maybePlayerPosition =
        Arrays.stream(map)
            .flatMap(Arrays::stream)
            .filter(p -> p.getPlayer() != null && p.getPlayer().equals(player))
            .findAny();

    if (maybePlayerPosition.isPresent()) {
      var playerPosition = maybePlayerPosition.get();
      this.map[playerPosition.getX()][playerPosition.getX()].setPlayer(null);
    }

    this.map[x][y].setPlayer(player);
  }
}
