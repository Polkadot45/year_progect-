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
        loadImage("room", "resources/room.jpg");

        // Персонажи
        loadImage("knurov","resources/knurov.png");
        loadImage("serega","resources/serega.png");
        loadImage("ivan", "resources/ivan.png");
        loadImage("larusaS", "resources/larusaS.png");
        loadImage("paratov", "resources/paratov.png");
        loadImage("mama", "resources/mama.png");
        loadImage("gavrilo","resources/gavrilo.png");
        loadImage("karandashev","resources/karandashev.png");

        // Реплики
        loadImage("replica_s", "resources/replica_s.png");
        loadImage("replica_d", "resources/replica_d.png");
        loadImage("chois1", "resources/сhois1.png");
        loadImage("chois2", "resources/сhois2.png");
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