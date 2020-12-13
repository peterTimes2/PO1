package map;

import configs.Config;
import coordinates.MapDirection;
import coordinates.Vector2d;
import elements.*;
import simulation.ISimulationObserver;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WorldMap implements IMapElementObserver {
    private final Vector2d lowerLeft;
    private final Vector2d upperRight;
    private final Board board;
    private final Map<Vector2d, Set<Animal>> animals;
    private final Map<Vector2d, Plant> plants;
    private final Set<Vector2d> eatingAnimalsFields;
    private final Set<Vector2d> reproductionFields;
    private final List<ISimulationObserver> observers;
    private int day = 0;

    public WorldMap(List<ISimulationObserver> observers) {
        Vector2d jungleLowerLeft = Config.getJungleLowerLeft();
        Vector2d jungleUpperRight = Config.getJungleUpperRight();
        this.lowerLeft = new Vector2d(0, 0);
        this.upperRight = new Vector2d(Config.getWidth() - 1, Config.getHeight() - 1);
        this.board = new Board(lowerLeft, upperRight, jungleLowerLeft, jungleUpperRight);
        this.animals = new HashMap<>();
        this.plants = new HashMap<>();
        this.eatingAnimalsFields = new HashSet<>();
        this.reproductionFields = new HashSet<>();
        this.observers = observers;
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
            case POSITION_CHANGED -> {

                Animal animal = (Animal) eventTarget;
                Vector2d oldPosition = (Vector2d) oldValue;
                if (board.getElements(animal.getPosition()).stream().anyMatch(IMapElement::isConsumable)) {
                    eatingAnimalsFields.add(animal.getPosition());
                }
                if (board.getElements(animal.getPosition()).stream().anyMatch(IMapElement::isReproducible)) {
                    reproductionFields.add(animal.getPosition());
                }
                //
                List<Animal> movingAnimals = new LinkedList<>();
                for (Set<Animal> animalSet: animals.values()) {
                    movingAnimals.addAll(animalSet);
                }
                System.out.println("size before: " + movingAnimals.size());
                //
                animals.get(oldPosition).remove(animal);
                animals.putIfAbsent(animal.getPosition(), new HashSet<>());
                animals.get(animal.getPosition()).add(animal);
                //
                List<Animal> movingAnimals2 = new LinkedList<>();
                for (Set<Animal> animalSet: animals.values()) {
                    movingAnimals2.addAll(animalSet);
                }
                System.out.println("size after: " + movingAnimals2.size());
                if(movingAnimals2.size() != movingAnimals.size()) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                //
                board.removeElement(oldPosition, animal);
                board.putOnBoard(animal);
            }
            case ANIMAL_DIED -> {
                System.out.println("animal died");
                Animal deceased = (Animal) eventTarget;
                this.board.removeElement(eventTarget.getPosition(), eventTarget);
                this.animals.get(deceased.getPosition()).remove(deceased);
            }
        }
    }

    public void morningRoutine() {
        if (day == 0) {
            populateWorld();
        }
        day++;
        reproductionFields.clear();
        eatingAnimalsFields.clear();
    }

    public void feedAnimals() {
        for (Vector2d field: eatingAnimalsFields) {
            List<Animal> maxEnergyAnimals = animals.get(field)
                 .stream()
                 .collect(Collectors.groupingBy(Animal::getEnergy, TreeMap::new, Collectors.toList()))
                 .lastEntry()
                 .getValue();

            for (Animal animal: maxEnergyAnimals) {
                animal.consumePlant(maxEnergyAnimals.size());
            }

            Plant eaten = this.plants.get(field);
            eaten.getEaten();
            board.removeElement(eaten.getPosition(), eaten);
            this.plants.remove(field);
        }
    }

    public void turnAndMoveAnimals() {
        List<Animal> movingAnimals = new LinkedList<>();
        for (Set<Animal> animalSet: animals.values()) {
            movingAnimals.addAll(animalSet);
        }
        Set<Animal> set = new HashSet<>(movingAnimals);
        if (set.size() != movingAnimals.size()) {
            System.exit(1);
        }
        System.out.println(board.getRandomEmptyJungleField().isPresent());
        System.out.println(board.getRandomEmptySteppeField().isPresent());
        System.out.println("animals count befgo: " + movingAnimals.size());
        for (Animal animal: movingAnimals) {
            animal.turnAndMove();
        }
        System.out.println("animals count: " + movingAnimals.size());
        System.out.println("day: " + day);
    }

    public void breedAnimals() {
        for(Vector2d field: reproductionFields) {
            List<Animal> parents = new LinkedList<>();
            if (animals.get(field).size() < 2) {
                continue;
            }
            Iterator<Animal> energySortedAnimalsIterator = animals.get(field)
                    .stream()
                    .sorted(Comparator.comparing(Animal::getEnergy).reversed())
                    .iterator();
            int lastParentEnergy = 0;
            while (energySortedAnimalsIterator.hasNext()) {
                Animal animal = energySortedAnimalsIterator.next();
                if (animal.getEnergy() == lastParentEnergy || parents.size() < 2) {
                    parents.add(animal);
                    lastParentEnergy = animal.getEnergy();
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
        System.out.println("plants count: " + plants.size());
        Optional<Vector2d> jungleEmptyField = board.getRandomEmptyJungleField();
        Optional<Vector2d> steppeEmptyField = board.getRandomEmptySteppeField();
        System.out.println("jungle is present: " + jungleEmptyField.isPresent());
        System.out.println("steppe is present: " + steppeEmptyField.isPresent());
        steppeEmptyField.ifPresent(vector2d -> new Plant(vector2d, this));
        jungleEmptyField.ifPresent(vector2d -> new Plant(vector2d, this));
    }

    public void placeAnimal(Animal animal) {
        board.putOnBoard(animal);
        animals.putIfAbsent(animal.getPosition(), new HashSet<>());
        animals.get(animal.getPosition()).add(animal);
        animal.addObserver(this);
        animal.addAllObservers(observers);
    }

    public void placePlant(Plant plant) {
        plants.put(plant.getPosition(), plant);
        board.putOnBoard(plant);
        plant.addAllObservers(observers);
    }

    private void populateWorld() {
        int animalsCount = 0;
        while(animalsCount < Config.getStartAnimals()) {
            Optional<Vector2d> randomSteppePosition = board.getRandomEmptySteppeField();
            Optional<Vector2d> randomJunglePosition = board.getRandomEmptyJungleField();
            if (randomSteppePosition.isPresent() && randomJunglePosition.isPresent()) {
                Animal a = new Animal((int)(Math.random() * 2) == 1 ? randomJunglePosition.get() : randomSteppePosition.get(),this);
                System.out.println("starting animal pos: " + a.getPosition());
                animalsCount++;
            }
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

    private Stream<IMapElement> getElementsStream() {
        Stream<IMapElement> result = Stream.of();
        for (Set<Animal> animalSet: animals.values()) {
            result = Stream.concat(animalSet.stream(), result);
        }
        return Stream.concat(result, plants.values().stream());
    }

    public void handleAddingObserver(ISimulationObserver observer) {
        getElementsStream().forEach(e -> e.addObserver(observer));
    }

}
