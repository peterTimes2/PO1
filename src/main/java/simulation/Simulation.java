package simulation;

import map.WorldMap;

import java.util.LinkedList;
import java.util.List;

public class Simulation {
    private final WorldMap map;
    private final List<ISimulationObserver> observers;

    public Simulation() {
        this.observers = new LinkedList<>();
        this.map = new WorldMap(observers);
    }

    public void makeMove() {
        map.morningRoutine();
        map.turnAndMoveAnimals();
        map.feedAnimals();
        map.breedAnimals();
        map.addPlants();
        for (ISimulationObserver observer: observers) {
            observer.handleDayFinished();
        }
    }

    public void addObserver(ISimulationObserver observer) {
        observers.add(observer);
        map.handleAddingObserver(observer);
    }
}
