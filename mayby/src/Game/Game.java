package Game;
import Size.Size;
import MyImage.MyImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.ArrayList;

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
        MENU, SETTINGS, INTRO,
        CAFE_DIALOG,
    }

    private final Map<String, Rect> buttons = new HashMap<>();
    private final Map<String, Rect> backgrounds = new HashMap<>();
    private final Map<String, Rect> characters = new HashMap<>();
    private final Map<String, Rect> replicas = new HashMap<>();
    private final TreeMap<Integer, String> backgroundChanges = new TreeMap<>();  // Хранит: индекс реплики → ключ фона (TreeMap позволяет искать "последний ключ <= текущего")


    private Scene currentScene = Scene.MENU;
    private final MyImage imageManager = new MyImage();

    private List<Replica> currentReplicas = new ArrayList<>();  //хранение реплик
    private int currentReplicaIndex = 0;
    private String currentScenarioFile = null; //запоминает имя файла текущего сценария для возврата после ветвления.
    private boolean inChoiceMode = false; // обычный диалог/отрисовка разветвления
    private Replica choiceReplica = null; //сохраняет реплику, после которой появляется точка выбора

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

    public static class Replica {
        String text;
        int x, y;
        String characterKey;
        boolean isChoicePoint;
        String choiceFile1;
        String choiceFile2;
        String choiceText1;
        String choiceText2;

        // Обычная реплика
        public Replica(String text, int x, int y, String characterKey) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.characterKey = characterKey; // ключ персонажа
            this.isChoicePoint = false;
        }

        // Точка выбора
        public Replica(int x, int y, String text1, String file1, String text2, String file2) {
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

        // Обработка выбора, приоритет
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
                return; //стоп
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

        // общие кнопки
        if (isInside(x, y, buttons.get("back"))) {
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
                if (isInside(x, y, buttons.get("set"))) currentScene = Scene.SETTINGS;
                else if (isInside(x, y, buttons.get("start"))) currentScene = Scene.INTRO;
                currentReplicaIndex = 0;
                break;

            case INTRO:
                if (isInside(x, y, buttons.get("next"))) {
                    currentScene = Scene.CAFE_DIALOG;
                    loadScenario("1_1.txt");
                    currentReplicaIndex = 0;
                }
                break;

            case CAFE_DIALOG:
                if (isInside(x, y, buttons.get("next"))) {
                    if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                        Replica current = currentReplicas.get(currentReplicaIndex);

                        if (current.isChoicePoint) {
                            inChoiceMode = true;
                            choiceReplica = current;
                            return; //остановиться показать варианты
                        }

                        if (currentReplicaIndex < currentReplicas.size() - 1) {
                            currentReplicaIndex++;
                        } else {
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

    private void loadScenario(String filename) {
        try (InputStream is = getClass().getResourceAsStream("/" + filename)) {  //поток байтов , через который читаются данные файла;загружает файлы
            if (is == null) {
                System.out.println("Файл не найден: " + filename);
                return;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8")); //быстрее читает;из байтов в символы
            String line; //хранения текущей строки файла
            currentReplicas.clear();
            backgroundChanges.clear(); // очищаем старые события смены фона
            currentScenarioFile = filename;

            while ((line = br.readLine()) != null) { //сохранение строк,читая их до \n
                line = line.trim();  //убирает пробелы по краям
                if (line.isEmpty()) continue; //пропуск пустых строк

                // обычная реплика
                if (line.startsWith("#") && !line.startsWith("#choice:")) {
                    int colonIndex = line.indexOf(':', 1); //правильный ли формат
                    if (colonIndex > 0) {
                        String charPart = line.substring(1, colonIndex).trim(); //получение key персонажа
                        String rest = line.substring(colonIndex + 1).trim(); //реплика без персонажа

                        int spaceIndex = rest.indexOf(' '); //поиск пробела после коорд
                        if (spaceIndex > 0) {
                            String coordPart = rest.substring(0, spaceIndex).trim(); //запись коорд
                            String text = rest.substring(spaceIndex + 1).trim(); //запись реплики
                            text = text.replace("|", " ");

                            String[] coords = coordPart.split(","); //массив координат
                            if (coords.length == 2) {
                                try {
                                    int x = Integer.parseInt(coords[0].trim()); //из строки в целое число, запись в массив
                                    int y = Integer.parseInt(coords[1].trim());
                                    String charKey = charPart.isEmpty() ? null : charPart; //разделение на сцены с перс и без
                                    currentReplicas.add(new Replica(text, x, y, charKey));
                                    continue;
                                } catch (NumberFormatException e) {
                                    System.err.println("Неверный формат координат: " + line);
                                }
                            }
                        }
                    }
                }
                // выбор
                else if (line.startsWith("#choice:")) {
                    String params = line.substring(8).trim(); //убираем чойз
                    String[] parts = params.split("\\|"); //массив реплик разделение |
                    if (parts.length == 4) {
                        String text1 = parts[0].trim();
                        String file1 = parts[1].trim();
                        String text2 = parts[2].trim();
                        String file2 = parts[3].trim();
                        currentReplicas.add(new Replica(0, 0, text1, file1, text2, file2));
                        continue;
                    }
                }

                if (line.startsWith("#bg:")) {
                    String bgKey = line.substring(4).trim();
                    backgroundChanges.put(currentReplicas.size(), bgKey); // Сохраняем смену фона для текущего индекса реплики

                    continue;
                }
                System.err.println("Пропущена строка: " + line);
            }
            currentReplicaIndex = 0;
            inChoiceMode = false;
            choiceReplica = null;
            currentScenarioFile = filename;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawReplica(Graphics g, Replica replica) {
        Graphics2D g2d = (Graphics2D) g.create(); //поддержка сглаживания, трансформаций;создает копию и на нем исправляет
        Font font = new Font("Arial", Font.PLAIN, 32); //вид текста
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); //сглаживание текста

        FontMetrics fm = g2d.getFontMetrics(); //информация о шрифте
        int lineHeight = fm.getHeight();//высота строки

        int maxWidth;
        if (replica.characterKey == null) {
            Rect r = replicas.get("replica_s");
            maxWidth = (r != null) ? r.w - 380 : 450; // 50px отступов с каждой стороны
        } else {
            Rect r = replicas.get("replica_d");
            maxWidth = (r != null) ? r.w - 380 : 450;
        }

        int leftPadding = 0; //отступ слева

        String[] paragraphs = replica.text.split("\n"); //разделение на абзацы
        int currentY = replica.y + lineHeight - fm.getDescent(); //y=837 , 2=40 3=8,расстояние от базовой линии до низа символов
        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty()) {
                currentY += lineHeight;
                continue;
            }

            List<String> lines = wrapText(paragraph, fm, maxWidth); //разбивает абзац на слова, набирает их пока меньше мак

            for (String line : lines) {
                int textX = replica.x + leftPadding;
                g2d.drawString(line, textX, currentY);
                currentY += lineHeight;
            }
            currentY += lineHeight / 3; //дополнительный отступ после абзаца для визуального разделения
        }

        g2d.dispose(); //отчистка
    }

    private List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>(); //динамический массив (растёт по мере добавления строк)
        String[] words = text.split(" ");//в массив отдельные слова

        if (words.length == 0) return lines;

        StringBuilder currentLine = new StringBuilder(); //Мутабельная строка — можно добавлять символы без создания новых объектов (эффективно для циклов)

        for (String word : words) {
            if (fm.stringWidth(word) > maxWidth) { //помещается ли слово
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());//сохранение накопленной строки перед обработкой длинного слова
                    currentLine = new StringBuilder();
                }
                StringBuilder chunk = new StringBuilder(); // создание буфера для части длинного слова
                for (char c : word.toCharArray()) { //по словам слова
                    String test = chunk.toString() + c; //помещается ли в строку
                    //если влезает сохранить новую букву,если нет сохранить часть
                    if (fm.stringWidth(test) > maxWidth && chunk.length() > 0) {
                        lines.add(chunk.toString());
                        chunk = new StringBuilder(String.valueOf(c));
                    } else {
                        chunk.append(c);
                    }
                }
                //начало строки с остатка
                if (chunk.length() > 0) {
                    currentLine.append(chunk);
                }
                continue;
            }

            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word; //пусто=просто слово;есть слово=слово+пробел+следующее слво
            if (fm.stringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

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

        List<String> lines = wrapText(text, fm, maxWidth);
        int currentY = y+20;
        int leftPadding = 25;

        for (String line : lines) {
            g2d.drawString(line, x + leftPadding, currentY);
            currentY += lineHeight;
        }

        g2d.dispose();
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
                    drawImage("dot", buttons.get("dot"), g);
                    drawImage("stop", buttons.get("back"), g);
                    break;

                case INTRO:
                    drawBackground("people", g);
                    drawImage("next", buttons.get("next"), g);
                    break;

                case CAFE_DIALOG:
                    drawBackground("cafe", g);

                    if (!currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                        Replica current = currentReplicas.get(currentReplicaIndex);

                        if (current.isChoicePoint && !inChoiceMode) {
                            inChoiceMode = true;
                            choiceReplica = current;
                        }

                        if (!inChoiceMode) {
                            // обычный режим
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

                            // персонаж
                            if (currentReplicaIndex > 0) {
                                Replica previousReplica = currentReplicas.get(currentReplicaIndex - 2);
                                if (previousReplica.characterKey != null) {
                                    Rect charRect = characters.get(previousReplica.characterKey);
                                    if (charRect != null) {
                                        drawImage(charRect.imgKey, charRect, g);
                                    }
                                }
                            }

                            // режим выбора
                            Rect choice1 = replicas.get("chois1");
                            if (choice1 != null) {
                                drawImage("chois1", choice1, g);
                                drawChoiceText(g, choiceReplica.choiceText1, choice1.x, choice1.y + 40, choice1.w - 50);
                            }

                            Rect choice2 = replicas.get("chois2");
                            if (choice2 != null) {
                                drawImage("chois2", choice2, g);
                                drawChoiceText(g, choiceReplica.choiceText2, choice2.x, choice2.y + 40,choice2.w - 50);
                            }

                        }
                    }

                    drawImage("stop", buttons.get("back"), g);
                    drawImage("next", buttons.get("next"), g);
                    break;


            }


            if ( !currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                Replica current = currentReplicas.get(currentReplicaIndex);
                if (!current.isChoicePoint) {
                    drawReplica(g, current);
                }
            }
        }

        private void drawBackground(String defaultKey, Graphics g) {
            String bgKey = defaultKey;

            if (!backgroundChanges.isEmpty() && currentReplicaIndex >= 0) {   // Если есть события смены фона И текущий индекс не первый

                Integer lastChangeIndex = backgroundChanges.floorKey(currentReplicaIndex); // Находим последний индекс смены фона, который <= текущего
                if (lastChangeIndex != null) {
                    String newBg = backgroundChanges.get(lastChangeIndex);
                    if (backgrounds.containsKey(newBg)) {   // Применяем, только если такой фон существует в ресурсах

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