package statistics;

import configs.Config;
import elements.Animal;
import elements.IMapElement;
import elements.MapElementAction;
import simulation.ISimulationObserver;
import simulation.Simulation;

public class StatisticsManager implements ISimulationObserver {
    private int plantsCount = 0;
    private int deadAnimalsCount = 0;
    private int deadAnimalsAgeSum = 0;
    private int livingAnimalsEnergy = 0;
    private int livingAnimalsCount = 0;
    private int totalChildrenCount = -Config.getStartAnimals() * 2;
    private int worldAge = 0;
    private final AverageStatisticsManager averageStatistics;

    public StatisticsManager(Simulation simulation) {
        simulation.addObserver(this);
        this.averageStatistics = new AverageStatisticsManager(5);
    }

    @Override
    public void handleDayFinished() {
        worldAge += 1;
        averageStatistics.handleDay(
                getPlantsCount(),
                getAnimalsCount(),
                getAverageDeadAnimalsAge(),
                getAverageLivingAnimalsEnergy(),
                getAverageChildrenCount()
        );
    }

    @Override
    public void handleElementChange(IMapElement eventTarget, MapElementAction context, Object oldValue) {
        switch (context) {
            case ANIMAL_BORN -> {
                Animal born = (Animal) eventTarget;
                livingAnimalsEnergy += born.getEnergy();
                livingAnimalsCount++;
                totalChildrenCount += 2;
            }
            case PLANT_ADDED -> plantsCount++;
            case ANIMAL_DIED -> {
                Animal dead = (Animal) eventTarget;
                livingAnimalsCount--;
                deadAnimalsCount++;
                livingAnimalsEnergy -= dead.getEnergy();
                deadAnimalsAgeSum += dead.getAge();
                totalChildrenCount -= dead.getParentsCount() + dead.getChildrenCount();
            }
            case PLANT_EATEN -> plantsCount--;
            case ENERGY_LEVEL_CHANGED -> {
                int currentEnergy = ((Animal) eventTarget).getEnergy();
                int oldEnergy = (int) oldValue;
                livingAnimalsEnergy += currentEnergy - oldEnergy;
            }
        }
    }

    public int getPlantsCount() {
        return plantsCount;
    }

    public int getAnimalsCount() {
        return livingAnimalsCount;
    }

    public int getAverageDeadAnimalsAge() {
        if (deadAnimalsCount == 0) {
            return 0;
        }
        return deadAnimalsAgeSum / deadAnimalsCount;
    }

    public int getAverageLivingAnimalsEnergy() {
        if (livingAnimalsCount == 0) {
            return 0;
        }
        return livingAnimalsEnergy / livingAnimalsCount;
    }

    public double getAverageChildrenCount() {
        if (livingAnimalsCount == 0) {
            return 0;
        }
        return Math.round(totalChildrenCount * 100.0 / livingAnimalsCount) / 100.0;
    }

    public int getWorldAge() {
        return worldAge;
    }

    public AverageStatisticsManager getAverageStatistics() {
        return averageStatistics;
    }
}
