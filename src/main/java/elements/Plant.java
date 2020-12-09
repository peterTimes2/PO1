package elements;

import coordinates.Vector2d;
import map.WorldMap;

public class Plant extends AbstractMapElement {
    public Plant(Vector2d position, WorldMap map) {
        super(position, map);
    }

    @Override
    public boolean isConsumable() {
        return true;
    }

    @Override
    public boolean isReproducible() {
        return false;
    }
}
