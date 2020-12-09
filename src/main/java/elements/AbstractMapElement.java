package elements;

import coordinates.Vector2d;
import map.WorldMap;

import java.util.LinkedList;
import java.util.List;

abstract public class AbstractMapElement implements IMapElement {
    private Vector2d position;
    private final List<IMapElementObserver> observers;
    protected final WorldMap map;

    public AbstractMapElement(Vector2d position, WorldMap map) {
        this.observers = new LinkedList<>();
        this.position = position;
        this.map = map;
    }

    @Override
    public Vector2d getPosition() {
        return position;
    }

    @Override
    public void setPosition(Vector2d position) {
        Vector2d oldValue = this.position;
        this.position = position;
        this.notifyObservers(MapElementAction.POSITION_CHANGED, oldValue);
    }

    @Override
    public void addObserver(IMapElementObserver observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(IMapElementObserver observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(MapElementAction context, Object oldValue) {
        for (IMapElementObserver observer: this.observers) {
            observer.handleElementChange(this, context, oldValue);
        }
    }
}
