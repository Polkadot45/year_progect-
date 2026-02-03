package Game;
import Size.Size;
import MyImage.MyImage;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
        //1 явление
        CAFE_1, CAFE_2, CAFE_3, CAFE_4, CAFE_5, CAFE_6, CAFE_7, CAFE_8,CAFE_9,CAFE_10, CAFE_11,CAFE_12,
        //2 явление
        CAFE_13,CAFE_14,CAFE_15,
    }

    private final Map<String, Rect> buttons = new HashMap<>();
    private final Map<String, Rect> backgrounds = new HashMap<>();
    private final Map<String, Rect> characters = new HashMap<>();
    private final Map<String, Rect> replicas = new HashMap<>();

    private Scene currentScene = Scene.MENU;
    private final MyImage imageManager = new MyImage();

    private List<Replica> currentReplicas = new ArrayList<>();
    private int currentReplicaIndex = 0;

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

        public Replica(String text, int x, int y) {
            this.text = text;
            this.x = x;
            this.y = y;
        }
    }

    private void handleMouseClick(MouseEvent e) {
        int x = e.getX(), y = e.getY();

        if (isInside(x, y, buttons.get("back"))) {
            currentScene = Scene.MENU;
            currentReplicas.clear();
            currentReplicaIndex = 0;
            return;
        }

        if ((isInside(x, y, buttons.get("exit"))) && currentScene==Scene.MENU) {
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
                    currentScene = Scene.CAFE_1;
                    loadScenario("1_1.txt");
                    currentReplicaIndex = 0;
                }
                break;
//            case CAFE_CHOICE:
//                if (isInside(x, y, buttons.get("choice1"))) currentScene = Scene.CAFE_IVAN_PATH;
//                else if (isInside(x, y, buttons.get("choice2"))) currentScene = Scene.CAFE_PARATOV_PATH;
//                break;

            //1 часть
            //1 явление
            case CAFE_1:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_2;
                break;

            case CAFE_2:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_3;
                break;

            case CAFE_3:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_4;
                break;

            case CAFE_4:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_5;
                break;

            case CAFE_5:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_6;
                break;

            case CAFE_6:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_7;
                break;

            case CAFE_7:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_8;
                break;

            case CAFE_8:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_9;
                break;
            case CAFE_9:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_10;
                break;
            case CAFE_10:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_11;
                break;
            case CAFE_11:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_12;
                break;

            //2 явление
            case CAFE_12:
                if (isInside(x, y, buttons.get("next"))) {
                    currentReplicas.clear();
                    currentReplicaIndex = 0;
                }
                break;
        }

        if (isCafeScene(currentScene) && !currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size() - 1) {
            currentReplicaIndex++;
        }
    }

    private boolean isCafeScene(Scene scene) {
        return scene.ordinal() >= Scene.CAFE_1.ordinal() && scene.ordinal() <= Scene.CAFE_11.ordinal();
        //ordinal() — это метод Java для перечислений (enum), который возвращает порядковый номер константы в её объявлении, начиная с 0.
    }

    private boolean isInside(int mx, int my, Rect r) {
        return r != null && mx >= r.x && mx <= r.x + r.w && my >= r.y && my <= r.y + r.h;
    }

    private void loadScenario(String filename) {
        try (InputStream is = getClass().getResourceAsStream("/" + filename)) {
            if (is == null) {
                System.out.println("Файл не найден: " + filename);
                return;
            }
            //InputStreamReader преобразует байты в символы
            //UTF-8 - способ превратить буквы, цифры и символы в последовательность байтов (нулей и единиц), которые компьютер может хранить и передавать.
            //BufferedReader ускоряет чтение по строкам
            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            currentReplicas.clear();

            while ((line = br.readLine()) != null) {
                line = line.trim(); //убирает пробелы
                if (line.isEmpty()) continue; // пропускать пустые строки

                if (line.startsWith("#")) {
                    int spaceIndex = line.indexOf(' ', 1); //Ищем первый пробел после #
                    if (spaceIndex > 0) {
                        String coordPart = line.substring(1, spaceIndex).trim();
                        String text = line.substring(spaceIndex + 1).trim();

                        text = text.replace("|", "\n");

                        //координаты
                        String[] coords = coordPart.split(",");
                        if (coords.length == 2) {
                            try {
                                int x = Integer.parseInt(coords[0].trim());
                                int y = Integer.parseInt(coords[1].trim());
                                currentReplicas.add(new Replica(text, x, y));
                                continue;
                            } catch (NumberFormatException e) {
                                System.err.println("Неверный формат координат в строке: " + line);
                            }
                        }
                    }
                }
                System.err.println("Пропущена некорректная строка сценария: " + line);
            }
            currentReplicaIndex = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawReplica(Graphics g, Replica replica) {
        Graphics2D g2d = (Graphics2D) g.create(); //создание копии чтобы исходный текст не портился

        Font font = new Font("Arial", Font.PLAIN, 35);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        FontMetrics fm = g2d.getFontMetrics(); //объект с информацией о шрифте
        int lineHeight = fm.getHeight();

        String[] lines = replica.text.split("\n");

        for (int i = 0; i < lines.length; i++) {
            int yLine = replica.y + (i + 1) * lineHeight - fm.getDescent();
            g2d.drawString(lines[i].trim(), replica.x, yLine);
        }
        g2d.dispose();//Уничтожает копию контекста рисования
    }

    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            switch (currentScene) {
                case MENU:
                    drawBackground("phon",g);
                    drawImage("start", buttons.get("start"), g);
                    drawImage("set", buttons.get("set"), g);
                    drawImage("exit", buttons.get("exit"), g);
                    break;

                case SETTINGS:
                    drawBackground("seti",g);
                    drawImage( "dot", buttons.get("dot"), g);
                    drawImage( "stop", buttons.get("back"), g);
                    break;

                case INTRO:
                    drawBackground("people", g);
                    drawImage("next", buttons.get("next"), g);
                    break;
//                case CAFE_CHOICE:
//                    drawCafe(g, "larusa", null);
//                    drawImage(g, "razvilka1", buttons.get("choice1"));
//                    drawImage(g, "razvilka2", buttons.get("choice2"));
//                    break;

                //1 часть
                //1 явление
                case CAFE_1:
                    drawCafe( null, "replica_s",g);
                    break;

                case CAFE_2, CAFE_4, CAFE_7, CAFE_10:
                    drawCafe("ivan", "replica_d",g);
                    break;

                case CAFE_3, CAFE_5,CAFE_6, CAFE_8,CAFE_9,CAFE_11:
                    drawCafe("gavrilo", "replica_d",g);
                    break;
            }


            if (isCafeScene(currentScene) && !currentReplicas.isEmpty() && currentReplicaIndex < currentReplicas.size()) {
                drawReplica(g, currentReplicas.get(currentReplicaIndex));
            }
        }

        private void drawBackground(String key,Graphics g) {
            Rect bg = backgrounds.get(key);
            if (bg == null) return;
            BufferedImage img = imageManager.getImage(bg.imgKey);
            g.drawImage(img, bg.x, bg.y, getWidth(), getHeight(), null);
        }

        private void drawImage(String imgKey,Rect r,Graphics g) {
            if (r == null) return;
            BufferedImage img = imageManager.getImage(imgKey);
            g.drawImage(img, r.x, r.y, r.w, r.h, null);
        }

        private void drawCafe(String charKey, String replicaKey, Graphics g) {
            drawBackground("cafe",g);
            if (charKey != null) {
                Rect r = characters.get(charKey);
                if (r != null) {
                    drawImage(r.imgKey, r, g);
                }
            }
            if (replicaKey != null) {
                Rect r = replicas.get(replicaKey);
                if (r != null) drawImage(replicaKey, r, g);
            }
            drawImage("stop", buttons.get("back"), g);
            drawImage("next", buttons.get("next"), g);
        }
    }
}