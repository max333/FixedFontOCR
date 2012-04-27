package fixedfontocr;

import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import fixedfontocr.glyph.FontGlyph;

/**
 * Given a few columns of pixels, the search tree can figure out which branch (SearchNode) to
 * follow. The end of a branch corresponds to a Glyph.
 *
 * <p> See the documentation for SearchNode.
 */
public abstract class SearchTreeOCR {

   public final static int defaultPixelsBetweenLines = 0;
   protected List<FontGlyph> glyphs;
   protected int descent;
   protected int lineHeight;
   protected Font font;

   public SearchTreeOCR(List<String> alphabet, Font font) {
      this(FontGlyph.processAlphabet(alphabet, font));
   }

   public SearchTreeOCR(List<FontGlyph> glyphs) {
      this.glyphs = glyphs;
      if (glyphs.isEmpty())
         throw new IllegalArgumentException("Must have some glyphs.");
      this.descent = Math.round(glyphs.get(0).getLineMetrics().getDescent());

      lineHeight = glyphs.get(0).getDimension().height;
      font = glyphs.get(0).getFont();
      for (FontGlyph glyph : glyphs) {
         if (glyph.getDimension().height != lineHeight)
            throw new IllegalArgumentException("Expecting all glyphs to have the same height.");
         if (glyph.getFont() != font)
            throw new IllegalArgumentException("Expecting the font for all glyphs to be the same.");
      }
   }

   public abstract List<FontGlyph> detectGlyphsOnOneLine(BufferedImage image, Point topLeft);

   /**
    * @return null if detected nothing.
    */
   public String detectCharactersOnOneLine(BufferedImage image, Point topLeft) {
      StringBuilder stringBuilder = new StringBuilder();
      List<FontGlyph> glyphsOnLine = detectGlyphsOnOneLine(image, topLeft);
      if (glyphsOnLine == null)
         return null;
      for (FontGlyph glyph : glyphsOnLine)
         stringBuilder.append(glyph.getGeneratingString());
      return stringBuilder.toString();
   }

   /**
    * @return null if no match is found.
    */
   public List<String> detectCharactersOnMultipleLines(BufferedImage image, Point topLeft) {
      List<String> lines = new ArrayList<>();
      String line;
      int currentHeight = topLeft.y;
      while (currentHeight + lineHeight <= image.getHeight()) {
         Point topLeftCopy = new Point(topLeft.x, currentHeight);
         line = detectCharactersOnOneLine(image, topLeftCopy);
         if (line == null)
            break;
         lines.add(line);
         currentHeight += lineHeight + defaultPixelsBetweenLines;
      }
      return lines;
   }

   /**
    * @return all characters from ' ' to '~' in ascii ordering, which includes all letters and
    * numbers.
    */
   public static List<String> getDefaultAlphabet() {
      List<String> alphabet = new ArrayList<>();
      StringBuilder alphabetInSingleString = new StringBuilder();
      for (char c = ' '; c <= '~'; c++) {
         String cc = Character.valueOf(c).toString();
         alphabet.add(cc);
         alphabetInSingleString.append(cc);
      }
      return alphabet;
   }

   /**
    * Check if all symbols from the alphabet can be identified. I will fail if some symbols have the
    * same glyph.
    */
   public void checkIfAllSymbolsCanBeRecognized(Collection<String> alphabet) {
      for (String symbol : alphabet) {
         BufferedImage symbolImage = FontGlyph.makeImage(symbol, font);
         String matchString = detectCharactersOnOneLine(symbolImage, new Point(0, 0));
         if (!matchString.equals(symbol))
            //throw new AssertionError("Failed at matching " + symbol + " to its glyph.");
            Logger.getLogger(SearchTreeOCR.class.getName()).log(Level.INFO,
                    "Failed at matching " + symbol + " to its glyph. Likely due to many symbols having the same glyph.");
      }
      Logger.getLogger(SearchTreeOCR.class.getName()).log(Level.INFO,
              "Successfully matched all " + alphabet.size() + " symbols.");
   }

   /////////////////////////////////////////////////////////////////////////////////////////////
   /**
    * This should only be used for fonts which are guaranteed to never leak pixels out of their
    * bounding box.
    *
    * ContextualSearchTreeOCR should be used in general.
    */
   public static class NonContextual extends SearchTreeOCR {

      protected SearchNode headNode;

      public NonContextual(List<String> alphabet, Font font) {
         this(FontGlyph.processAlphabet(alphabet, font));
      }

      public NonContextual(List<FontGlyph> glyphs) {
         super(glyphs);
         this.headNode = new SearchNode(glyphs);
      }

      @Override
      public List<FontGlyph> detectGlyphsOnOneLine(BufferedImage image, Point topLeft) {
         List<FontGlyph> glyphsList = new ArrayList<>();
         FontGlyph match;
         while ((match = headNode.findLongestMatch(image, topLeft)) != null) {
            glyphsList.add(match);
            topLeft.x += match.getDimension().width;
         }
         return glyphsList;
      }
   }
}
