package visualization;

import coordinates.Vector2d;
import elements.Animal;
import elements.IMapElement;
import elements.MapElementAction;
import elements.Plant;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import simulation.ISimulationObserver;
import utils.ImagesManager;

import java.util.*;

public class MapVisualizer implements ISimulationObserver {
    private static final String earthTexturePath = "earth-32.jpg";
    private static final String grassTexturePath = "grass-32.jpg";
    private static final String animalTexturePath = "animal-32.jpg";
    private static final String jungleTexturePath = "jungle-32.jpg";
    private static final String superSaiyanTexturePath = "";
    private final Vector2d jungleLowerLeft;
    private final Vector2d jungleUpperRight;
    private final GridPane mapVisualization;
    private final Map<Vector2d, FieldType> currentFields;
    private final Map<Vector2d, ImageView> currentTextures;
    private final Map<Vector2d, FieldType> fieldsToUpdate;
    private final Map<Vector2d, Set<Animal>> animals;
    private final Map<Vector2d, Plant> plants;

    public MapVisualizer(int mapWidth, int mapHeight, Vector2d jungleLowerLeft, Vector2d jungleUpperRight) {
        this.mapVisualization = new GridPane();
        this.currentTextures = new HashMap<>();
        this.fieldsToUpdate = new HashMap<>();
        this.currentFields = new HashMap<>();
        this.animals = new HashMap<>();
        this.plants = new HashMap<>();
        this.jungleLowerLeft = jungleLowerLeft;
        this.jungleUpperRight = jungleUpperRight;
        initMap(mapWidth, mapHeight, jungleLowerLeft, jungleUpperRight, mapVisualization);
    }

    private void initMap(int width, int height, Vector2d jungleLowerLeft, Vector2d jungleUpperRight, GridPane mapGrid) {
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
                if (currentFields.get(field) == copyFieldsToUpdate.get(field)) {
                    continue;
                }
                ImageView texture = getTexture(copyFieldsToUpdate.get(field));
                mapVisualization.getChildren().remove(currentTextures.get(field));
                mapVisualization.add(texture, field.x, field.y);
                currentTextures.put(field, texture);
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
            case ANIMAL_LOW_ENERGY -> ImagesManager.getView(animalTexturePath);
            case ANIMAL_MID_ENERGY -> ImagesManager.getView(animalTexturePath);
            case ANIMAL_HIGH_ENERGY -> ImagesManager.getView(animalTexturePath);
            case ANIMAL_ENERGY_OVER_9000 -> ImagesManager.getView(superSaiyanTexturePath);
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
            updated = FieldType.ANIMAL_LOW_ENERGY;
        }
        if (current != updated) {
            fieldsToUpdate.put(field, updated);
        }
    }

    private boolean isInJungle(Vector2d pos) {
        return jungleLowerLeft.precedes(pos) && jungleUpperRight.follows(pos);
    }

    public GridPane getMapVisualization() {
        return mapVisualization;
    }
}
