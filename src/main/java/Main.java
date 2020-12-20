import configs.ConfigParser;
import javafx.application.Application;
import javafx.stage.Stage;
import simulation.Simulation;
import visualization.Visualization;

public class Main extends Application {
    public static void main(String[] args) {
        System.out.println("hello world");
        ConfigParser.parse("parameters.json");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Simulation world = new Simulation();
        Visualization visualization = new Visualization(
                stage,
                world
        );
        world.startSimulation();
        visualization.render();
    }
}