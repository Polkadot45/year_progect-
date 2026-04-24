package Game;
import java.io.*;
import java.util.*;
import java.awt.FontMetrics;

public class Replica {

    private final Game game; // Ссылка на основной класс игры (нужна для доступа к ресурсам)
    private java.util.List<Game.DialogueLine> currentReplicas = new java.util.ArrayList<>(); //Список всех реплик текущего сценария
    private TreeMap<Integer, String> backgroundChanges = new TreeMap<>();

    public Replica(Game game) { //ссылка на основной класс игры
        this.game = game;
    }

    public boolean loadScenario(String filename) {

        // Очищаем предыдущие данные
        currentReplicas.clear();
        backgroundChanges.clear();

        try (
                InputStream is = game.getClass().getResourceAsStream("/" + filename) // Загружаем файл как ресурс из classpath
        ) {
            if (is == null) {
                System.err.println("Файл не найден: " + filename);
                return false;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8")); // Создаём читатель с поддержкой UTF-8 (для русского текста)
            String line;
            int lineNumber = 0;

            while ((line = br.readLine()) != null) { // Читаем файл построчно
                lineNumber++;
                line = line.trim(); // Убираем пробелы по краям
                if (line.isEmpty()) continue; // Пропускаем пустые строки

                // Определяем тип строки по её началу
                if (line.startsWith("#choice:") || line.toLowerCase().startsWith("#choice:")) {
                    parseChoiceLine(line); // Обработка точки выбора
                } else if (line.toLowerCase().startsWith("#bg:")) {
                    parseBackgroundChange(line); // Обработка смены фона
                } else if (line.startsWith("#") && !line.toLowerCase().startsWith("#bg:")) {
                    parseReplicaLine(line); // Обработка обычной реплики
                } else {
                    System.err.println("Пропущена строка " + lineNumber + ": " + line); // Если строка не распознана — выводим предупреждение
                }
            }
            return true; // Успешная загрузка
        } catch (IOException e) {
            e.printStackTrace(); // Обработка ошибок ввода-вывода
            return false;
        }
    }

    private void parseReplicaLine(String line) {
        int colonIndex = line.indexOf(':', 1); // Находим первый символ ':' после начала строки (начиная с позиции 1)
        if (colonIndex > 0) {
            String charPart = line.substring(1, colonIndex).trim(); // Извлекаем имя персонажа (между '#' и ':')
            String rest = line.substring(colonIndex + 1).trim(); // Извлекаем всё после ':'

            int spaceIndex = rest.indexOf(' ');// Находим первый пробел в остатке (разделяет координаты и текст)
            if (spaceIndex > 0) {
                String coordPart = rest.substring(0, spaceIndex).trim();// Извлекаем координаты (до первого пробела)
                String text = rest.substring(spaceIndex + 1).trim();// Извлекаем текст (после первого пробела)

                String[] coords = coordPart.split(",");// Разделяем координаты по запятой
                if (coords.length == 2) {
                    try {
                        // Преобразуем координаты в числа
                        int x = Integer.parseInt(coords[0].trim());
                        int y = Integer.parseInt(coords[1].trim());
                        String charKey = charPart.isEmpty() ? null : charPart;
                        currentReplicas.add(new Game.DialogueLine(text, x, y, charKey)); // Создаём и добавляем новую реплику в список
                    } catch (NumberFormatException e) {
                        System.err.println("Неверный формат координат: " + line);
                    }
                }
            }
        }
    }

    public static java.util.List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        String[] words = text.split(" "); // Разбиваем текст на слова по пробелам
        if (words.length == 0) return lines;

        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (fm.stringWidth(word) > maxWidth) { // Если слово само по себе шире maxWidth
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString()); // Сохраняем текущую строку (если она не пустая)
                    currentLine = new StringBuilder();
                }

                // Разбиваем слишком длинное слово по символам
                StringBuilder chunk = new StringBuilder();
                for (char c : word.toCharArray()) {
                    String test = chunk.toString() + c;
                    if (fm.stringWidth(test) > maxWidth && chunk.length() > 0) { // Если добавление символа превышает maxWidth
                        lines.add(chunk.toString()); // Сохраняем часть слова
                        chunk = new StringBuilder(String.valueOf(c)); // Начинаем новую часть
                    } else {
                        chunk.append(c);
                    }
                }
                if (chunk.length() > 0) currentLine.append(chunk);
                continue;
            }

            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word; // Проверяем, помещается ли слово в текущую строку
            if (fm.stringWidth(testLine) > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString()); // Текущая строка полная — сохраняем её
                currentLine = new StringBuilder(word); // Начинаем новую строку
            } else {
                if (currentLine.length() > 0) currentLine.append(" ");// Добавляем слово к текущей строке
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) lines.add(currentLine.toString());// Сохраняем последнюю строку (если она не пустая)
        return lines;
    }

    private void parseChoiceLine(String line) {
        String params = line.substring(8).trim(); // Извлекаем параметры после "#choice:"
        String[] parts = params.split("\\|");// Разделяем параметры по вертикальной черте
        if (parts.length == 6) {
            String text1 = parts[0].trim(); // Текст первого варианта
            String file1 = parts[1].trim(); // Файл для первого варианта
            int value1 = Integer.parseInt(parts[2].trim()); // Очки за первый вариант
            String text2 = parts[3].trim(); // Текст второго варианта
            String file2 = parts[4].trim(); // Файл для второго варианта
            int value2 = Integer.parseInt(parts[5].trim()); // Очки за второй вариант

            // Создаём и добавляем точку выбора в список реплик
            currentReplicas.add(new Game.DialogueLine(0, 0, text1, file1, value1, text2, file2, value2));
        }
    }

    private void parseBackgroundChange(String line) {
        String normalized = line.toLowerCase().replace(" ", "");// Нормализуем строку: приводим к нижнему регистру и убираем пробелы
        String bgKey = normalized.substring(4).trim();// Извлекаем имя фона (после "#bg:")
        // Запоминаем, что с этой реплики (currentReplicas.size()) нужно менять фон
        int index = currentReplicas.size();
        backgroundChanges.put(index, bgKey);
    }

    public java.util.List<Game.DialogueLine> getCurrentReplicas() {
        return currentReplicas;
    }

    public TreeMap<Integer, String> getBackgroundChanges() {
        return backgroundChanges;
    }
}