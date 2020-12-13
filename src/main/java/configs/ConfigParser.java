package configs;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;

public class ConfigParser {
    static private final String[] paramsNames = {
            "height", "width", "jungleRatio",
            "plantEnergy", "moveEnergy", "startEnergy", "startAnimals"
    };

    public static void parse(String filePath) {
        JSONParser jsonParser = new JSONParser();
        try (FileReader paramsFile = new FileReader(filePath)) {
            JSONObject params = (JSONObject) jsonParser.parse(paramsFile);
            for (String param: paramsNames) {
                if (params.get(param) == null) {
                    throw new InvalidParameterException("Parameter " + param + " not found in " + filePath);
                }
            }
            Config.loadConfig(
                    (int)(long) params.get("height"),
                    (int)(long) params.get("width"),
                    (int)(long) params.get("startEnergy"),
                    (int)(long) params.get("plantEnergy"),
                    (int)(long) params.get("moveEnergy"),
                    (double) params.get("jungleRatio"),
                    (int)(long) params.get("startAnimals")
            );

        } catch (FileNotFoundException e) {
            System.out.println(filePath + " file not found");
            System.exit(0);
        } catch (IOException | ParseException | InvalidParameterException | ClassCastException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
}
