package town.gather.challenge.domain.game;

import com.google.common.collect.Sets;
import town.gather.challenge.domain.commands.MoveDirection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameState {
  private final Position[][] map;

  private GameState() {
    this.map = new Position[20][20];
    IntStream.range(0, 20)
        .forEach(i -> IntStream.range(0, 20).forEach(j -> this.map[i][j] = new Position(i, j)));
  }

  public GameState(List<Position> players) {
    this();

    players.forEach(p -> this.map[p.getX()][p.getY()] = p);
  }

  public List<Position> getAllPlayerPositions() {
    return Arrays.stream(this.map)
        .flatMap(Arrays::stream)
        .filter(p -> p.getPlayer() != null)
        .collect(Collectors.toList());
  }

  public boolean removePlayer(UUID player) {
    var maybePlayerPosition =
        Arrays.stream(this.map)
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
        Arrays.stream(this.map)
            .flatMap(Arrays::stream)
            .filter(p -> p.getPlayer() == null)
            .findAny();

    if (maybeEmptyPosition.isEmpty()) {
      return Optional.empty();
    }

    var position = maybeEmptyPosition.get();

    this.map[position.getX()][position.getY()].setPlayer(player);

    return Optional.of(position);
  }

  public Optional<Position> movePlayerInDirection(UUID player, MoveDirection direction) {
    var maybePlayerPosition =
        Arrays.stream(this.map)
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

  public List<Position> playersThatJoined(GameState newState) {
    var currentPlayers =
        Arrays.stream(this.map)
            .flatMap(Arrays::stream)
            .filter(p -> p.getPlayer() != null)
            .map(Position::getPlayer)
            .collect(Collectors.toSet());
    var newPlayers =
        Arrays.stream(newState.map)
            .flatMap(Arrays::stream)
            .filter(p -> p.getPlayer() != null)
            .map(Position::getPlayer)
            .collect(Collectors.toSet());

    Set<UUID> playersThatJoined = Sets.difference(newPlayers, currentPlayers);

    return Arrays.stream(newState.map)
        .flatMap(Arrays::stream)
        .filter(p -> p.getPlayer() != null && playersThatJoined.contains(p.getPlayer()))
        .collect(Collectors.toList());
  }

  public List<Position> playersThatDisconnected(GameState newState) {
    return List.of();
  }

  public List<Position> playersThatMoved(GameState newState) {
    return List.of();
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
}
