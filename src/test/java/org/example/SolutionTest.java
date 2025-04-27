package org.example;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

import static org.example.Solution.RANK_CHARS;
import static org.example.Solution.cardExists;
import static org.example.Solution.getCard;
import static org.example.TestUtils.assertTrue;

public class SolutionTest {

    @Test
    public void test_getCard_ShouldRecognizeCardsOnImages() throws Exception {
        File[] imgFiles = new File("img").listFiles();
        for (File img : imgFiles) {
            testImg(img);
        }
    }

    private void testImg(File file) throws Exception {
        BufferedImage img = ImageIO.read(file);
        char[] cardNames = file.getName().split("\\.")[0].toCharArray();
        int cardIndex = 0;
        for (int i = 0; i < cardNames.length; cardIndex++) {
            String rank = "";
            for (; ; i++) {
                if (RANK_CHARS.contains(cardNames[i])) {
                    rank += cardNames[i];
                } else {
                    break;
                }
            }
            char suit = cardNames[i++];

            assertTrue(cardExists(img, cardIndex));
            assertTrue((rank + suit).equals(getCard(img, cardIndex)));
        }
    }

}
