package elements;
import configs.Config;
import coordinates.MapDirection;
import coordinates.Vector2d;
import map.WorldMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class Animal extends AbstractMapElement {
    private final List<Animal> children;
    private final Set<Animal> successors;
    private int energy;
    private int age;
    private boolean isAlive;
    private MapDirection orientation;
    private final Genotype genotype;
    private Animal mom;
    private Animal dad;

    public Animal(Vector2d position, WorldMap map) {
        super(position, map);
        this.energy = Config.getStartEnergy();
        this.orientation = MapDirection.NORTH;
        this.genotype = new Genotype();
        this.mom = null;
        this.dad = null;
        this.children = new LinkedList<>();
        this.successors = new HashSet<>();
        this.isAlive = true;
        this.age = 0;
        map.placeAnimal(this);
        notifyObservers(MapElementAction.ANIMAL_BORN, null);
    }

    public Animal(Animal mom, Animal dad, Vector2d position) {
        super(position, mom.map);
        this.genotype = new Genotype(mom.genotype, dad.genotype);
        this.energy = (mom.energy + dad.energy) / 4;
        this.orientation = MapDirection.randomDirection();
        this.mom = mom;
        this.dad = dad;
        this.children = new LinkedList<>();
        this.successors = new HashSet<>();
        this.isAlive = true;
        this.age = 0;
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
        setEnergy(energy * 3 / 4);
        partner.setEnergy(partner.energy * 3 / 4);
        List<Vector2d> emptyNeighbours = map.getEmptyNeighbours(getPosition());
        Vector2d childPosition = getPosition().add(MapDirection.randomDirection().toUnitVector());
        if (!emptyNeighbours.isEmpty()) {
            childPosition = emptyNeighbours.get((int)(Math.random() * emptyNeighbours.size()));
        }
        Animal born = new Animal(this, partner, childPosition);
        addSuccessor(born);
        partner.addSuccessor(born);
        children.add(born);
        return born;
    }

    public void turnAndMove() {
        if (energy < Config.getMoveEnergy()) {
            die();
            return;
        }
        age += 1;
        orientation = orientation.add(genotype.getRandomDirection());
        Vector2d newPosition = getPosition().add(orientation.toUnitVector());
        newPosition = newPosition.fitToRectangle(map.getLowerLeft(), map.getUpperRight());
        setPosition(newPosition);
        setEnergy(energy - Config.getMoveEnergy());
    }

    public void consumePlant(int eatingAnimalsCount) {
        setEnergy(energy + Config.getPlantEnergy() / eatingAnimalsCount);
    }


    private void addSuccessor(Animal successor) {
        successors.add(successor);
        if (mom != null ) {
            mom.addSuccessor(successor);
        }
        if (dad != null) {
            dad.addSuccessor(successor);
        }
    }

    private void die() {
        if (isAlive) {
            notifyObservers(MapElementAction.ANIMAL_DIED, null);
            if (mom != null) {
                mom.removeSuccessor(this);
            }
            if (dad != null) {
                dad.removeSuccessor(this);
            }
        }
        this.isAlive = false;
        if (this.mom == null && this.dad == null) {
            for (Animal child: children) {
                if (child.dad == this) {
                    child.dad = null;
                }
                if (child.mom == this) {
                    child.mom = null;
                }
                if (!child.isAlive) {
                    child.die();
                }
            }
        }
    }

    private void removeSuccessor(Animal successor) {
        successors.remove(successor);
        if (mom != null) {
            mom.removeSuccessor(successor);
        }
        if (dad != null) {
            dad.removeSuccessor(successor);
        }
    }

    public int getEnergy() {
        return energy;
    }

    public List<Integer> getGenes() {
        return genotype.genes;
    }

    public int getSuccessorsCount() {
        return successors.size();
    }

    public int getChildrenCount() {
        return (int) this.children.stream().filter(a -> a.isAlive).count();
    }

    public int getAge() {
        return age;
    }

    public int getMostFrequentGene() {
        return genotype.getMostFrequentGene();
    }

    private void setEnergy(int energy) {
        int oldEnergy = this.energy;
        this.energy = energy;
        notifyObservers(MapElementAction.ENERGY_LEVEL_CHANGED, oldEnergy);
    }

    public int getParentsCount() {
        int result = 0;
        if (dad != null && dad.isAlive) {
            result++;
        }
        if (mom != null && mom.isAlive) {
            result++;
        }
        return result;
    }
}
