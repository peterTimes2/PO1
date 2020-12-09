package map;

import configs.Config;
import coordinates.MapDirection;
import coordinates.Vector2d;
import elements.*;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

public class WorldMap implements IMapElementObserver {
    private final Vector2d lowerLeft;
    private final Vector2d upperRight;
    private final Board board;
    private final Map<Vector2d, SortedSet<Animal>> animals;
    private final Map<Vector2d, Plant> plants;
    private Set<Vector2d> eatingAnimalsFields;
    private Set<Vector2d> reproductionFields;

    public WorldMap() {
        Vector2d jungleLowerLeft = Config.getJungleLowerLeft();
        Vector2d jungleUpperRight = Config.getJungleUpperRight();
        this.lowerLeft = new Vector2d(0, 0);
        this.upperRight = new Vector2d(Config.getWidth() - 1, Config.getHeight() - 1);
        this.board = new Board(lowerLeft, upperRight, jungleLowerLeft, jungleUpperRight);
        this.animals = new HashMap<>();
        this.plants = new HashMap<>();
        this.eatingAnimalsFields = new HashSet<>();
        this.reproductionFields = new HashSet<>();
        populateWorld();
    }

    public Vector2d getLowerLeft() {
        return lowerLeft;
    }

    public Vector2d getUpperRight() {
        return upperRight;
    }

    @Override
    public void handleElementChange(IMapElement eventTarget, MapElementAction context, Object oldValue) {
        switch (context) {
            case POSITION_CHANGED: {
                Animal animal = (Animal) eventTarget;
                System.out.println("new position " + animal.getPosition() + " old position: " + oldValue);
                if (board.getElements(animal.getPosition()).stream().anyMatch(IMapElement::isConsumable)) {
                    eatingAnimalsFields.add(animal.getPosition());
                }
                if (board.getElements(animal.getPosition()).stream().anyMatch(IMapElement::isReproducible)) {
                    reproductionFields.add(animal.getPosition());
                }
                animals.get(oldValue).remove(animal);
                animals.putIfAbsent(animal.getPosition(), new TreeSet<>(Comparator.comparing(Animal::getEnergy))); //TODO REVERSE SORTING
                animals.get(animal.getPosition()).add(animal);
                board.removeElement((Vector2d) oldValue, animal);
                board.putOnBoard(animal);
                return;
            }
            case ANIMAL_DIED: {
                System.out.println("animal died");
                Animal deceased = (Animal) eventTarget;
                this.board.removeElement(eventTarget.getPosition(), eventTarget);
                this.animals.get(deceased.getPosition()).remove(deceased);
                return;
            }
        }
    }

    public void morningRoutine() {
        reproductionFields = new HashSet<>();
        eatingAnimalsFields = new HashSet<>();
    }

    public void feedAnimals() {
        for (Vector2d field: eatingAnimalsFields) {
            List<Animal> maxEnergyAnimals = animals.get(field)
                 .stream()
                 .collect(groupingBy(Animal::getEnergy, TreeMap::new, toList()))
                 .lastEntry()
                 .getValue();

            for (Animal animal: maxEnergyAnimals) {
                animal.consumePlant(maxEnergyAnimals.size());
            }

            Plant eaten = this.plants.get(field);
            board.removeElement(eaten.getPosition(), eaten);
            this.plants.remove(field);
        }
    }

    public void turnAndMoveAnimals() {
        List<Animal> movingAnimals = new LinkedList<>();
        for (SortedSet<Animal> animalSet: animals.values()) {
            movingAnimals.addAll(animalSet);
        }
        for (Animal animal: movingAnimals) {
            System.out.println("energy: " + animal.getEnergy() + " position: "+ animal.getPosition());
            animal.turnAndMove();
        }
        System.out.println("animals count: " + movingAnimals.size());
    }

    public void breedAnimals() {
        for(Vector2d field: reproductionFields) {
            List<Animal> parents = new LinkedList<>();
            int lastParentEnergy = this.animals.get(field).first().getEnergy();
            for (Animal animal: this.animals.get(field)) {
                if (animal.getEnergy() == lastParentEnergy || parents.size() < 2) {
                    parents.add(animal);
                }
            }

            int randomPositionA = (int) (Math.random() * parents.size());
            int randomPositionB = (int) (Math.random() * parents.size());
            while (randomPositionB == randomPositionA) {
                randomPositionB = (int) (Math.random() * parents.size());
            }
            Animal mom = parents.get((int) (Math.random() * parents.size()));
            Animal dad = parents.get(randomPositionB);
            if (parents.get(0).getEnergy() > parents.get(1).getEnergy()) {
                mom = parents.get(0);
                dad = parents.get((int) (Math.random() * (parents.size() - 1)) + 1);
            }
            if (mom.getEnergy() > Config.getStartEnergy() / 2 && dad.getEnergy() > Config.getStartEnergy() / 2) {
                dad.growFamily(mom);
            }
        }
    }

    public void addPlants() {
        Optional<Vector2d> jungleEmptyField = board.getRandomEmptyJungleField();
        Optional<Vector2d> steppeEmptyField = board.getRandomEmptySteppeField();
        if (steppeEmptyField.isPresent()) {
            Plant created = new Plant(steppeEmptyField.get(), this);
            plants.put(created.getPosition(), created);
            board.putOnBoard(created);
        }
        if (jungleEmptyField.isPresent()) {
            Plant created = new Plant(jungleEmptyField.get(), this);
            plants.put(created.getPosition(), created);
            board.putOnBoard(created);
        }
    }

    public void placeAnimal(Animal animal) {
        board.putOnBoard(animal);
        animals.putIfAbsent(animal.getPosition(), new TreeSet<>(Comparator.comparing(Animal::getEnergy))); //TODO REVERSE SORTING
        animals.get(animal.getPosition()).add(animal);
        animal.addObserver(this);
    }

    private void populateWorld() {
        Optional<Vector2d> randomSteppePosition = board.getRandomEmptySteppeField();
        Optional<Vector2d> randomJunglePosition = board.getRandomEmptyJungleField();
        if (randomSteppePosition.isPresent() && randomJunglePosition.isPresent()) {
            animals.put(randomSteppePosition.get(), new TreeSet<>(Comparator.comparing(Animal::getEnergy))); //TODO REVERSE SORTING
            animals.put(randomJunglePosition.get(), new TreeSet<>(Comparator.comparing(Animal::getEnergy))); //TODO REVERSE SORTING
            Animal adam = new Animal(randomSteppePosition.get(),this);
            System.out.println("Adam position: " + adam.getPosition());
            Animal eva = new Animal(randomJunglePosition.get(),this);
            System.out.println("Eva position: " + eva.getPosition());
            animals.get(randomSteppePosition.get()).add(adam);
            animals.get(randomJunglePosition.get()).add(eva);
            board.putOnBoard(adam);
            board.putOnBoard(eva);
            adam.addObserver(this);
            eva.addObserver(this);
        }

    }

    public boolean isOccupied(Vector2d position) {
        return !objectsAt(position).isEmpty();
    }

    public Set<? extends IMapElement> objectsAt(Vector2d position) {
        return board.getElements(position);
    }

    public List<Vector2d> getEmptyNeighbours(Vector2d position) {
        List<Vector2d> result = new LinkedList<>();
        for (int i = 0; i < 8; i++) {
            Vector2d neighbour = position.add(MapDirection.fromInt(i).toUnitVector());
            neighbour = neighbour.fitToRectangle(lowerLeft, upperRight);
            if (!isOccupied(neighbour)) {
                result.add(neighbour);
            }
        }
        return result;
    }
}
