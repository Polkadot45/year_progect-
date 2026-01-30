package Size;
import java.util.Map;
import Game.Game;

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

        // Кнопки
        buttons.put("start", new Game.Rect("start", 815, 652, 245, 205));
        buttons.put("set", new Game.Rect("set", 565, 875, 250, 109));
        buttons.put("exit", new Game.Rect("exit", 1050, 882, 190, 109));
        buttons.put("back", new Game.Rect("stop", 1800, 30, 70, 70));
        buttons.put("next", new Game.Rect("next", 1500, 830, 420, 210));
        buttons.put("choice1", new Game.Rect("razvilka1", 500, 860, 387, 119));
        buttons.put("choice2", new Game.Rect("razvilka2", 950, 860, 387, 119));
        buttons.put("dot", new Game.Rect("dot", 605, 360, 23, 23));

        // Персонажи
        characters.put("serega", new Game.Rect("serega", 60, 300, 740, 740));
        characters.put("ivan", new Game.Rect("ivan", 100, 320, 720, 720));
        characters.put("larusa", new Game.Rect("larusaS", 1100, 355, 710, 710));
        characters.put("paratov", new Game.Rect("paratov", 150, 340, 750, 750));
        characters.put("mama", new Game.Rect("mama", 150, 370, 710, 710));

        // Реплики
        replicas.put("r1_1", new Game.Rect("r1_1", 535, 510, 800, 140));
        replicas.put("r1_2", new Game.Rect("r1_2", 590, 740, 772, 190));
    }
}
