package coordinates;

import java.util.Objects;

public class Vector2d {
    final public int x;
    final public int y;

    public Vector2d(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + ")";
    }

    @Override
    public boolean equals(Object other) {
        if(!other.getClass().equals(this.getClass())) {
            return false;
        }
        Vector2d otherVector = (Vector2d) other;
        return x == otherVector.x && y == otherVector.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public boolean precedes(Vector2d other) {
        return x <= other.x && y <= other.y;
    }

    public boolean follows(Vector2d other) {
        return x >= other.x && y >= other.y;
    }

    public Vector2d upperRight(Vector2d other) {
        return new Vector2d(Math.max(x, other.x), Math.max(y, other.y));
    }

    public Vector2d lowerLeft(Vector2d other) {
        return new Vector2d(Math.min(x, other.x), Math.min(y, other.y));
    }

    public Vector2d add(Vector2d other) {
        return new Vector2d(x + other.x, y + other.y);
    }

    public Vector2d subtract(Vector2d other) {
        return new Vector2d(x - other.x, y - other.y);
    }

    public Vector2d opposite() {
        return new Vector2d(-x, -y);
    }

    public Vector2d fitToRectangle(Vector2d lowerLeft, Vector2d upperRight) {
        int width = upperRight.x - lowerLeft.x + 1;
        int height = upperRight.y - lowerLeft.y + 1;
        int newX = (x - lowerLeft.x) % width;
        int newY = (y - lowerLeft.y) % height;
        if (x < lowerLeft.x) {
            newX = upperRight.x + newX + 1;
        }
        if (y < lowerLeft.y) {
            newY = upperRight.y + newY + 1;
        }
        Vector2d relativePos = new Vector2d(newX, newY);
        return lowerLeft.add(relativePos);
    }
}
