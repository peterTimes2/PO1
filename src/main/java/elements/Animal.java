package elements;

import configs.Config;
import coordinates.MapDirection;
import coordinates.Vector2d;
import map.WorldMap;

import java.util.List;


public class Animal extends AbstractMapElement {
    private int energy;
    private MapDirection orientation;
    private Genotype genotype;

    public Animal(Vector2d position, WorldMap map) {
        super(position, map);
        this.energy = Config.getStartEnergy();
        this.orientation = MapDirection.NORTH;
        this.genotype = new Genotype();
        map.placeAnimal(this);
        notifyObservers(MapElementAction.ANIMAL_BORN, null);
    }

    public Animal(Animal mom, Animal dad, Vector2d position) {
        super(position, mom.map);
        this.genotype = new Genotype(mom.genotype, dad.genotype);
        this.energy = (mom.energy + dad.energy) / 4;
        this.orientation = MapDirection.randomDirection();
        map.placeAnimal(this);
        notifyObservers(MapElementAction.ANIMAL_BORN, null);
    }

    @Override
    public boolean isConsumable() {
        return false;
    }

    @Override
    public boolean isReproducible() {
        return true;
    }

    public Animal growFamily(Animal partner) {
        energy = energy * 3 / 4;
        partner.energy = partner.energy * 3 / 4;
        List<Vector2d> emptyNeighbours = map.getEmptyNeighbours(getPosition());
        Vector2d childPosition = getPosition().add(MapDirection.randomDirection().toUnitVector());
        if (!emptyNeighbours.isEmpty()) {
            childPosition = emptyNeighbours.get((int)(Math.random() * emptyNeighbours.size()));
        }
        return new Animal(this, partner, childPosition);
    }

    public void turnAndMove() {
        if (energy < Config.getMoveEnergy()) {
            notifyObservers(MapElementAction.ANIMAL_DIED, null);
            return;
        }
        orientation = orientation.add(genotype.getRandomDirection());
        Vector2d newPosition = getPosition().add(orientation.toUnitVector());
        newPosition = newPosition.fitToRectangle(map.getLowerLeft(), map.getUpperRight());
        setPosition(newPosition);
        energy -= Config.getMoveEnergy();
    }

    public void consumePlant(int eatingAnimalsCount) {
        energy += Config.getPlantEnergy() / eatingAnimalsCount;
    }

    public int getEnergy() {
        return energy;
    }

    public List<Integer> getGenes() {
        return genotype.genes;
    }
}
