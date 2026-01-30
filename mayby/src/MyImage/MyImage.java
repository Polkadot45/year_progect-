package MyImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyImage {
    private final Map<String, BufferedImage> images = new HashMap<>();

    public void loadAllImages() throws IOException {
        // Фон/меню
        loadImage("phon", "resources/phon.jpg");

        // Кнопки
        loadImage("start", "resources/start.png");
        loadImage("set", "resources/set.png");
        loadImage("exit", "resources/exit.png");
        loadImage("stop", "resources/stop.png");
        loadImage("next", "resources/next.png");
        loadImage("razvilka1", "resources/razvilka1.png");
        loadImage("razvilka2", "resources/razvilka2.png");

        // Фоны сцен
        loadImage("people", "resources/people.jpg");
        loadImage("seti", "resources/seti.jpg");
        loadImage("cafe", "resources/cafe.jpg");
        loadImage("dot", "resources/dot.png");

        // Персонажи
        loadImage("serega","resources/serega.png");
        loadImage("ivan", "resources/ivan.png");
        loadImage("larusaS", "resources/larusaS.png");
        loadImage("paratov", "resources/paratov.png");
        loadImage("mama", "resources/mama.png");

        // Реплики
        loadImage("r1_1", "resources/r1_1.png");
        loadImage("r1_2", "resources/r1_2.png");
    }

    private void loadImage(String key, String path) throws IOException {
        java.net.URL url = getClass().getClassLoader().getResource(path);
        if (url == null) {
            throw new IOException("Изображение не найдено: " + path);
        }
        images.put(key, ImageIO.read(url));
    }

    public BufferedImage getImage(String key) {
        return images.get(key);
    }
}