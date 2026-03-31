package Game;
import Size.Size;
import MyImage.MyImage;
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

public class Game extends JFrame {

    public static void main(String[] args) {
        new Game().setVisible(true);
    }

    public Game() {
        try {
            imageManager.loadAllImages();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Ошибка загрузки изображений:\n" + e.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        Size.initSize(backgrounds, buttons, characters, replicas);

        animations = new Animations(this, imageManager);
        replicaManager = new Replica(this);

        playBackgroundMusic();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentScene == Scene.STORY && currentScenarioFile != null) {
                    saveProgress("save_var.dat");
                }
            }
        });

        setTitle("Бесприданница");
        setSize(1920, 1080);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        GamePanel panel = new GamePanel();
        add(panel);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
                panel.repaint();
            }
        });
    }

    enum Scene {
        MENU, SETTINGS, INTRO, STORY, MODE_SELECT, RESET_SCREEN,
    }


    //доделать презу, проверить презинтацию,все ли подпрограммы и в таблице классы есть
    //музыка,настройки,заменить грустную ларису на веселую,робинзон в конце поменять
    //выбор основа или с развилками

    private final Map<String, Rect> buttons = new HashMap<>();
    private final Map<String, Rect> backgrounds = new HashMap<>();
    private final Map<String, Rect> characters = new HashMap<>();
    private final Map<String, Rect> replicas = new HashMap<>();

    private Scene currentScene = Scene.MENU;
    private final MyImage imageManager = new MyImage();

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
    private boolean musicEnabled = true; // Флаг включения музыки

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

        public DialogueLine(String text, int x, int y, String characterKey) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.characterKey = characterKey;
            this.isChoicePoint = false;
        }

        public DialogueLine(int x, int y, String text1, String file1, String text2, String file2) {
            this.text = "";
            this.x = x;
            this.y = y;
            this.characterKey = null;
            this.isChoicePoint = true;
            this.choiceText1 = text1;
            this.choiceFile1 = file1;
            this.choiceText2 = text2;
            this.choiceFile2 = file2;
        }
    }

    private void handleMouseClick(MouseEvent e) {
        int x = e.getX(), y = e.getY();

        if (inChoiceMode && choiceReplica != null) {
            Rect choice1 = replicas.get("chois1");
            if (choice1 != null && isInside(x, y, choice1)) {
                if (!"continue".equals(choiceReplica.choiceFile1)) {
                    loadScenario(choiceReplica.choiceFile1);
                } else {
                    currentReplicaIndex++;
                }
                inChoiceMode = false;
                choiceReplica = null;
                return;
            }

            Rect choice2 = replicas.get("chois2");
            if (choice2 != null && isInside(x, y, choice2)) {
                if (!"continue".equals(choiceReplica.choiceFile2)) {
                    loadScenario(choiceReplica.choiceFile2);
                } else {
                    currentReplicaIndex++;
                }
                inChoiceMode = false;
                choiceReplica = null;
                return;
            }
        }

        if (isInside(x, y, buttons.get("back"))) {
            if (currentScene == Scene.STORY && currentScenarioFile != null) {
                saveProgress("save_var.dat");
            }

            currentScene = Scene.MENU;
            currentReplicas.clear();
            currentReplicaIndex = 0;
            inChoiceMode = false;
            choiceReplica = null;
            return;
        }

        if (isInside(x, y, buttons.get("exit")) && currentScene == Scene.MENU) {
            System.exit(0);
        }

        switch (currentScene) {
            case MENU:
                // Кнопка "Начать"
                if (isInside(x, y, buttons.get("start"))) {
                    if (hasSavedProgress("save_var.dat")) {
                        if (loadProgress("save_var.dat")) {
                            currentScene = Scene.STORY;
                        } else {
                            JOptionPane.showMessageDialog(Game.this,
                                    "Ошибка загрузки сохранения. Начинаем новую игру.",
                                    "Ошибка", JOptionPane.WARNING_MESSAGE);
                            resetGameState();
                            currentScene = Scene.INTRO;
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
                if (isInside(x, y, buttons.get("music"))) {
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
                    // Проверяем наличие сохранения
                    if (hasSavedProgress("save_var.dat")) {
                        if (loadProgress("save_var.dat")) {
                            currentScene = Scene.STORY;
                        } else {
                            JOptionPane.showMessageDialog(Game.this,
                                    "Ошибка загрузки сохранения. Начинаем новую игру.",
                                    "Ошибка", JOptionPane.WARNING_MESSAGE);
                            resetGameState();
                            currentScene = Scene.MODE_SELECT;
                        }
                    } else {
                        currentScene = Scene.MODE_SELECT;
                    }
                }
                break;

            case MODE_SELECT:
                // Выбор линейной истории
                if (isInside(x, y, buttons.get("line_s"))) {
                    storyWithChoices = false;
                    resetGameState();
                    currentScene = Scene.STORY;
                    loadScenario("line_story.txt");
                    currentReplicaIndex = 0;
                }

                else if (isInside(x, y, buttons.get("var_s"))) {
                    storyWithChoices = true;
                    File saveFile = new File("save_var.dat");
                    if (saveFile.exists()) {
                        if (loadProgress("save_var.dat")) {
                            currentScene = Scene.STORY;
                        } else {

                            resetGameState();
                            currentScene = Scene.STORY;
                            loadScenario("1_1.txt");
                            currentReplicaIndex = 0;
                        }
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

                        if (currentReplicaIndex < currentReplicas.size() - 1) {
                            currentReplicaIndex++;
                        } else {
                            currentReplicas.clear();
                            currentReplicaIndex = 0;
                            backgroundChanges.clear();
                            currentScene = Scene.MENU;
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
        } catch (IOException e) {
            System.err.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private boolean loadProgress(String filename) {
        File saveFile = new File(filename);
        if (!saveFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String file = reader.readLine();
            int index = Integer.parseInt(reader.readLine());
            boolean choiceMode = Boolean.parseBoolean(reader.readLine());
            boolean mode = Boolean.parseBoolean(reader.readLine());

            loadScenario(file);
            currentReplicaIndex = index;
            inChoiceMode = choiceMode;
            storyWithChoices = mode;
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка загрузки: " + e.getMessage());
            return false;
        }
    }

    // Проверка наличия сохранения
    private boolean hasSavedProgress(String filename) {
        return new File(filename).exists();
    }

    // Сброс прогресса
    private void resetProgress() {
        File saveFile = new File("save_var.dat");

        // Попытка удаления файла
        boolean deleted = false;
        if (saveFile.exists()) {
            deleted = saveFile.delete();
            if (deleted) {
                System.out.println("🗑️ Файл сохранения удален: " + saveFile.getAbsolutePath());
            } else {
                System.err.println("❌ Не удалось удалить файл: " + saveFile.getAbsolutePath());
                // Принудительная очистка через перезапись
                try (FileWriter writer = new FileWriter(saveFile)) {
                    writer.write(""); // Очищаем содержимое
                    System.out.println("🗑️ Файл сохранения очищен");
                } catch (IOException e) {
                    System.err.println("❌ Ошибка очистки файла: " + e.getMessage());
                }
            }
        } else {
            System.out.println("ℹ️ Файл сохранения не найден");
        }

        // Сброс состояния игры

        resetGameState();
        storyWithChoices = true; // Возвращаем режим по умолчанию
    }

    private void resetGameState() {
        currentReplicas.clear();
        backgroundChanges.clear();
        currentReplicaIndex = 0;
        inChoiceMode = false;
        choiceReplica = null;
        currentScenarioFile = null;
        currentScene = Scene.MENU;
        System.out.println("🔄 Состояние игры сброшено");
    }

    private void loadScenario(String filename) {
        if (replicaManager.loadScenario(filename)) {

            currentReplicas.clear();
            currentReplicas.addAll(replicaManager.getCurrentReplicas());

            backgroundChanges.clear();
            backgroundChanges.putAll(replicaManager.getBackgroundChanges());

            inChoiceMode = false;
            choiceReplica = null;
            currentScenarioFile = filename;

            animations.setCurrentScenario(filename);
        }
    }

    private void drawReplica(Graphics g, DialogueLine replica) {
        Graphics2D g2d = (Graphics2D) g.create();
        Font font = new Font("Arial", Font.PLAIN, 32);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();

        int maxWidth;
        if (replica.characterKey == null) {
            Rect r = replicas.get("replica_s");
            maxWidth = (r != null) ? r.w - 380 : 450;
        } else {
            Rect r = replicas.get("replica_d");
            maxWidth = (r != null) ? r.w - 380 : 450;
        }

        int leftPadding = 0;
        String[] paragraphs = replica.text.split("\n");
        int currentY = replica.y + lineHeight - fm.getDescent();

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                currentY += lineHeight;
                continue;
            }

            java.util.List<String> lines = wrapText(paragraph, fm, maxWidth);
            for (String line : lines) {
                int textX = replica.x + leftPadding;
                g2d.drawString(line, textX, currentY);
                currentY += lineHeight;
            }
            currentY += lineHeight / 3;
        }
        g2d.dispose();
    }

    private java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" ");
        if (words.length == 0) return lines;

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (fm.stringWidth(word) > maxWidth) {
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder();
                }
                StringBuilder chunk = new StringBuilder();
                for (char c : word.toCharArray()) {
                    String test = chunk.toString() + c;
                    if (fm.stringWidth(test) > maxWidth && chunk.length() > 0) {
                        lines.add(chunk.toString());
                        chunk = new StringBuilder(String.valueOf(c));
                    } else {
                        chunk.append(c);
                    }
                }
                if (chunk.length() > 0) currentLine.append(chunk);
                continue;
            }

            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            if (fm.stringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) lines.add(currentLine.toString());
        return lines;
    }

    private void drawChoiceText(Graphics g, String text, int x, int y, int maxWidth) {
        Graphics2D g2d = (Graphics2D) g.create();
        Font font = new Font("Arial", Font.PLAIN, 32);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight();
        java.util.List<String> lines = wrapText(text, fm, maxWidth);
        int currentY = y + 20;
        int leftPadding = 25;

        for (String line : lines) {
            g2d.drawString(line, x + leftPadding, currentY);
            currentY += lineHeight;
        }
        g2d.dispose();
    }

    public Scene getCurrentScene() { return currentScene; }
    public void repaintPanel() {
        if (getComponentCount() > 0 && getComponent(0) instanceof GamePanel) {
            ((GamePanel) getComponent(0)).repaint();
        }
    }

    private void playBackgroundMusic() {
        if (!musicEnabled) return;

        try {
            File musicFile = new File("C:/Users/User/IdeaProjects/year_progect-/mayby/src/resources/background.wav");
            if (!musicFile.exists()) {
                System.err.println("🎵 Файл музыки не найден: " + musicFile.getAbsolutePath());
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Зациклить музыку
            backgroundMusic.start();

            System.out.println("🎵 Фоновая музыка запущена");
        } catch (Exception e) {
            System.err.println("❌ Ошибка воспроизведения музыки: " + e.getMessage());
        }
    }

    // Остановка музыки
    private void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
            System.out.println("🎵 Музыка остановлена");
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
        System.out.println("🎵 Музыка: " + (musicEnabled ? "ВКЛ" : "ВЫКЛ"));
    }

    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            switch (currentScene) {
                case MENU:
                    drawBackground("phon", g);
                    drawImage("start", buttons.get("start"), g);
                    drawImage("set", buttons.get("set"), g);
                    drawImage("exit", buttons.get("exit"), g);
                    break;

                case SETTINGS:
                    drawBackground("seti", g);
                    drawImage("stop", buttons.get("back"), g);
                    drawImage("sbros", buttons.get("sbros"), g);
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
                    drawImage("line_s", buttons.get("line_s"), g);
                    drawImage("var_s", buttons.get("var_s"), g);
                    drawImage("stop", buttons.get("back"), g);

                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setFont(new Font("Arial", Font.BOLD, 36));
                    g2d.setColor(Color.BLACK);
                    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2d.drawString("Выберите режим игры:", 700, 300);
                    g2d.dispose();

                    Graphics2D g2dm = (Graphics2D) g.create();
                    g2dm.setFont(new Font("Arial", Font.BOLD, 32));
                    g2dm.setColor(Color.BLACK);
                    g2dm.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2dm.drawString("Оригинал пьессы", 500, 485);
                    g2dm.drawString("Интерпретированная история", 1033, 485);
                    g2dm.dispose();

                    break;

                case STORY:
                    drawBackground("cafe", g);
                    animations.drawAnimatedObjects(g); // Используем менеджер анимаций

                    if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                        DialogueLine current = currentReplicas.get(currentReplicaIndex);

                        if (current.isChoicePoint && !inChoiceMode) {
                            inChoiceMode = true;
                            choiceReplica = current;
                        }

                        if (!inChoiceMode) {
                            if (current.characterKey != null && !current.isChoicePoint) {
                                Rect charRect = characters.get(current.characterKey);
                                if (charRect != null) {
                                    drawImage(charRect.imgKey, charRect, g);
                                }
                            }

                            if (!current.isChoicePoint) {
                                String replicaKey = (current.characterKey == null) ? "replica_s" : "replica_d";
                                Rect replicaRect = replicas.get(replicaKey);
                                if (replicaRect != null) {
                                    drawImage(replicaKey, replicaRect, g);
                                }
                            }
                        } else {
                            if (currentReplicaIndex > 0) {
                                DialogueLine previousReplica = currentReplicas.get(currentReplicaIndex - 2);
                                if (previousReplica.characterKey != null) {
                                    Rect charRect = characters.get(previousReplica.characterKey);
                                    if (charRect != null) {
                                        drawImage(charRect.imgKey, charRect, g);
                                    }
                                }
                            }

                            Rect choice1 = replicas.get("chois1");
                            if (choice1 != null) {
                                drawImage("chois1", choice1, g);
                                drawChoiceText(g, choiceReplica.choiceText1, choice1.x, choice1.y + 40, choice1.w - 50);
                            }

                            Rect choice2 = replicas.get("chois2");
                            if (choice2 != null) {
                                drawImage("chois2", choice2, g);
                                drawChoiceText(g, choiceReplica.choiceText2, choice2.x, choice2.y + 40, choice2.w - 50);
                            }
                        }
                    }

                    drawImage("stop", buttons.get("back"), g);
                    drawImage("next", buttons.get("next"), g);
                    break;
            }

            if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                DialogueLine current = currentReplicas.get(currentReplicaIndex);
                if (!current.isChoicePoint) {
                    drawReplica(g, current);
                }
            }
        }

        private void drawBackground(String defaultKey, Graphics g) {
            String bgKey = defaultKey;

            if (!backgroundChanges.isEmpty() && currentReplicaIndex >= 0) {
                Integer lastChangeIndex = backgroundChanges.floorKey(currentReplicaIndex);
                if (lastChangeIndex != null) {
                    String newBg = backgroundChanges.get(lastChangeIndex);
                    if (backgrounds.containsKey(newBg)) {
                        bgKey = newBg;
                    }
                }
            }

            Rect bg = backgrounds.get(bgKey);
            if (bg == null) return;

            BufferedImage img = imageManager.getImage(bg.imgKey);
            if (img != null) {
                g.drawImage(img, bg.x, bg.y, getWidth(), getHeight(), null);
            }
        }

        private void drawImage(String imgKey, Rect r, Graphics g) {
            if (r == null) return;
            BufferedImage img = imageManager.getImage(imgKey);
            g.drawImage(img, r.x, r.y, r.w, r.h, null);
        }
    }
}