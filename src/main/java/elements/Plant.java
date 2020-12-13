package elements;

import coordinates.Vector2d;
import map.WorldMap;

public class Plant extends AbstractMapElement {
    public Plant(Vector2d position, WorldMap map) {
        super(position, map);
        map.placePlant(this);
        notifyObservers(MapElementAction.PLANT_ADDED, null);
    }

    public void getEaten() {
        notifyObservers(MapElementAction.PLANT_EATEN,null);
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
