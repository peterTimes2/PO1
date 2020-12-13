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
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        javafx.scene.control.Label l = new javafx.scene.control.Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        MapVisualizer visualization = new MapVisualizer(Config.getWidth(), Config.getHeight(), Config.getJungleLowerLeft(), Config.getJungleUpperRight());
        GridPane map  = visualization.getMapVisualization();
        Simulation world = new Simulation();
        world.addObserver(visualization);
        Thread simulationTread = new Thread(() -> {
            for (int i = 0; i < 150; i++) {
                try {
                    Thread.sleep(300);
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