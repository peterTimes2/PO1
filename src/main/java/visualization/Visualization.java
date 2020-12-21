package visualization;

import elements.IMapElement;
import elements.MapElementAction;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simulation.ISimulationObserver;
import simulation.Simulation;
import statistics.StatisticsManager;


public class Visualization implements ISimulationObserver {
    private final MapVisualizer mapVisualizer;
    private final Stage window;
    private final Simulation simulation;
    private final StatisticsManager statistics;
    private final Parent statisticsPanel;
    public Visualization(
        Stage window,
        Simulation simulation,
        StatisticsManager statistics
    ) {
        this.mapVisualizer = new MapVisualizer();
        this.window = window;
        this.simulation = simulation;
        this.statistics = statistics;
        this.statisticsPanel = new VBox();
        simulation.addObserver(mapVisualizer);
        simulation.addObserver(this);
    }

    public void render() {
        window.setTitle("Simulation");
        Parent root = new VBox(mapVisualizer.getMapVisualization(), renderToggleButton(), statisticsPanel);
        Scene scene = new Scene(root, 1500, 800);
        window.setScene(scene);
        window.show();
    }

    private Button renderToggleButton() {
        Button button = new Button();
        button.setOnAction(e -> {
            button.setText(simulation.getIsRunning() ? "Resume simulation" : "Stop simulation");
            toggleSimulation();
        });
        button.setText(simulation.getIsRunning() ? "Stop simulation" : "Resume simulation");
        return button;
    }

    private void renderStatisticsPanel() {
        VBox statisticsRoot = (VBox) statisticsPanel;
        statisticsRoot.getChildren().clear();
        statisticsRoot.getChildren().add(new Label("day: " + statistics.getWorldAge()));
        statisticsRoot.getChildren().add(new Label("animals population: " + statistics.getAnimalsCount()));
        statisticsRoot.getChildren().add(new Label("plants population: " + statistics.getPlantsCount()));
        statisticsRoot.getChildren().add(new Label("average children: " + statistics.getAverageChildrenCount()));
        statisticsRoot.getChildren().add(new Label("average death age: " + statistics.getAverageDeadAnimalsAge()));
        statisticsRoot.getChildren().add(new Label("average energy: " + statistics.getAverageLivingAnimalsEnergy()));
    }

    private void toggleSimulation() {
        if (simulation.getIsRunning()) {
            simulation.stopSimulation();
        } else {
            simulation.resumeSimulation();
        }
    }

    @Override
    public void handleDayFinished() {
        Platform.runLater(() -> {
            renderStatisticsPanel();
        });
    }

    @Override
    public void handleElementChange(IMapElement eventTarget, MapElementAction context, Object oldValue) {

    }
}
