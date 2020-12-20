package simulation;

import configs.Config;
import map.WorldMap;

import java.util.LinkedList;
import java.util.List;

public class Simulation {
    private final WorldMap map;
    private final List<ISimulationObserver> observers;
    private final Thread simulationThread;
    private boolean isRunning;

    public Simulation() {
        this.observers = new LinkedList<>();
        this.map = new WorldMap(observers);
        this.simulationThread = new Thread(this::run);
    }

    private void makeMove() {
        map.morningRoutine();
        map.turnAndMoveAnimals();
        map.feedAnimals();
        map.breedAnimals();
        map.addPlants();
        for (ISimulationObserver observer: observers) {
            observer.handleDayFinished();
        }
    }

    private void run() {
        while (true) {
            try {
                Thread.sleep(Config.getDayRefreshTime());
                if (!isRunning) {
                    synchronized (simulationThread) {
                        simulationThread.wait();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            makeMove();
        }
    }

    public void startSimulation() {
        simulationThread.start();
        isRunning = true;
    }

    public void stopSimulation() {
        isRunning = false;
    }

    public void resumeSimulation() {
        synchronized (simulationThread) {
            simulationThread.notify();
        }
        isRunning = true;
    }

    public boolean getIsRunning() {
        return isRunning;
    }

    public void addObserver(ISimulationObserver observer) {
        observers.add(observer);
        map.handleAddingObserver(observer);
    }
}
