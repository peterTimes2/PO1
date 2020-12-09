package elements;

public interface IMapElementObserver {
    void handleElementChange(IMapElement eventTarget, MapElementAction context, Object oldValue);
}
