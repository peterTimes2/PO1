package configs;

import coordinates.Vector2d;

public class Config {
    private static int height = 0;
    private static int width = 0;
    private static int startEnergy = 0;
    private static int plantEnergy = 0;
    private static int moveEnergy = 0;
    private static double jungleRatio = 0;
    private static Vector2d jungleLowerLeft = new Vector2d(0, 0);
    private static Vector2d jungleUpperRight = new Vector2d(0, 0);

    public static void loadConfig(
          int height, int width, int startEnergy,
          int plantEnergy,int moveEnergy, double jungleRatio
    ) {
        int jungleXOffset = (int) (width * (1 - jungleRatio) / 2);
        int jungleYOffset = (int) (height * (1 - jungleRatio) / 2);
        Config.height = height;
        Config.width = width;
        Config.startEnergy = startEnergy;
        Config.plantEnergy = plantEnergy;
        Config.moveEnergy = moveEnergy;
        Config.jungleRatio = jungleRatio;
        Config.jungleLowerLeft = new Vector2d(jungleXOffset, jungleYOffset);
        Config.jungleUpperRight = new Vector2d(width - jungleXOffset - 1, height - jungleYOffset - 1);
    };

    public static int getHeight() {
        return height;
    }

    public static int getWidth() {
        return width;
    }

    public static int getStartEnergy() {
        return startEnergy;
    }

    public static int getPlantEnergy() {
        return plantEnergy;
    }

    public static int getMoveEnergy() {
        return moveEnergy;
    }

    public static Vector2d getJungleLowerLeft() {
        return jungleLowerLeft;
    }

    public static Vector2d getJungleUpperRight() {
        return jungleUpperRight;
    }

    public static double getJungleRatio() {
        return jungleRatio;
    }
}
