package simulation;

import map.WorldMap;

public class Simulation {
    private final WorldMap map;
    public Simulation() {
        this.map = new WorldMap();
    }
    public void makeMove() {
        map.morningRoutine();
        map.turnAndMoveAnimals();
        map.feedAnimals();
        map.breedAnimals();
        map.addPlants();
    }

}
