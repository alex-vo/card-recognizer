package org.example;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Solution {

    static final int RANK_OFFSET_X = 145;
    static final int RANK_OFFSET_Y = 589;
    static final int CARD_WIDTH = 72;
    static final int RANK_WIDTH = 35;
    static final int RANK_HEIGHT = 28;
    static final int SUIT_OFFSET_X = 166;
    static final int SUIT_OFFSET_Y = 632;
    static final int SUIT_WIDTH = 36;
    static final int SUIT_HEIGHT = 37;
    static final int WHITE_PIXEL_OFFSET_X = 40;
    static final int WHITE_PIXEL_OFFSET_Y = 10;

    static final Set<Character> RANK_CHARS = Set.of('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'J', 'Q', 'K', 'A');

    private static final Map<String, char[][]> dict = new HashMap<>();

    static {
        initDictionary();
    }

    public static String getCard(BufferedImage img, int index) {
        String rank = getFromDictionary(getRankSample(img, index));
        String suit = getFromDictionary(getSuitSample(img, index));

        return rank + suit;
    }

    public static boolean cardExists(BufferedImage img, int index) {
        int rgb = img.getRGB(RANK_OFFSET_X + CARD_WIDTH * index + WHITE_PIXEL_OFFSET_X, RANK_OFFSET_Y + WHITE_PIXEL_OFFSET_Y);

        return isWhite(rgb);
    }

    public static char[][] getRankSample(BufferedImage img, int index) {
        try {
            return toBinaryImage(img.getSubimage(RANK_OFFSET_X + index * CARD_WIDTH, RANK_OFFSET_Y, RANK_WIDTH, RANK_HEIGHT));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to get rank segment %d", index), e);
        }
    }

    public static char[][] getSuitSample(BufferedImage img, int index) {
        try {
            return toBinaryImage(img.getSubimage(SUIT_OFFSET_X + index * CARD_WIDTH, SUIT_OFFSET_Y, SUIT_WIDTH, SUIT_HEIGHT));
        } catch (Exception e) {
            throw new RuntimeException(String.format("Failed to get suit segment %d", index), e);
        }
    }

    public static char[][] overlap(char[][] a, char[][] b) {
        char[][] res = new char[a.length][a[0].length];
        for (int y = 0; y < a.length; y++) {
            for (int x = 0; x < a[y].length; x++) {
                if (a[y][x] == 'x' && b[y][x] == 'x') {
                    res[y][x] = 'x';
                } else {
                    res[y][x] = ' ';
                }
            }
        }
        return res;
    }

    public static boolean contains(char[][] pattern, char[][] sample) {
        for (int y = 0; y < pattern.length; y++) {
            for (int x = 0; x < pattern[y].length; x++) {
                if (pattern[y][x] == 'x' && sample[y][x] != 'x') {
                    return false;
                }
            }
        }

        return true;
    }

    public static void flush(String filePath, char[][] c) throws Exception {
        String s = toReadableString(c);

        Files.writeString(Path.of(filePath), s);
    }

    public static String toReadableString(char[][] c) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < c.length; y++) {
            for (int x = 0; x < c[y].length; x++) {
                sb.append(c[y][x]);
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private static void initDictionary() {
        try {
            File[] imgFiles = new File("src/main/resources/patterns").listFiles();
            if (imgFiles == null) return;
            for (File imgFile : imgFiles) {
                String k = imgFile.getName().split("\\.")[0];
                char[][] v = readContent(imgFile);
                dict.put(k, v);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize pattern dictionary", e);
        }
    }

    private static boolean isWhite(int rgb) {
        return rgb == -1 || rgb == -8882056;
    }

    private static String getFromDictionary(char[][] sample) {
        return dict.entrySet().stream()
                .filter(e -> contains(e.getValue(), sample))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }

    private static char[][] toBinaryImage(BufferedImage img) {
        char[][] res = new char[img.getHeight()][img.getWidth()];
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                res[y][x] = isWhite(rgb) ? ' ' : 'x';
            }
        }
        return res;
    }

    private static char[][] readContent(File f) throws IOException {
        List<String> lines = Files.readAllLines(f.toPath());
        char[][] result = new char[lines.size()][];
        for (int i = 0; i < lines.size(); i++) {
            result[i] = lines.get(i).toCharArray();
        }

        return result;
    }

    private static void processSample(String key, char[][] newSample) {
        char[][] pattern = dict.get(key);
        if (pattern == null) {
            dict.put(key, newSample);
        } else {
            dict.put(key, overlap(pattern, newSample));
        }
    }

    private static void buildSamples() throws Exception {
        File imgDir = new File("img");
        File[] files = imgDir.listFiles();
        for (File imgFile : files) {
            char[] cards = imgFile.getName().split("\\.")[0].toCharArray();
            int cardIndex = 0;
            for (int i = 0; i < cards.length; cardIndex++) {
                String rank = "";
                for (; ; i++) {
                    if (!RANK_CHARS.contains(cards[i])) break;
                    rank += cards[i];
                }
                BufferedImage img = ImageIO.read(new File("img/" + imgFile.getName()));
                processSample(rank, getRankSample(img, cardIndex));

                String suit = "" + cards[i++];
                processSample(suit, getSuitSample(img, cardIndex));
            }
        }

        for (Map.Entry<String, char[][]> e : dict.entrySet()) {
            flush(String.format("src/main/resources/patterns/%s.txt", e.getKey()), e.getValue());
        }
    }

    private static String processFile(File file) throws Exception {
        BufferedImage img = ImageIO.read(file);
        StringBuilder result = new StringBuilder();
        result.append(file.getName()).append(" - ");
        for (int i = 0; i < 5; i++) {
            if (!cardExists(img, i)) break;

            result.append(getCard(img, i)).append(" ");
        }

        return result.toString();
    }

    private static void init() throws Exception {
        File patternsDir = new File("src/main/resources/patterns");
        if (!patternsDir.exists()) {
            patternsDir.mkdir();
        }
        File[] patternFiles = patternsDir.listFiles();
        if (patternFiles.length < 17) {
            for (File patternFile : patternFiles) {
                patternFile.delete();
            }
            buildSamples();
            initDictionary();
        }
    }

    public static void main(String[] args) throws Exception {
        init();

        File fileDir = new File(args[0]);
        if (!fileDir.exists() || !fileDir.isDirectory() || fileDir.listFiles() == null) {
            throw new IllegalArgumentException(String.format("Failed to read image files from directory %s", args[0]));
        }

        for (File file : fileDir.listFiles()) {
            System.out.println(processFile(file));
        }
    }
}
