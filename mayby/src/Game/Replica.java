package Game;

import java.io.*;
import java.util.*;

public class Replica {
    private final Game game;
    private java.util.List<Game.DialogueLine> currentReplicas = new java.util.ArrayList<>();
    private TreeMap<Integer, String> backgroundChanges = new TreeMap<>();

    public Replica(Game game) {
        this.game = game;
    }

    public boolean loadScenario(String filename) {
        currentReplicas.clear();
        backgroundChanges.clear();

        try (InputStream is = game.getClass().getResourceAsStream("/" + filename)) {
            if (is == null) {
                System.err.println("Файл не найден: " + filename);
                return false;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("#choice:") || line.toLowerCase().startsWith("#choice:")) {
                    parseChoiceLine(line);
                } else if (line.toLowerCase().startsWith("#bg:")) {
                    parseBackgroundChange(line);
                } else if (line.startsWith("#") && !line.toLowerCase().startsWith("#bg:")) {
                    parseReplicaLine(line);
                } else {
                    System.err.println("Пропущена строка " + lineNumber + ": " + line);
                }
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void parseReplicaLine(String line) {
        int colonIndex = line.indexOf(':', 1);
        if (colonIndex > 0) {
            String charPart = line.substring(1, colonIndex).trim();
            String rest = line.substring(colonIndex + 1).trim();

            int spaceIndex = rest.indexOf(' ');
            if (spaceIndex > 0) {
                String coordPart = rest.substring(0, spaceIndex).trim();
                String text = rest.substring(spaceIndex + 1).trim();
                text = text.replace("|", " ");

                String[] coords = coordPart.split(",");
                if (coords.length == 2) {
                    try {
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        String charKey = charPart.isEmpty() ? null : charPart;
                        currentReplicas.add(new Game.DialogueLine(text, x, y, charKey));
                    } catch (NumberFormatException e) {
                        System.err.println("Неверный формат координат: " + line);
                    }
                }
            }
        }
    }

    private void parseChoiceLine(String line) {
        if (line.startsWith("#CHOICE:")) {
            line = "#choice:" + line.substring(8);
        }

        String params = line.substring(8).trim();
        String[] parts = params.split("\\|");
        if (parts.length == 4) {
            String text1 = parts[0].trim();
            String file1 = parts[1].trim();
            String text2 = parts[2].trim();
            String file2 = parts[3].trim();
            currentReplicas.add(new Game.DialogueLine(0, 0, text1, file1, text2, file2));
        }
    }

    private void parseBackgroundChange(String line) {
        String normalized = line.toLowerCase().replace(" ", "");
        if (!normalized.startsWith("#bg:")) {
            System.err.println("❌ Ошибка: строка не распознана как #bg: " + line);
            return;
        }
        String bgKey = normalized.substring(4).trim();
        int index = currentReplicas.size();
        backgroundChanges.put(index, bgKey);
    }

    public java.util.List<Game.DialogueLine> getCurrentReplicas() {
        return currentReplicas;
    }

    public TreeMap<Integer, String> getBackgroundChanges() {
        return backgroundChanges;
    }

    public void clear() {
        currentReplicas.clear();
        backgroundChanges.clear();
    }
}