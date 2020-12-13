package utils;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.HashMap;
import java.util.Map;


public class ImagesManager {
    public static Map<String, Image> imagesCache = new HashMap<>();
    public static ImageView getView(String url) {
        Image image = imagesCache.get(url);
        ImageView view;
        if (image == null) {
            image = new Image(url);
        }
        view = new ImageView(image);
        view.setFitHeight(20);
        view.setFitWidth(20);
        return view;
    }
}
