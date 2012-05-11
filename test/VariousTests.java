
import fixedfontocr.ContextualSearchTreeOCR;
import fixedfontocr.FixedFontOCR;
import fixedfontocr.SearchTreeOCR;
import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.Glyph;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class VariousTests {

   public static void main(String[] args) throws AWTException, IOException {
      //FontGlyph.setRenderingHints(new RenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)));

      //Font font = new Font("monospaced", Font.BOLD, 13);
      Font font = new Font("Verdana", Font.PLAIN, 16);
      //Font font = new Font("MS Reference Sans Serif", Font.PLAIN, 10);
      //Font font = new Font("MS Sans Serif", Font.PLAIN, 12); // does not exist use some default.
      //Font font = new Font("Microsoft Sans Serif", Font.PLAIN, 10);
      Color fontColor = Glyph.DEFAULT_FOREGROUND_COLOR;

      // Build the search nodes for recognizing the glyphs.
      List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
      SearchTreeOCR searchTree = new ContextualSearchTreeOCR(alphabet, font);

      testAllSymbolsRecognized(searchTree, alphabet);
      testSingleLine(searchTree, fontColor);
      testMultiLine(searchTree, fontColor);
      testSimplifiedMultiLine(fontColor);
   }

   protected static void testAllSymbolsRecognized(SearchTreeOCR searchTree, List<String> alphabet) {
      searchTree.checkIfAllSymbolsCanBeRecognized(alphabet);
      System.out.println("<<<\n\n");
   }

   protected static void testSingleLine(SearchTreeOCR searchTree, Color fontColor) {
      boolean doDisplayFluff = true;
      //String targetString = "j1lfajF_Lfpf*j&$j($)}{|/NOooI234'\"'ff";
      String targetString = "This is Verdana";
      BufferedImage image = FontGlyph.makeImage(targetString, searchTree.getFont(), doDisplayFluff);
      //Utilities.displayImage(image);
      //Utilities.displayImage(Utilities.enlargeImage(image, 7));
      //ImageIO.write(image, "png", new File("MS-Sans-Serif-12-Max.png"));

      String lineMatch =
              searchTree.detectCharactersOnOneLine(image, fontColor, new Point(0, 0));
      System.out.println("Target line:      " + targetString);
      System.out.println("Interpreted line: " + lineMatch);
      System.out.println("is " + (lineMatch.equals(targetString) ? "SUCCESS" : "FAIL"));
      System.out.println("<<<\n\n");
   }

   protected static void testMultiLine(SearchTreeOCR searchTree, Color fontColor) {
      int lineHeight = searchTree.getGlyphHeight() + 5;
      BufferedImage multiLineImage = generateMultiLineImage(searchTree.getFont(), lineHeight);
      //Utilities.displayImage(Utilities.enlargeImage(multiLineImage, 7));

      List<String> matchedLines =
              searchTree.detectCharactersOnMultipleLines(multiLineImage,
              fontColor, lineHeight, new Point(0, 0));
      for (String line : matchedLines)
         System.out.println(line);
      System.out.println("<<<\n\n");
   }

   /**
    * Same as above, but uses the simplified FixedFontOCR wrapper instead of
    * ContextualSearchTreeOCR.
    */
   protected static void testSimplifiedMultiLine(Color fontColor) {
      int lineHeight = 23;
      BufferedImage multiLineImage = 
              generateMultiLineImage(new Font("Verdana", Font.PLAIN, 16), lineHeight);

      FixedFontOCR ocr = new FixedFontOCR("Verdana", 16);
      String recognizedText = ocr.recognize(multiLineImage, fontColor, lineHeight);
      System.out.println(recognizedText);
      System.out.println("<<<\n\n");
   }

   protected static BufferedImage generateMultiLineImage(Font font, int lineHeight) {
      String targetString = "j1lfajF_Lfpf*j&$j($)}{|/NOooI234'\"'ff";
      List<String> lines = new ArrayList<>(Arrays.asList(targetString, "Some line...", " ...and another"));
      //List<String> lines = new ArrayList<>(Arrays.asList("This is Verdana with no pixels", "  between lines"));
      return FontGlyph.makeMultiLineImage(lines, font, lineHeight);
   }
}
