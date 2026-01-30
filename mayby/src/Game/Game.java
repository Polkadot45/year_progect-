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
        MENU, SETTINGS, INTRO, CAFE_1, CAFE_2, CAFE_CHOICE,
        CAFE_IVAN_PATH, CAFE_PARATOV_PATH,
        CAFE_IVAN_END, CAFE_PARATOV_END, CAFE_MAMA_END
    }

    private final Map<String, Rect> buttons = new HashMap<>();
    private final Map<String, Rect> backgrounds = new HashMap<>();
    private final Map<String, Rect> characters = new HashMap<>();
    private final Map<String, Rect> replicas = new HashMap<>();

    private Scene currentScene = Scene.MENU;
    private final MyImage imageManager = new MyImage();

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

    private void handleMouseClick(MouseEvent e) {
        int x = e.getX(), y = e.getY();

        if (isInside(x, y, buttons.get("back"))) {
            currentScene = Scene.MENU;
            return;
        }

        if ((isInside(x, y, buttons.get("exit"))) && currentScene==Scene.MENU) {
            System.exit(0);
        }

        switch (currentScene) {
            case MENU:
                if (isInside(x, y, buttons.get("set"))) currentScene = Scene.SETTINGS;
                else if (isInside(x, y, buttons.get("start"))) currentScene = Scene.INTRO;
                break;

            case INTRO:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_1;
                break;

            case CAFE_1:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_2;
                break;

            case CAFE_2:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_CHOICE;
                break;

            case CAFE_CHOICE:
                if (isInside(x, y, buttons.get("choice1"))) currentScene = Scene.CAFE_IVAN_PATH;
                else if (isInside(x, y, buttons.get("choice2"))) currentScene = Scene.CAFE_PARATOV_PATH;
                break;

            case CAFE_IVAN_PATH:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_IVAN_END;
                break;

            case CAFE_PARATOV_PATH:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_PARATOV_END;
                break;

            case CAFE_PARATOV_END,CAFE_IVAN_END:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.CAFE_MAMA_END;
                break;

            case CAFE_MAMA_END:
                if (isInside(x, y, buttons.get("next"))) currentScene = Scene.MENU;
                break;
        }
    }

    private boolean isInside(int mx, int my, Rect r) {
        return r != null && mx >= r.x && mx <= r.x + r.w && my >= r.y && my <= r.y + r.h;
    }

    class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            switch (currentScene) {
                case MENU:
                    drawBackground(g, "phon");
                    drawImage(g, "start", buttons.get("start"));
                    drawImage(g, "set", buttons.get("set"));
                    drawImage(g, "exit", buttons.get("exit"));
                    break;

                case SETTINGS:
                    drawBackground(g, "seti");
                    drawImage(g, "dot", buttons.get("dot"));
                    drawImage(g, "stop", buttons.get("back"));
                    break;

                case INTRO:
                    drawBackground(g, "people");
                    drawImage(g, "next", buttons.get("next"));
                    break;

                case CAFE_1:
                    drawCafe(g, null, "r1_1");
                    break;

                case CAFE_2, CAFE_IVAN_END, CAFE_IVAN_PATH:
                    drawCafe(g, "ivan", "r1_2");
                    break;

                case CAFE_CHOICE:
                    drawCafe(g, "larusa", null);
                    drawImage(g, "razvilka1", buttons.get("choice1"));
                    drawImage(g, "razvilka2", buttons.get("choice2"));
                    break;

                case CAFE_PARATOV_PATH:
                    drawCafe(g, "paratov", "r1_2");
                    break;

                case CAFE_PARATOV_END:
                    drawCafe(g,"ivan","r1_1" );

                case CAFE_MAMA_END:
                    drawCafe(g, "mama", "r1_2");
                    break;
            }
        }

        private void drawBackground(Graphics g, String key) {
            Rect bg = backgrounds.get(key);
            BufferedImage img = imageManager.getImage(bg.imgKey);
            g.drawImage(img, bg.x, bg.y, getWidth(), getHeight(), null);
        }

        private void drawImage(Graphics g, String imgKey, Rect r) {
            BufferedImage img = imageManager.getImage(imgKey);
            g.drawImage(img, r.x, r.y, r.w, r.h, null);
        }

        private void drawCafe(Graphics g, String charKey, String replicaKey) {
            drawBackground(g, "cafe");
            drawImage(g, "stop", buttons.get("back"));
            drawImage(g, "next", buttons.get("next"));

            if (charKey != null) {
                Rect r = characters.get(charKey);
                if (r != null) {
                    drawImage(g, r.imgKey, r);
                }
            }
            if (replicaKey != null) {
                Rect r = replicas.get(replicaKey);
                if (r != null) drawImage(g, replicaKey, r);
            }
        }
    }
}