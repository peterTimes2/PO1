import configs.ConfigParser;
import simulation.Simulation;

public class Main {
    public static void main(String[] args) {
        System.out.println("hello world");
        ConfigParser.parse("parameters.json");

        Simulation world = new Simulation();
        for (int i = 0; i < 250; i++) {
            world.makeMove();
        }
    }
}