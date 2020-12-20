package visualization;

import configs.Config;
import coordinates.Vector2d;
import elements.Animal;
import elements.IMapElement;
import elements.MapElementAction;
import elements.Plant;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import simulation.ISimulationObserver;
import utils.ImagesManager;

import java.util.*;

public class MapVisualizer implements ISimulationObserver {
    private static final String earthTexturePath = "earth-32.jpg";
    private static final String grassTexturePath = "grass-32.jpg";
    private static final String jungleTexturePath = "jungle-32.jpg";
    private static final String animalLowestTexturePath = "animal-dying-32.jpg";
    private static final String animalLowTexturePath = "animal-low-32.jpg";
    private static final String animalNormalTexturePath = "animal-normal-32.jpg";
    private static final String animalHighTexturePath = "animal-fat-32.jpg";
    private static final String animalFullTexturePath = "animal-full-32.jpg";
    private static final String animalTrackedTexturePath = "animal-tracked-32.jpg";
    private final Vector2d jungleLowerLeft;
    private final Vector2d jungleUpperRight;
    private final GridPane mapVisualization;
    private final VBox trackedAnimalPanel;
    private final Map<Vector2d, FieldType> currentFields;
    private final Map<Vector2d, ImageView> currentTextures;
    private final Map<Vector2d, FieldType> fieldsToUpdate;
    private final Map<Vector2d, Set<Animal>> animals;
    private final Map<Vector2d, Plant> plants;
    private Optional<Animal> trackedAnimal;

    public MapVisualizer() {
        this.mapVisualization = new GridPane();
        this.trackedAnimalPanel = new VBox();
        this.currentTextures = new HashMap<>();
        this.fieldsToUpdate = new HashMap<>();
        this.currentFields = new HashMap<>();
        this.animals = new HashMap<>();
        this.plants = new HashMap<>();
        this.jungleLowerLeft = Config.getJungleLowerLeft();
        this.jungleUpperRight = Config.getJungleUpperRight();
        this.trackedAnimal = Optional.empty();
        initMap(Config.getWidth(), Config.getHeight(), mapVisualization);
    }

    private void initMap(int width, int height, GridPane mapGrid) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Vector2d pos = new Vector2d(x, y);
                ImageView image;
                if (isInJungle(pos)) {
                    image = getTexture(FieldType.JUNGLE);
                } else {
                    image = getTexture(FieldType.STEPPE);
                }
                currentTextures.put(pos, image);
                attachClickEventHandler(image, pos);
                mapGrid.add(image, x, y);
            }
        }
        mapGrid.setAlignment(Pos.CENTER);
    }

    @Override
    public void handleDayFinished() {
        Map<Vector2d, FieldType> copyFieldsToUpdate = new HashMap<>(fieldsToUpdate);
        Platform.runLater(() -> {
            for (Vector2d field: copyFieldsToUpdate.keySet()) {
                ImageView texture = getTexture(copyFieldsToUpdate.get(field));
                updateTrackedAnimalPanel();
                updateMapGrid(field, texture);
            }
        });
        fieldsToUpdate.clear();
    }


    @Override
    public void handleElementChange(IMapElement eventTarget, MapElementAction context, Object oldValue) {
        switch (context) {
            case ANIMAL_BORN -> {
                Animal born = (Animal) eventTarget;
                Vector2d field = born.getPosition();
                animals.putIfAbsent(field, new HashSet<>());
                animals.get(field).add(born);
                updateFieldIfNeeded(field);
            }
            case PLANT_ADDED -> {
                Plant added = (Plant) eventTarget;
                Vector2d field = added.getPosition();
                plants.put(field, added);
                updateFieldIfNeeded(field);
            }
            case POSITION_CHANGED -> {
                Animal animal = (Animal) eventTarget;
                Vector2d oldPosition = (Vector2d) oldValue;
                animals.get(oldPosition).remove(animal);
                animals.putIfAbsent(animal.getPosition(), new HashSet<>());
                animals.get(animal.getPosition()).add(animal);
                updateFieldIfNeeded(oldPosition);
                updateFieldIfNeeded(animal.getPosition());
            }
            case ANIMAL_DIED -> {
                Animal deceased = (Animal) eventTarget;
                Vector2d field = deceased.getPosition();
                this.animals.get(field).remove(deceased);
                updateFieldIfNeeded(field);
            }
            case PLANT_EATEN -> {
                Plant eaten = (Plant) eventTarget;
                Vector2d field = eaten.getPosition();
                plants.remove(field);
                updateFieldIfNeeded(field);
            }
        }
    }

    private ImageView getTexture(FieldType fieldType) {
        return switch (fieldType) {
            case PLANT -> ImagesManager.getView(grassTexturePath);
            case JUNGLE -> ImagesManager.getView(jungleTexturePath);
            case STEPPE -> ImagesManager.getView(earthTexturePath);
            case ANIMAL_LOWEST_ENERGY -> ImagesManager.getView(animalLowestTexturePath);
            case ANIMAL_LOW_ENERGY -> ImagesManager.getView(animalLowTexturePath);
            case ANIMAL_MID_ENERGY -> ImagesManager.getView(animalNormalTexturePath);
            case ANIMAL_HIGH_ENERGY -> ImagesManager.getView(animalHighTexturePath);
            case ANIMAL_ENERGY_OVER_9000 -> ImagesManager.getView(animalFullTexturePath);
            case ANIMAL_TRACKED -> ImagesManager.getView(animalTrackedTexturePath);
        };
    }

    private void updateFieldIfNeeded(Vector2d field) {
        FieldType current = currentFields.get(field);
        FieldType updated = isInJungle(field) ? FieldType.JUNGLE : FieldType.STEPPE;
        if (plants.get(field) != null) {
            updated = FieldType.PLANT;
        }
        animals.putIfAbsent(field, new HashSet<>());
        if (!animals.get(field).isEmpty()) {
            updated = getAnimalFieldType(field);
        }
        if (current != updated) {
            fieldsToUpdate.put(field, updated);
            currentFields.put(field, updated);
        }
    }

    private FieldType getAnimalFieldType(Vector2d field) {
        if (trackedAnimal.isPresent() && animals.get(field).contains(trackedAnimal.get())) {
            return FieldType.ANIMAL_TRACKED;
        }
        Animal highestEnergyAnimal = animals.get(field)
                .stream()
                .max(Comparator.comparing(Animal::getEnergy))
                .get();
        int energy = highestEnergyAnimal.getEnergy();
        if (energy < Config.getLowEnergyRatio() * Config.getStartEnergy()) {
            return FieldType.ANIMAL_LOWEST_ENERGY;
        } else if (energy < Config.getNormalEnergyRatio() * Config.getStartEnergy()) {
            return FieldType.ANIMAL_LOW_ENERGY;
        } else if (energy < Config.getHighEnergyRatio() * Config.getStartEnergy()) {
            return FieldType.ANIMAL_MID_ENERGY;
        } else if (energy < Config.getFullEnergyRatio() * Config.getStartEnergy()) {
            return FieldType.ANIMAL_HIGH_ENERGY;
        } else {
            return FieldType.ANIMAL_ENERGY_OVER_9000;
        }
    }

    private boolean isInJungle(Vector2d pos) {
        return jungleLowerLeft.precedes(pos) && jungleUpperRight.follows(pos);
    }

    public Parent getMapVisualization() {
        return new HBox(mapVisualization, trackedAnimalPanel);
    }

    private void updateTrackedAnimalPanel() {
        trackedAnimalPanel.getChildren().clear();
        if (trackedAnimal.isPresent()) {
            Animal animal = trackedAnimal.get();
            trackedAnimalPanel.getChildren().add(new Label("position: " + animal.getPosition()));
            trackedAnimalPanel.getChildren().add(new Label("energy: " + animal.getEnergy()));
            trackedAnimalPanel.getChildren().add(new Label("genes: " + animal.getGenes()));
        }
    }

    private void updateTrackedAnimal(Optional<Animal> tracked) {
        Optional<Animal> previous = trackedAnimal;
        trackedAnimal = tracked;
        if (previous.isPresent()) {
            Vector2d previouslyTrackedField = previous.get().getPosition();
            updateFieldIfNeeded(previouslyTrackedField);
            updateMapGrid(previouslyTrackedField, getTexture(fieldsToUpdate.get(previouslyTrackedField)));
        }
        if (tracked.isPresent()) {
            updateTrackedAnimalPanel();
            updateMapGrid(tracked.get().getPosition(), getTexture(FieldType.ANIMAL_TRACKED));
            currentFields.put(tracked.get().getPosition(), FieldType.ANIMAL_TRACKED);
        }
    }

    private void updateMapGrid(Vector2d field, ImageView newTexture) {
        mapVisualization.getChildren().remove(currentTextures.get(field));
        mapVisualization.add(newTexture, field.x, field.y);
        attachClickEventHandler(newTexture, field);
        currentTextures.put(field, newTexture);
    }

    private void attachClickEventHandler(ImageView texture, Vector2d field) {
        texture.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            animals.putIfAbsent(field, new HashSet<>());
            updateTrackedAnimal(animals.get(field).stream().max(Comparator.comparing(Animal::getEnergy)));
            event.consume();
        });
    }
}
