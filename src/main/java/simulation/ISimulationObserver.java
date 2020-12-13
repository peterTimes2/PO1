package simulation;
import elements.IMapElementObserver;

public interface ISimulationObserver extends IMapElementObserver {
    public void handleDayFinished();
}
