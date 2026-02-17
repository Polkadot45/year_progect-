package Size;
import java.util.Map;
import Game.Game;
import java.io.File;

public class Size {
    public static void initSize(
            Map<String, Game.Rect> backgrounds,
            Map<String, Game.Rect> buttons,
            Map<String, Game.Rect> characters,
            Map<String, Game.Rect> replicas
    ) {
        // Фоны
        backgrounds.put("phon", new Game.Rect("phon", 0, 0, 1920, 1080));
        backgrounds.put("people", new Game.Rect("people", 0, 0, 1920, 1080));
        backgrounds.put("seti", new Game.Rect("seti", 0, 0, 1920, 1080));
        backgrounds.put("cafe", new Game.Rect("cafe", 0, 0, 1920, 1080));
        backgrounds.put("room", new Game.Rect("room", 0, 0, 1920, 1080));

        // Кнопки
        buttons.put("start", new Game.Rect("start", 815, 652, 245, 205));
        buttons.put("set", new Game.Rect("set", 565, 875, 250, 109));
        buttons.put("exit", new Game.Rect("exit", 1050, 882, 190, 109));
        buttons.put("back", new Game.Rect("stop", 1800, 30, 70, 70));
        buttons.put("next", new Game.Rect("next", 1660, 900,240 , 150));
        buttons.put("choice1", new Game.Rect("razvilka1", 500, 860, 387, 119));
        buttons.put("choice2", new Game.Rect("razvilka2", 950, 860, 387, 119));
        buttons.put("dot", new Game.Rect("dot", 605, 360, 23, 23));

        // Персонажи
        characters.put("serega", new Game.Rect("serega", -100, 240, 860, 860));
        characters.put("knurov", new Game.Rect("knurov", 1050, 200, 900, 900));
        characters.put("ivan", new Game.Rect("ivan", -100, 240, 860, 860));
        characters.put("larusaS", new Game.Rect("larusaS", 1050, 200, 850, 850));
        characters.put("paratov", new Game.Rect("paratov", -100, 240, 860, 860));
        characters.put("mama", new Game.Rect("mama", -100, 240, 860, 860));
        characters.put("gavrilo", new Game.Rect("gavrilo", 1000, 200, 900, 900));
        characters.put("karandashev", new Game.Rect("karandashev", -100, 240, 870, 870));

        // Реплики
        replicas.put("replica_s", new Game.Rect("replica_s", 270, 270, 1400, 550));
        replicas.put("replica_d", new Game.Rect("replica_d", 220, 615, 1400, 550));
        replicas.put("chois1", new Game.Rect("chois1", 220, 800, 600, 200));
        replicas.put("chois2", new Game.Rect("chois2", 900, 800, 600, 200));
    }
}
