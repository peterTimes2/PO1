import configs.Config;
import configs.ConfigParser;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import simulation.Simulation;
import visualization.MapVisualizer;

public class Main extends Application {
    public static void main(String[] args) {
        System.out.println("hello world");
        ConfigParser.parse("parameters.json");
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Simulation");
        MapVisualizer visualization = new MapVisualizer(
                Config.getWidth(),
                Config.getHeight(),
                Config.getJungleLowerLeft(),
                Config.getJungleUpperRight()
        );
        GridPane map  = visualization.getMapVisualization();
        Simulation world = new Simulation();
        world.addObserver(visualization);
        Thread simulationTread = new Thread(() -> {
            for (int i = 0; i < 100; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                world.makeMove();
            }
        });
        simulationTread.start();
        Scene scene = new Scene(map, 640, 480);
        stage.setScene(scene);
        stage.show();
    }
}