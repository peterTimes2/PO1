import configs.ConfigParser;
import javafx.application.Application;
import javafx.stage.Stage;
import simulation.Simulation;
import statistics.StatisticsManager;
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
        StatisticsManager statistics = new StatisticsManager(world, 50);
        Visualization visualization = new Visualization(stage, world, statistics);
        world.startSimulation();
        visualization.render();

        Simulation world2 = new Simulation();
        StatisticsManager statistics2 = new StatisticsManager(world2);
        Visualization visualization2 = new Visualization(new Stage(), world2, statistics2);
        world2.startSimulation();
        visualization2.render();
    }

}