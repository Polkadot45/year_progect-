package Game; //все классы находятся в одном пакете Game, поэтому они видят друг друга без дополнительных импортов
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.awt.image.BufferedImage;
import javax.sound.sampled.*;
import java.io.File;
import java.awt.FontMetrics;

public class Game extends JFrame {

    public static void main(String[] args) {
        new Game().setVisible(true);
    }  //создание окна игры

    public Game() {

        setTitle("Бесприданница");//заголовок окна
        setSize(1920, 1080);//размер окна
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//при закрытии окна программа завершает работу
        setResizable(false);//запрет на изменение размера окна
        setLocationRelativeTo(null);//окно в центре экрана

        GamePanel panel = new GamePanel();
        add(panel); //добавление панели в окно

        Size.initSize(backgrounds, buttons, characters, replicas); // задача координат всем элементам

        animations = new Animations(this, imageManager); //получение информации о сцене и нужных картинках
        replicaManager = new Replica(this);

        playBackgroundMusic(); //запуск фоновой музыки

        try { //загрузка всех озображений
            imageManager.loadAllImages();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки изображений:\n" + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        addWindowListener(new WindowAdapter() { //сохранение прогресса при закрытии программы
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentScene == Scene.STORY && currentScenarioFile != null) {
                    saveProgress("save_var.dat");
                }
            }
        });

        addMouseListener(new MouseAdapter() {  //любой клик > handleMouseClick , перерисовка экрана
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
                panel.repaint();
            }
        });
    }

    enum Scene { //тип данных в Java,представляет фиксированный набор констант
        MENU, SETTINGS, INTRO, STORY, MODE_SELECT, RESET_SCREEN,
    }

    private final Map<String, Rect> buttons = new HashMap<>(); //ассоциативный массив(ключ>значение), нельзя поменять ссылку на объект
    private final Map<String, Rect> backgrounds = new HashMap<>();
    private final Map<String, Rect> characters = new HashMap<>();
    private final Map<String, Rect> replicas = new HashMap<>();

    private Scene currentScene = Scene.MENU;
    private final MyImage imageManager = new MyImage(); //определенная картинка

    private Animations animations;
    private Replica replicaManager;

    private int currentReplicaIndex = 0;
    private String currentScenarioFile = null;
    private boolean inChoiceMode = false;
    private DialogueLine choiceReplica = null;

    private TreeMap<Integer, String> backgroundChanges = new TreeMap<>();
    private java.util.List<DialogueLine> currentReplicas = new java.util.ArrayList<>();

    private boolean storyWithChoices = true;

    private Clip backgroundMusic;
    private boolean musicEnabled = true;// Флаг включения музыки

    private int choicePoints = 0; // Накопленные очки выборов
    private boolean isEndingScenario = false; // Флаг: сейчас идёт показ концовки

    public static class Rect {
        int x, y, w, h;
        String imgKey;
        public Rect(String imgKey, int x, int y, int w, int h) {
            this.imgKey = imgKey;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }

    public static class DialogueLine {
        String text;
        int x, y;
        String characterKey;
        boolean isChoicePoint;
        String choiceFile1;
        String choiceFile2;
        String choiceText1;
        String choiceText2;
        int choiceValue1; // Значение первого выбора
        int choiceValue2; // Значение второго выбора

        // Обычная реплика
        public DialogueLine(String text, int x, int y, String characterKey) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.characterKey = characterKey;
            this.isChoicePoint = false;
        }

        // Точка выбора
        public DialogueLine(int x, int y, String text1, String file1, int value1,
                            String text2, String file2, int value2) {
            this.text = "";
            this.x = x;
            this.y = y;
            this.characterKey = null;
            this.isChoicePoint = true;
            this.choiceText1 = text1;
            this.choiceFile1 = file1;
            this.choiceValue1 = value1;
            this.choiceText2 = text2;
            this.choiceFile2 = file2;
            this.choiceValue2 = value2;
        }
    }

    private void handleMouseClick(MouseEvent e) {
        int x = e.getX(), y = e.getY();

        if (inChoiceMode && choiceReplica != null) {

            Rect choice1 = replicas.get("chois1");
            Rect choice2 = replicas.get("chois2");

            if (choice1 != null && isInside(x, y, choice1)) {

                choicePoints += choiceReplica.choiceValue1;// Накопление очков выбора
                loadScenario(choiceReplica.choiceFile1);
                currentReplicaIndex = 0; // начинаем с первой реплики нового сценария
                inChoiceMode = false;
                choiceReplica = null;
                return;
            }


            if (choice2 != null && isInside(x, y, choice2)) {

                choicePoints += choiceReplica.choiceValue2;
                loadScenario(choiceReplica.choiceFile2);
                currentReplicaIndex = 0;
                inChoiceMode = false;
                choiceReplica = null;
                return;
            }
        }

        //"Back"
        if (x>=1800 && x<=1880 && y>=42 && y<=142) {

            if (currentScene == Scene.STORY && currentScenarioFile != null) {
                saveProgress("save_var.dat");
            }

            backgroundChanges.clear();
            currentScene = Scene.MENU;
            currentReplicas.clear();
            currentReplicaIndex = 0;
            inChoiceMode = false;
            choiceReplica = null;
            return;
        }

        switch (currentScene) {
            case MENU:

                // Кнопка "Начать"
                if (isInside(x, y, buttons.get("start"))){
                    if (hasSavedProgress("save_var.dat")) {
                        if (loadProgress("save_var.dat")) {
                            currentScene = Scene.STORY;
                        }
                    } else {
                        currentScene = Scene.INTRO;
                    }
                }
                // Кнопка "Настройки"
                else if (isInside(x, y, buttons.get("set"))) {
                    currentScene = Scene.SETTINGS;
                }
                // Кнопка "Выход"
                else if (isInside(x, y, buttons.get("exit"))) {
                    System.exit(0);
                }
                break;

            case SETTINGS:

                if (isInside(x, y, buttons.get("sbros"))) {
                    resetProgress();
                    currentScene = Scene.RESET_SCREEN;
                }
                if (x>=733 && x<=793 && y>=384 && y<=424) {
                    toggleMusic();
                }
                break;

            case RESET_SCREEN:

                if (isInside(x, y, buttons.get("sbros_set"))) {
                    currentScene = Scene.MENU;
                }
                break;

            case INTRO:

                if (isInside(x, y, buttons.get("next"))) {
                    if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                        DialogueLine current = currentReplicas.get(currentReplicaIndex);

                        if (current.isChoicePoint) {
                            inChoiceMode = true;
                            choiceReplica = current;
                            return;
                        }

                        if (currentReplicaIndex < currentReplicas.size() - 1) {
                            currentReplicaIndex++;
                        } else {
                            showEnding();//Конец сценария — показываем концовку
                        }
                    }
                    else{
                        currentScene = Scene.MODE_SELECT;
                    }
                }
                break;

            case MODE_SELECT:

                // Выбор линейной истории
                if (isInside(x, y, buttons.get("line_s"))) {
                    resetGameState();
                    storyWithChoices = false;
                    isEndingScenario = false;
                    currentScene = Scene.STORY;
                    loadScenario("line_story.txt");
                    currentReplicaIndex = 0;
                }

                else if (isInside(x, y, buttons.get("var_s"))) {
                    storyWithChoices = true;
                    File saveFile = new File("save_var.dat");
                    if (saveFile.exists()) {
                            currentScene = Scene.STORY;
                    } else {
                        // Новая игра с развилками
                        resetGameState();
                        currentScene = Scene.STORY;
                        loadScenario("1_1.txt");
                        currentReplicaIndex = 0;
                    }
                }

                else if (isInside(x, y, buttons.get("back"))) {
                    currentScene = Scene.MENU;
                    backgroundChanges.clear();
                }

                break;


            case STORY:
                if (isInside(x, y, buttons.get("next"))) {
                    if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                        DialogueLine current = currentReplicas.get(currentReplicaIndex);

                        if (current.isChoicePoint) {
                            inChoiceMode = true;
                            choiceReplica = current;
                            return;
                        }

                        // Если это последняя реплика текущего сценария
                        if (currentReplicaIndex == currentReplicas.size() - 1) {
                            if (isEndingScenario) {
                                // концовка завершена > сбрасываем всё и возвращаемся в меню
                                resetProgress();
                                resetGameState();
                                currentScene = Scene.MENU;
                            } else if (storyWithChoices) {
                                // основной сценарий с развилками завершён > показываем концовку
                                showEnding();
                            } else {
                                // Линейная история завершена > сразу в меню
                                resetProgress();
                                resetGameState();
                                currentScene = Scene.MENU;
                            }
                        } else {
                            // Обычный переход к следующей реплике
                            currentReplicaIndex++;
                        }
                    }
                }
                break;
        }
    }

    private boolean isInside(int mx, int my, Rect r) {
        return r != null && mx >= r.x && mx <= r.x + r.w && my >= r.y && my <= r.y + r.h;
    }

    private void saveProgress(String filename) {
        if (currentScenarioFile == null) return;

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(currentScenarioFile + "\n");
            writer.write(currentReplicaIndex + "\n");
            writer.write(inChoiceMode + "\n");
            writer.write(storyWithChoices + "\n");
            writer.write(choicePoints + "\n"); // Сохраняем очки выборов
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private boolean loadProgress(String filename) {
        File saveFile = new File(filename);
        if (!saveFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) { //BufferedReader делает чтение быстрее
            String file = reader.readLine();
            int index = Integer.parseInt(reader.readLine());
            boolean choiceMode = Boolean.parseBoolean(reader.readLine());
            boolean mode = Boolean.parseBoolean(reader.readLine());
            int points = Integer.parseInt(reader.readLine()); // Загружаем очки

            loadScenario(file);
            currentReplicaIndex = index;
            inChoiceMode = choiceMode;
            storyWithChoices = mode;
            choicePoints = points; // Восстанавливаем очки

            return true;
        } catch (Exception e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
            return false;
        }
    }

    // Проверка наличия сохранения
    private boolean hasSavedProgress(String filename) {
        return new File(filename).exists(); //Возвращает boolean (true/false)
    }

    // Сброс прогресса
    private void resetProgress() {
        File saveFile = new File("save_var.dat");

        if (saveFile.exists()) {
            saveFile.delete();
        }

        resetGameState();
    }

    private void resetGameState() {
        currentReplicas.clear();
        backgroundChanges.clear();
        currentReplicaIndex = 0;
        inChoiceMode = false;
        choiceReplica = null;
        currentScenarioFile = null;
        choicePoints = 0;
        storyWithChoices = true;
        isEndingScenario = false;
    }

    private void loadScenario(String filename) {
        if (replicaManager.loadScenario(filename)) {

            currentReplicas.clear(); // удаляем все старые реплики из памяти
            currentReplicas.addAll(replicaManager.getCurrentReplicas());//копируем новые реплики из парсера

            backgroundChanges.clear();
            backgroundChanges.putAll(replicaManager.getBackgroundChanges());

            inChoiceMode = false;
            choiceReplica = null;
            currentScenarioFile = filename;
            animations.setCurrentScenario(filename);
        }
    }

    public Scene getCurrentScene() { return currentScene; }

    public void repaintPanel() {
        if (getComponentCount() > 0 && getComponent(0) instanceof GamePanel) { //Проверяет есть ли компоненты в окне и что первый компонент — это GamePanel
            ((GamePanel) getComponent(0)).repaint(); //точечное обновление
        }
    }

    private void drawReplica(Graphics g, DialogueLine replica) {
        Graphics2D g2d = (Graphics2D) g.create(); //создаёт копию графического контекста чтобы не повредить оригинальный Graphics объект, все изменения будут применены только к этой копии
        Font font = new Font("Arial", Font.PLAIN, 32);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); //сглаживание текста

        FontMetrics fm = g2d.getFontMetrics(); //информация о шрифте(высота строки,ширина символов)
        int lineHeight = fm.getHeight(); //высота одной строки текста (в пикселях)
        int maxWidth;

        if (replica.characterKey == null) {
            Rect r = replicas.get("replica_s");
            maxWidth = (r != null) ? r.w - 380 : 450;
        } else {
            Rect r = replicas.get("replica_d");
            maxWidth = (r != null) ? r.w - 380 : 450;
        }

        String[] paragraphs = replica.text.split("\n"); //разбивает текст на абзацы по символу новой строки
        int currentY = replica.y + lineHeight - fm.getDescent(); //начальная вертикальная позиция первой строки

        //Рисование каждого абзаца,пробелы между строками
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                currentY += lineHeight;
                continue;
            }

            //Перенос текста и рисование строк
            java.util.List<String> lines = Replica.wrapText(paragraph, fm, maxWidth); //разбивает длинный абзац на строки, которые помещаются в maxWidth
            for (String line : lines) {
                g2d.drawString(line, replica.x, currentY); //рисует одну строку текста
                currentY += lineHeight;//перемещает позицию вниз для следующей строки
            }
            currentY += lineHeight / 3; // Дополнительный отступ между абзацами
        }
        g2d.dispose(); //облегчение работы программы
    }


    private void drawChoiceText(Graphics g, String text, int x, int y, int maxWidth) {
        Graphics2D g2d = (Graphics2D) g.create();
        Font font = new Font("Arial", Font.PLAIN, 32);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        java.util.List<String> lines = Replica.wrapText(text, fm, maxWidth);
        int currentY = y + 20; //начальная вертикальная позиция (с небольшим отступом сверху кнопки)
        int leftPadding = 25;//отступ слева (чтобы текст не прилипал к краю кнопки)

        //Рисование каждой строки
        for (String line : lines) {
            g2d.drawString(line, x + leftPadding, currentY);
            currentY += lineHeight; //увеличивается на высоту строки
        }
        g2d.dispose();
    }

    private void showEnding() {
        int endingIndex;
        if (choicePoints <= 1) endingIndex = 1;
        else if (choicePoints <= 3) endingIndex = 2;
        else if (choicePoints <= 5) endingIndex = 3;
        else endingIndex = 4;

        // Загружаем файл концовки как обычный сценарий
        loadScenario("ending_" + endingIndex + ".txt");
        currentReplicaIndex = 0;
        isEndingScenario = true; // Устанавливаем флаг

    }

    private void playBackgroundMusic() {
        if (!musicEnabled) return; //музыка включена > ничего не делать

        try {
            InputStream audioSrc = getClass().getResourceAsStream("/resources/background.wav"); // загружает файл из ресурсов
            if (audioSrc == null) {
                System.err.println("Ресурс не найден");
                return;
            }

            BufferedInputStream bufferedIn = new BufferedInputStream(audioSrc);//ускоряет чтение файла (буферизация)
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);//преобразует обычный поток в аудиопоток, который понимает Java Sound API

            backgroundMusic = AudioSystem.getClip();//специальный объект для воспроизведения аудио в Java
            backgroundMusic.open(audioStream);//загружает аудиофайл в память
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Зациклить музыку
            backgroundMusic.start();//начинает воспроизведение

        } catch (Exception e) {
            System.err.println("Ошибка воспроизведения музыки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Остановка музыки
    private void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    // Переключение музыки (вкл/выкл)
    private void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (musicEnabled) {
            playBackgroundMusic();
        } else {
            stopMusic();
        }
    }

    // Внутренний класс GamePanel — отвечает за всю графику игры
    class GamePanel extends JPanel {

        // Переопределяем метод paintComponent — он вызывается каждый раз, когда нужно перерисовать экран
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g); // Сначала рисуем стандартный фон панели (чёрный)

            switch (currentScene) {

                case MENU:
                    drawBackground("phon", g); // Рисуем фон главного меню
                    // Рисуем кнопки меню: "Начать", "Настройки", "Выход"
                    drawImage("start", buttons.get("start"), g);
                    drawImage("set", buttons.get("set"), g);
                    drawImage("exit", buttons.get("exit"), g);
                    break;

                case SETTINGS:
                    drawBackground("seti", g);
                    drawImage("stop", buttons.get("back"), g);
                    drawImage("sbros", buttons.get("sbros"), g);
                    // Кнопка музыки: показываем "включено" или "выключено" в зависимости от состояния
                    String musicButton = musicEnabled ? "music_on" : "music_off";
                    drawImage(musicButton, buttons.get("music"), g);
                    break;

                case INTRO:
                    drawBackground("people", g);
                    drawImage("next", buttons.get("next"), g);
                    break;

                case RESET_SCREEN:
                    drawBackground("reset", g);
                    drawImage("sbros_set", buttons.get("sbros_set"), g);
                    break;

                case MODE_SELECT:
                    drawBackground("cafe", g);
                    // Кнопки выбора: линейная история или с развилками
                    drawImage("line_s", buttons.get("line_s"), g);
                    drawImage("var_s", buttons.get("var_s"), g);
                    drawImage("stop", buttons.get("back"), g);

                    // Рисуем заголовок "Выберите режим игры:"
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setFont(new Font("Arial", Font.BOLD, 36));
                    g2d.setColor(Color.BLACK);
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.drawString("Выберите режим игры:", 700, 300);
                    g2d.dispose();

                    // Отрисовываем выбор истории
                    Graphics2D g2dm = (Graphics2D) g.create();
                    g2dm.setFont(new Font("Arial", Font.BOLD, 32));
                    g2dm.setColor(Color.BLACK);
                    g2dm.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2dm.drawString("Оригинал пьессы", 500, 485); // первая кнопка
                    g2dm.drawString("Интерпретированная история", 1033, 485); // вторая кнопка
                    g2dm.dispose();
                    break;

                case STORY:
                    drawBackground("cafe", g);

                    animations.updateProgress(currentReplicaIndex, currentReplicas.size());// Обновляем прогресс для анимаций (сколько реплик осталось)
                    animations.drawAnimatedObjects(g);// Рисуем анимации (птицы, кошки)

                    if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {  // Если есть реплики и мы не вышли за границы
                        DialogueLine current = currentReplicas.get(currentReplicaIndex); // Текущая реплика

                        if (current.isChoicePoint && !inChoiceMode) {  // Если это точка выбора и мы ещё не в режиме выбора
                            inChoiceMode = true;
                            choiceReplica = current; // Запоминаем текущую точку выбора
                        }

                        if (!inChoiceMode) { // Если не в режиме выбора — рисуем обычную реплику
                            if (current.characterKey != null && !current.isChoicePoint) { // Если у реплики есть персонаж — рисуем его
                                Rect charRect = characters.get(current.characterKey);
                                if (charRect != null) {
                                    drawImage(charRect.imgKey, charRect, g);
                                }
                            }

                            if (!current.isChoicePoint) { // Рисуем диалоговое окно
                                String replicaKey = (current.characterKey == null) ? "replica_s" : "replica_d";
                                Rect replicaRect = replicas.get(replicaKey);
                                if (replicaRect != null) {
                                    drawImage(replicaKey, replicaRect, g);
                                }
                            }
                        }
                        else { // Если В режиме выбора — рисуем кнопки выбора
                            if (currentReplicaIndex > 0) {
                                DialogueLine previousReplica = currentReplicas.get(currentReplicaIndex - 2);
                                if (previousReplica.characterKey != null) {
                                    Rect charRect = characters.get(previousReplica.characterKey);
                                    if (charRect != null) {
                                        drawImage(charRect.imgKey, charRect, g);
                                    }
                                }
                            }

                            // Рисуем первую кнопку выбора
                            Rect choice1 = replicas.get("chois1");
                            if (choice1 != null) {
                                drawImage("chois1", choice1, g);
                                drawChoiceText(g, choiceReplica.choiceText1, choice1.x, choice1.y + 40, choice1.w - 50); // Текст на кнопке (с переносом строк)
                            }

                            // Рисуем вторую кнопку выбора
                            Rect choice2 = replicas.get("chois2");
                            if (choice2 != null) {
                                drawImage("chois2", choice2, g);
                                drawChoiceText(g, choiceReplica.choiceText2, choice2.x, choice2.y + 40, choice2.w - 50);
                            }
                        }
                    }

                    // Всегда рисуем системные кнопки: "Назад" и "Далее"
                    drawImage("stop", buttons.get("back"), g);
                    drawImage("next", buttons.get("next"), g);
                    break;
            }

            // После всего — рисуем сам текст реплики (если это не точка выбора)
            if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                DialogueLine current = currentReplicas.get(currentReplicaIndex);
                if (!current.isChoicePoint) {
                    drawReplica(g, current); // Метод с автоматическим переносом строк
                }
            }
        }

        private void drawBackground(String defaultKey, Graphics g) {
            String bgKey = defaultKey; // Фон по умолчанию

            if (!backgroundChanges.isEmpty() && currentReplicaIndex >= 0) { // Если есть изменения фона и мы уже начали читать реплики
                Integer lastChangeIndex = backgroundChanges.floorKey(currentReplicaIndex); //Находим последнее изменение фона ДО или НА текущей реплике
                if (lastChangeIndex != null) {
                    String newBg = backgroundChanges.get(lastChangeIndex);
                    if (backgrounds.containsKey(newBg)) { // Проверяем, существует ли такой фон
                        bgKey = newBg; // Меняем фон
                    }
                }
            }

            Rect bg = backgrounds.get(bgKey); // Получаем координаты и размеры фона
            if (bg == null) return; // Если фон не найден — выходим

            BufferedImage img = imageManager.getImage(bg.imgKey); // Получаем изображение фона
            if (img != null) {
                g.drawImage(img, bg.x, bg.y, getWidth(), getHeight(), null);// Рисуем фон на весь экран (масштабируем до размеров окна)
            }
        }


        private void drawImage(String imgKey, Rect r, Graphics g) {
            BufferedImage img = imageManager.getImage(imgKey); //Получаем изображение по ключу
            g.drawImage(img, r.x, r.y, r.w, r.h, null);
        }
    }
}