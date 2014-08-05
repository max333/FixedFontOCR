package fixedfontocr;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test on test-verdana-16.png, which was generated from Photoshop.
 */
public class TestPhotoshopImageText {
    private static SampleImage sampleImage;

    @BeforeClass
    public static void readImageFile() throws IOException {
        sampleImage = new SampleImage();
    }

    @Test
    public void testVerdanaImage() throws IOException {
        List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
        SearchTreeOCR searchTree = new SearchTreeOCRWithLeakedPixels(alphabet, sampleImage.getFont());
        List<String> parsedLines = searchTree.detectCharactersOnMultipleLines(sampleImage.getImage(), Color.BLACK, new Point(0, 0));
        assertTrue(areMatchingTrimmedLines(sampleImage.getTargetLines(), parsedLines));
    }

    private boolean areMatchingTrimmedLines(List<String> list1, List<String> list2) {
        if (list1.size() != list2.size()) {
            return false;
        } else {
            return IntStream.range(0, list1.size())
                    .allMatch(index -> list1.get(index).trim().equals(list2.get(index).trim()));
        }
    }

    @Test
    public void testSpaceCharacterAtLineBeginning() {
        String secondTargetLineWithTwoLeadingSpace = " " + " " + sampleImage.getTargetLines().get(1);
        
         List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
        SearchTreeOCR searchTree = new SearchTreeOCRWithLeakedPixels(alphabet, sampleImage.getFont());
        List<String> parsedLines = searchTree.detectCharactersOnMultipleLines(sampleImage.getImage(), Color.BLACK, new Point(0, 0));
        String secondLineFromImage = parsedLines.get(1);
        System.out.println(">>" + secondLineFromImage);
        System.out.println(">>" + secondTargetLineWithTwoLeadingSpace);
        System.out.println(">>" + parsedLines.get(0));

        assertTrue(secondLineFromImage.startsWith(secondTargetLineWithTwoLeadingSpace));
    }
}
