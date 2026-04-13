package Game;
import MyImage.MyImage;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Animations {
    private final Game game;
    private final MyImage imageManager;
    private java.util.List<AnimatedObject> animatedObjects = new java.util.ArrayList<>();
    private Timer animationTimer;
    private boolean animationsEnabled = true;
    private String currentScenarioFile;
    private int currentReplicaIndex = 0;
    private int totalReplicas = 0;

    private final Map<String, List<String>> scenarioAllowedObjects = new HashMap<>();

    public void updateProgress(int currentIndex, int totalCount) {
        this.currentReplicaIndex = currentIndex;
        this.totalReplicas = totalCount;
    }

    public Animations(Game game, MyImage imageManager) {
        this.game = game;
        this.imageManager = imageManager;

        // Инициализация разрешённых объектов
        scenarioAllowedObjects.put("1_1.txt", Arrays.asList("bird"));
        scenarioAllowedObjects.put("1_2.txt", Arrays.asList("bird"));
        scenarioAllowedObjects.put("1_3.txt", Arrays.asList("bird"));
        scenarioAllowedObjects.put("1_03.txt", Arrays.asList("cat"));
        scenarioAllowedObjects.put("1_4.txt", Arrays.asList("cat"));
        scenarioAllowedObjects.put("1_5.txt", Arrays.asList("cat"));

        startTimer();
    }

    public void startTimer() {
        if (animationTimer == null) {
            animationTimer = new Timer(33, e -> updateAnimations());
            animationTimer.start();
        }
    }

    public void stopTimer() {
        if (animationTimer != null) {
            animationTimer.stop();
            animationTimer = null;
        }
    }

    private void updateAnimations() {
        if (!animationsEnabled || game.getCurrentScene() != Game.Scene.STORY || currentScenarioFile == null) {
            return;
        }

        animatedObjects.removeIf(obj -> !obj.update());

        int remaining = totalReplicas - currentReplicaIndex; // Сколько реплик осталось
        boolean shouldSpawnBird = false;
        boolean shouldSpawnCat = false;

        // Птица: только в 1_1.txt, 1_2.txt, 1_3.txt и если осталось ≥10 реплик
        if (("1_1.txt".equals(currentScenarioFile) ||
                "1_2.txt".equals(currentScenarioFile) ||
                "1_3.txt".equals(currentScenarioFile)) && remaining >= 10) {
            shouldSpawnBird = true;
        }

        // Кошка: только в 1_4.txt, 1_5.txt и если осталось ≥13 реплик
        if (("1_4.txt".equals(currentScenarioFile) ||
                "1_5.txt".equals(currentScenarioFile)) && remaining >= 13) {
            shouldSpawnCat = true;
        }

        double spawnChance = 0.0;
        boolean canSpawn = false;

        if (shouldSpawnBird) {
            spawnChance = 0.007;
            canSpawn = animatedObjects.size() < 2;
        } else if (shouldSpawnCat) {
            spawnChance = 0.003;
            canSpawn = animatedObjects.isEmpty();
        }

        if (canSpawn && Math.random() < spawnChance) {
            spawnRandomObject();
        }

        game.repaintPanel();
    }

    private void spawnRandomObject() {
        if (currentScenarioFile == null || !scenarioAllowedObjects.containsKey(currentScenarioFile)) return;
        List<String> types = scenarioAllowedObjects.get(currentScenarioFile);
        if (types.isEmpty()) return;
        String type = types.get((int) (Math.random() * types.size()));
        AnimatedObject obj = createRandomObject(type);
        if (obj != null) animatedObjects.add(obj);
    }

    private AnimatedObject createRandomObject(String type) {
        if ("bird".equals(type)) {
            int startX = 1250;
            int startY = 210;
            int endX = 480;
            int endY = 100;
            int arcHeight = 10;
            long duration = 3000;
            return new AnimatedObject("bird", startX, startY, endX, endY, arcHeight, duration);
        } else if ("cat".equals(type)) {
            int startY = 900;
            int startX = 1280;
            int endX = 630;
            int endY = 650;
            int arcHeight = 50;
            long duration = 6000;
            return new AnimatedObject("cat", startX, startY, endX, endY, arcHeight, duration);
        }
        return null;
    }

    public void drawAnimatedObjects(Graphics g) {
        if (!animationsEnabled || animatedObjects.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (AnimatedObject obj : animatedObjects) {
            if (!obj.active) continue;

            String key = obj.type + "_" + obj.frame;
            BufferedImage img = imageManager.getImage(key);
            if (img == null) continue;

            int width = "cat".equals(obj.type) ? 150 : 100;
            int height = (int) (width * ((double) img.getHeight() / img.getWidth()));
            g2d.drawImage(img, obj.currentX - width / 2, obj.currentY - height / 2, width, height, null);
        }
        g2d.dispose();
    }

    public void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
        if (!enabled) animatedObjects.clear();
    }

    public void setCurrentScenario(String filename) {
        this.currentScenarioFile = filename;
    }

    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }

    // Вложенный класс анимированного объекта
    public static class AnimatedObject {
        String type;
        int startX, startY, endX, endY;
        int arcHeight;
        long startTime, duration;
        boolean active;
        int currentX, currentY;
        int frame;

        public AnimatedObject(String type, int startX, int startY, int endX, int endY, int arcHeight, long duration) {
            this.type = type;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.arcHeight = arcHeight;
            this.duration = duration;
            this.startTime = System.currentTimeMillis();
            this.active = true;
            this.frame = 0;
            updatePosition(0);
        }

        public boolean update() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {
                active = false;
                return false;
            }

            double t = (double) elapsed / duration;
            updatePosition(t);
            frame = (int) ((elapsed / 150) % 2);
            return true;
        }

        private void updatePosition(double t) {
            currentX = (int) (startX + (endX - startX) * t);
            double linearY = startY + (endY - startY) * t;
            double arcOffset = arcHeight * Math.sin(Math.PI * t);
            currentY = (int) (linearY - arcOffset);
        }
    }
}