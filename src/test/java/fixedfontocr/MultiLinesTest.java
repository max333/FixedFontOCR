package fixedfontocr;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.Glyph;
import fixedfontocr.utilities.Utilities;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class MultiLinesTest {
    private final List<String> lines = new ArrayList<>(Arrays.asList(
            "j1lfajF_Lfpf*j&$j($)}{|/NOooI234\"'ff",
            "Some line...",
            " ...and another"));
    private final Font font = new Font("Verdana", Font.PLAIN, 16);
    private final Color fontColor = Glyph.DEFAULT_FOREGROUND_COLOR;
    private final List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
    private final SearchTreeOCR searchTree = new SearchTreeOCRWithLeakedPixels(alphabet, font);
    private final int lineHeight = searchTree.getGlyphHeight() + 5;
    private final BufferedImage image = FontGlyph.makeMultiLineImage(lines, font, lineHeight);

    @Test
    public void testMultiLine() {
        List<String> parsedLines = searchTree.detectCharactersOnMultipleLines(image,
                fontColor, lineHeight, new Point(0, 0));
        List<String> parsedLinesNoTrail = removeTrailingSpaces(parsedLines);
        Assert.assertEquals(parsedLinesNoTrail, lines);
    }

    /**
     * Same as above, but uses the simplified FixedFontOCR wrapper instead of
     * SearchTreeOCRWithLeakedPixels.
     */
    @Test
    public void testSimplifiedMultiLine() {
        FixedFontOCR ocr = new FixedFontOCR("Verdana", 16);
        String recognizedText = ocr.recognize(image, fontColor, lineHeight);
        String[] parsedLinesArray = recognizedText.split("\n");
        List<String> parsedLines = new ArrayList<>(parsedLinesArray.length);
        for (String line : parsedLinesArray)
            parsedLines.add(Utilities.removeTrailingSpaces(line));
        Assert.assertEquals(parsedLines, lines);
    }
    
    private List<String> removeTrailingSpaces(List<String> inputs) {
        return inputs.stream()
                .map(Utilities::removeTrailingSpaces)
                .collect(Collectors.toList());        
    }
}
