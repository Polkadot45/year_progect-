package Game;

import java.io.*;
import java.util.*;
import java.awt.FontMetrics;

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

    public static java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
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

    private void parseChoiceLine(String line) {
        String params = line.substring(8).trim();
        String[] parts = params.split("\\|");
        if (parts.length == 6) { // Теперь 6 частей вместо 4
            String text1 = parts[0].trim();
            String file1 = parts[1].trim();
            int value1 = Integer.parseInt(parts[2].trim()); // Значение первого выбора
            String text2 = parts[3].trim();
            String file2 = parts[4].trim();
            int value2 = Integer.parseInt(parts[5].trim()); // Значение второго выбора
            currentReplicas.add(new Game.DialogueLine(0, 0, text1, file1, value1, text2, file2, value2));
        }
    }

    private void parseBackgroundChange(String line) {
        String normalized = line.toLowerCase().replace(" ", "");
        if (!normalized.startsWith("#bg:")) {
            System.err.println("Ошибка: строка не распознана как #bg: " + line);
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