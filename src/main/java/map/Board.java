package map;


import coordinates.Vector2d;
import elements.IMapElement;

import java.util.*;


public class Board {
    private final Map<Vector2d, Set<IMapElement>> board;
    private final Vector2d jungleLowerLeft;
    private final Vector2d jungleUpperRight;
    private final Set<Vector2d> steppeEmptyFields;
    private final Set<Vector2d> jungleEmptyFields;

    public Board (Vector2d lowerLeft, Vector2d upperRight, Vector2d jungleLowerLeft, Vector2d jungleUpperRight) {
        this.steppeEmptyFields = new HashSet<>();
        this.jungleEmptyFields = new HashSet<>();
        this.jungleLowerLeft = jungleLowerLeft;
        this.jungleUpperRight = jungleUpperRight;
        for (int x = lowerLeft.x; x <= upperRight.x; x++) {
            for (int y = lowerLeft.y; y <= upperRight.y; y++) {
                Vector2d field = new Vector2d(x, y);
                if (isInJungle(field)) {
                    jungleEmptyFields.add(field);
                } else {
                steppeEmptyFields.add(field);
                }
            }
        }
        this.board = new HashMap<>();
    }

    private boolean isInJungle(Vector2d position) {
        return jungleLowerLeft.precedes(position) && jungleUpperRight.follows(position);
    }

    public void putOnBoard(IMapElement element) {
        Vector2d position = element.getPosition();
        board.putIfAbsent(position, new HashSet<>());
        board.get(position).add(element);
        jungleEmptyFields.remove(element.getPosition());
        steppeEmptyFields.remove(element.getPosition());
    }

    public void removeElement(Vector2d position, IMapElement element) {
        board.get(position).remove(element);
        if (board.get(position).isEmpty()) {
            if (isInJungle(position)) {
                jungleEmptyFields.add(position);
            } else {
                steppeEmptyFields.add(position);
            }
        }
    }

    public Optional<Vector2d> getRandomEmptySteppeField() {
        if (steppeEmptyFields.isEmpty()) {
            return Optional.empty();
        }
        int randomPosition = (int) (Math.random() * steppeEmptyFields.size());
        return Optional.of((Vector2d) steppeEmptyFields.toArray()[randomPosition]);
    }

    public Optional<Vector2d> getRandomEmptyJungleField() {
        if (jungleEmptyFields.isEmpty()) {
            return Optional.empty();
        }
        int randomPosition = (int) (Math.random() * jungleEmptyFields.size());
        return Optional.of((Vector2d) jungleEmptyFields.toArray()[randomPosition]);
    }

    public Set<? extends IMapElement> getElements(Vector2d position) {
        board.putIfAbsent(position, new HashSet<>());
        return board.get(position);
    }
}
