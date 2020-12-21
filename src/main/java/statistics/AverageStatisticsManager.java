package statistics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class AverageStatisticsManager {
    private int averagePlantsCountSum = 0;
    private int averageAnimalsCountSum = 0;
    private int averageDeadAnimalsAgeSum = 0;
    private int averageLivingAnimalsEnergySum = 0;
    private int averageChildrenCountSum = 0;
    private int worldAge = 0;
    private int dayToWriteStatistics;

    public AverageStatisticsManager(int dayToWriteStatistics) {
        this.dayToWriteStatistics = dayToWriteStatistics;
    }

    public void handleDay(
            int plantsCount,
            int animalsCount,
            int deadAnimalsAge,
            int animalsEnergy,
            double averageChildren
    ) {
        averagePlantsCountSum += plantsCount;
        averageAnimalsCountSum += animalsCount;
        averageDeadAnimalsAgeSum += deadAnimalsAge;
        averageLivingAnimalsEnergySum += animalsEnergy;
        averageChildrenCountSum += averageChildren;
        if (worldAge == dayToWriteStatistics) {
            writeToFile();
        }
        worldAge++;
    }

    public void writeToFile() {
        try {
            File averageStatisticsFile = new File("average_statistics.json");
            if (averageStatisticsFile.createNewFile()) {
                System.out.println("File created: " + averageStatisticsFile.getName());
            } else {
                System.out.println("File already exists.");
            }
            averageStatisticsFile.setWritable(true);
            FileWriter writer = new FileWriter(averageStatisticsFile);
            writer.write("{" + System.lineSeparator());
            writer.append("\t\"averagePlantsCount\": ")
                .append(String.valueOf(averagePlantsCountSum / worldAge))
                .append(",")
                .append(System.lineSeparator());
            writer.append("\t\"averageAnimalsCount\": ")
                .append(String.valueOf(averageAnimalsCountSum / worldAge))
                .append(",")
                .append(System.lineSeparator());
            writer.append("\t\"averageDeadAnimalsAge\": ")
                 .append(String.valueOf(averageDeadAnimalsAgeSum / worldAge))
                 .append(",")
                 .append(System.lineSeparator());
            writer.append("\t\"averageLivingAnimalsEnergy\": ")
                 .append(String.valueOf(averageLivingAnimalsEnergySum / worldAge))
                 .append(",")
                 .append(System.lineSeparator());
            writer.append("\t\"averageChildrenCount\": ")
                 .append(String.valueOf(averageChildrenCountSum / worldAge))
                 .append(",")
                 .append(System.lineSeparator());
            writer.append("}")
                 .append(System.lineSeparator());
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
}
