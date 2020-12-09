package elements;

import coordinates.Vector2d;

public interface IMapElement {
    Vector2d getPosition();
    void setPosition(Vector2d position);
    void addObserver(IMapElementObserver observer);
    void removeObserver(IMapElementObserver observer);
    void notifyObservers(MapElementAction context, Object oldValue);
    boolean isConsumable();
    boolean isReproducible();
}
