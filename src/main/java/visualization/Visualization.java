package visualization;

import coordinates.Vector2d;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import simulation.Simulation;


public class Visualization {
    private final MapVisualizer mapVisualizer;
    private final Stage window;
    private final Simulation simulation;
    public Visualization(
        Stage window,
        Simulation simulation,
        int mapWidth,
        int mapHeight,
        Vector2d jungleLowerLeft,
        Vector2d jungleUpperRight
    ) {
        this.mapVisualizer = new MapVisualizer(mapWidth, mapHeight, jungleLowerLeft, jungleUpperRight);
        this.window = window;
        this.simulation = simulation;
        simulation.addObserver(mapVisualizer);
    }

    public void render() {
        window.setTitle("Simulation");
        Parent root = new VBox(mapVisualizer.getMapVisualization(), getToggleButton());
        Scene scene = new Scene(root, 1500, 800);
        window.setScene(scene);
        window.show();
    }

    private Button getToggleButton() {
        Button button = new Button();
        button.setOnAction(e -> {
            System.out.println("button clicked");
            button.setText(simulation.getIsRunning() ? "Resume simulation" : "Stop simulation");
            toggleSimulation();
        });
        button.setText(simulation.getIsRunning() ? "Stop simulation" : "Resume simulation");
        return button;
    }

    private void toggleSimulation() {
        if (simulation.getIsRunning()) {
            simulation.stopSimulation();
        } else {
            simulation.resumeSimulation();
        }
    }
}
