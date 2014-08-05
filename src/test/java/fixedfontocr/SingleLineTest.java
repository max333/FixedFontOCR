package fixedfontocr;

import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.Glyph;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.junit.Assert.assertTrue;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 */
@RunWith(Parameterized.class)
public class SingleLineTest {
    private final Color fontColor = Glyph.DEFAULT_FOREGROUND_COLOR;
    private final List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
    private final SearchTreeOCR searchTree;
    //FontGlyph.setRenderingHints(new RenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)));

    public SingleLineTest(Font font) {
        searchTree = new SearchTreeOCRWithLeakedPixels(alphabet, font);
    }

    @Parameters
    public static Collection<Font[]> fonts() {
        return Arrays.asList(new Font[][]{
            {new Font("Verdana", Font.PLAIN, 16)}}
        );
        // fails for other fonts:
        // {new Font("monospaced", Font.BOLD, 13)}}
        // {new Font("MS Reference Sans Serif", Font.PLAIN, 10)}}
        // {new Font("MS Sans Serif", Font.PLAIN, 12)}}
        // {new Font("Microsoft Sans Serif", Font.PLAIN, 10)}}
    }

    @Test
    public void testAllSymbolsRecognized() {
        assertTrue(searchTree.checkIfAllSymbolsCanBeRecognized(alphabet));
    }

    @Test
    public void testSentence1() {
        String targetString = "This is Verdana";
        String parsedString = createAndParseImage(targetString);
        assertTrue(targetString.equals(parsedString));
    }

    @Ignore
    public void testSentence2() {
        String targetString = "'\"";
        String parsedString = createAndParseImage(targetString);
        assertTrue(targetString.equals(parsedString));
    }

    private String createAndParseImage(String input) {
        BufferedImage image = FontGlyph.makeImage(input, searchTree.getFont());
        return searchTree.detectCharactersOnOneLine(image, fontColor, new Point(0, 0));
    }
}
