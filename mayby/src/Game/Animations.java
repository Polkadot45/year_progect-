package Game;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public class Animations {

    private final Game game;// Ссылка на основной класс игры
    private final MyImage imageManager;// Менеджер изображений для получения анимаций
    private java.util.List<AnimatedObject> animatedObjects = new java.util.ArrayList<>();// Список всех активных анимированных объектов
    private Timer animationTimer;// Таймер для обновления анимаций (30 кадров в секунду)
    private boolean animationsEnabled = true;// Флаг включения/выключения анимаций
    private String currentScenarioFile;// Текущий файл сценария (определяет, какие анимации разрешены)
    private int currentReplicaIndex = 0;
    private int totalReplicas = 0;// Общее количество реплик в текущем сценарии
    private final Map<String, List<String>> scenarioAllowedObjects = new HashMap<>();// Карта разрешённых анимаций для каждого файла сценария

    public static class AnimatedObject {
        String type; // Тип объекта ("bird" или "cat")
        int startX, startY, endX, endY; // Начальные и конечные координаты
        int arcHeight; // Высота дуги траектории
        long startTime, duration; // Время начала и длительность анимации
        boolean active; // Активен ли объект
        int currentX, currentY; // Текущие координаты
        int frame; // Текущий кадр анимации (0 или 1)

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
            updatePosition(0); // Инициализируем начальную позицию
        }

        public boolean update() {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {// Проверяем, завершена ли анимация
                active = false;
                return false;
            }

            double t = (double) elapsed / duration; // Вычисляем прогресс анимации (0.0 = начало, 1.0 = конец)
            updatePosition(t); // Обновляем позицию
            frame = (int) ((elapsed / 150) % 2); // Чередуем кадры каждые 150 мс
            return true;
        }

        private void updatePosition(double t) {
            currentX = (int) (startX + (endX - startX) * t); //по прямой линии
            double linearY = startY + (endY - startY) * t; //по прямой линии
            double arcOffset = arcHeight * Math.sin(Math.PI * t); //вертикальное смещение, которое создаёт эффект дуги
            currentY = (int) (linearY - arcOffset);
        }
    }

    public void updateProgress(int currentIndex, int totalCount) {
        this.currentReplicaIndex = currentIndex;
        this.totalReplicas = totalCount;
    }

    public Animations(Game game, MyImage imageManager) {
        this.game = game;
        this.imageManager = imageManager;

        // Инициализация разрешённых анимаций для каждого сценария
        scenarioAllowedObjects.put("1_1.txt", Arrays.asList("bird"));
        scenarioAllowedObjects.put("1_2.txt", Arrays.asList("bird"));
        scenarioAllowedObjects.put("1_3.txt", Arrays.asList("bird"));
        scenarioAllowedObjects.put("1_03.txt", Arrays.asList("cat"));
        scenarioAllowedObjects.put("1_4.txt", Arrays.asList("cat"));
        scenarioAllowedObjects.put("1_5.txt", Arrays.asList("cat"));

        startTimer(); // Запускаем таймер анимаций
    }

    public void startTimer() {
        if (animationTimer == null) {
            animationTimer = new Timer(33, e -> updateAnimations());// Создаём таймер с интервалом 33 мс (примерно 30 кадров в секунду)
            animationTimer.start();
        }
    }

    private void updateAnimations() {
        if (!animationsEnabled || game.getCurrentScene() != Game.Scene.STORY || currentScenarioFile == null) {  // Проверяем, можно ли обновлять анимации
            return;
        }

        animatedObjects.removeIf(obj -> !obj.update());// Удаляем завершённые анимации

        int remaining = totalReplicas - currentReplicaIndex;// сколько реплик осталось до конца сценария
        boolean shouldSpawnBird = false;
        boolean shouldSpawnCat = false;

        // Проверяем условия для появления птиц
        if (("1_1.txt".equals(currentScenarioFile) ||
                "1_2.txt".equals(currentScenarioFile) ||
                "1_3.txt".equals(currentScenarioFile)) && remaining >= 10) {
            shouldSpawnBird = true;
        }

        // Проверяем условия для появления кошек
        if (("1_4.txt".equals(currentScenarioFile) ||
                "1_5.txt".equals(currentScenarioFile)) && remaining >= 13) {
            shouldSpawnCat = true;
        }

        double spawnChance = 0.0;
        boolean canSpawn = false;

        // Настраиваем параметры спавна в зависимости от типа анимации
        if (shouldSpawnBird) {
            spawnChance = 0.007; // Вероятность появления птицы
            canSpawn = animatedObjects.size() < 2; // Максимум 2 птицы одновременно
        } else if (shouldSpawnCat) {
            spawnChance = 0.003; // Вероятность появления кошки
            canSpawn = animatedObjects.isEmpty(); // Только одна кошка одновременно
        }

        // С вероятностью spawnChance создаём новый анимированный объект
        if (canSpawn && Math.random() < spawnChance) {
            spawnRandomObject();
        }

        game.repaintPanel();// перерисовку панели
    }

    private void spawnRandomObject() {

        if (currentScenarioFile == null || !scenarioAllowedObjects.containsKey(currentScenarioFile)) return;// Проверяем, есть ли разрешённые анимации для текущего сценария
        List<String> types = scenarioAllowedObjects.get(currentScenarioFile);
        if (types.isEmpty()) return;

        // Выбираем случайный тип анимации
        String type = types.get((int) (Math.random() * types.size())); //случайное число от 0.0 до 1.0 ,Количество элементов в списке
        AnimatedObject obj = createRandomObject(type); //кошка\птица
        if (obj != null) animatedObjects.add(obj);//Добавляет новый анимированный объект
    }

    private AnimatedObject createRandomObject(String type) {
        if ("bird".equals(type)) {
            // Параметры полёта птицы
            int startX = 1250;
            int startY = 210;
            int endX = 480;
            int endY = 100;
            int arcHeight = 10; // Высота дуги полёта
            long duration = 3000; // Длительность анимации (3 секунды)
            return new AnimatedObject("bird", startX, startY, endX, endY, arcHeight, duration);
        } else if ("cat".equals(type)) {
            // Параметры движения кошки
            int startY = 900;
            int startX = 1280;
            int endX = 630;
            int endY = 650;
            int arcHeight = 50; // Большая дуга для кошки
            long duration = 6000; // Длительность 6 секунд
            return new AnimatedObject("cat", startX, startY, endX, endY, arcHeight, duration);
        }
        return null;
    }

    public void drawAnimatedObjects(Graphics g) {
        if (!animationsEnabled || animatedObjects.isEmpty()) return;// Проверяем, можно ли рисовать анимации

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// Включаем сглаживание для плавных анимаций
        for (AnimatedObject obj : animatedObjects) {
            if (!obj.active) continue; // Пропускаем неактивные объекты

            // Формируем ключ для получения изображения (bird_0, bird_1, cat_0, cat_1)
            String key = obj.type + "_" + obj.frame;
            BufferedImage img = imageManager.getImage(key);
            if (img == null) continue; // Если изображение не найдено — пропускаем

            // Устанавливаем размеры изображения
            int width = "cat".equals(obj.type) ? 150 : 100;
            int height = (int) (width * ((double) img.getHeight() / img.getWidth()));

            g2d.drawImage(img, obj.currentX - width / 2, obj.currentY - height / 2, width, height, null);// Рисуем изображение с центрированием по текущей позиции
        }
        g2d.dispose();
    }

    public void setAnimationsEnabled(boolean enabled) {
        animationsEnabled = enabled;
        if (!enabled) animatedObjects.clear(); // При выключении очищаем список
    }

    public void setCurrentScenario(String filename) {
        this.currentScenarioFile = filename;
    }

    public boolean isAnimationsEnabled() {
        return animationsEnabled;
    }
}