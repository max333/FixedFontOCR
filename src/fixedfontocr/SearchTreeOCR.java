package fixedfontocr;

import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.Glyph;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Given a few columns of pixels, the search tree can figure out which branch (SearchNode) to
 * follow. The end of a branch corresponds to a Glyph.
 *
 * <p> See the documentation for SearchNode.
 */
public abstract class SearchTreeOCR {

   protected Font font;
   protected List<FontGlyph> fontGlyphs;
   protected int glyphHeight;

   public SearchTreeOCR(List<String> alphabet, Font font) {
      this(FontGlyph.buildGlyphsFromAlphabet(alphabet, font));
   }

   public SearchTreeOCR(List<FontGlyph> fontGlyphs) {
      this.fontGlyphs = fontGlyphs;
      if (fontGlyphs.isEmpty())
         throw new IllegalArgumentException("Must have some glyphs.");

      glyphHeight = fontGlyphs.get(0).getDimension().height;
      font = fontGlyphs.get(0).getFont();
      for (FontGlyph glyph : fontGlyphs) {
         if (glyph.getDimension().height != glyphHeight)
            throw new IllegalArgumentException("Expecting all glyphs to have the same height.");
         if (glyph.getFont() != font)
            throw new IllegalArgumentException("Expecting the font for all glyphs to be the same.");
      }
   }

   public abstract List<FontGlyph> detectGlyphsOnOneLine(BufferedImage image, Color fontColor, Point topLeft);

   /**
    * @return null if detected nothing.
    */
   public String detectCharactersOnOneLine(BufferedImage image, Color fontColor, Point topLeft) {
      StringBuilder stringBuilder = new StringBuilder();
      List<FontGlyph> glyphsOnLine = detectGlyphsOnOneLine(image, fontColor, topLeft);
      if (glyphsOnLine == null)
         return null;
      for (FontGlyph glyph : glyphsOnLine)
         stringBuilder.append(glyph.getGeneratingString());
      return stringBuilder.toString();
   }

   /**
    * @param topLeft must take the {@code lineHeight} into account: the top left corner of the 
    * actionable image includes the full height for the first line too.
    * @return null if no match is found.
    */
   public List<String> detectCharactersOnMultipleLines(BufferedImage image, Color fontColor,
           int lineHeight, Point topLeft) {
      List<String> lines = new ArrayList<>();
      // TODO should it be allowed to have nEmptyRowsBetweenLines < 0?
      int nEmptyRowsBetweenLines = lineHeight - glyphHeight;
      int currentHeight = topLeft.y;
      while (currentHeight + lineHeight <= image.getHeight()) {
         Point topLeftCopy = new Point(topLeft.x, currentHeight + nEmptyRowsBetweenLines);
         String line = detectCharactersOnOneLine(image, fontColor, topLeftCopy);
         if (line == null)
            break;
         lines.add(line);
         currentHeight += lineHeight;
      }
      return lines;
   }
   
   /**
    * Uses the default glyph height for the line height.
    */
    public List<String> detectCharactersOnMultipleLines(BufferedImage image, Color fontColor,
           Point topLeft) {
       return detectCharactersOnMultipleLines(image, fontColor, this.glyphHeight, topLeft);
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
    * </p>
    * Prints results to {@code System.out}.
    */
   public boolean checkIfAllSymbolsCanBeRecognized(Collection<String> alphabet) {
      boolean success = true;
      for (String symbol : alphabet) {
         BufferedImage symbolImage = FontGlyph.makeImage(symbol, font);
         String matchString = detectCharactersOnOneLine(symbolImage, Glyph.DEFAULT_FOREGROUND_COLOR, new Point(0, 0));
         if (!matchString.equals(symbol)) {
            success = false;
            //throw new AssertionError("Failed at matching " + symbol + " to its glyph.");
            System.out.println("Failed at matching " + symbol + " to its glyph. Likely due to many symbols having the same glyph.");
         }
      }
      System.out.println("Successfully matched all " + alphabet.size() + " symbols.");
      return success;
   }

   public Font getFont() {
      return font;
   }

   /**
    * The glyph height depends on how {@code FontGlyph} builds the fontGlyphs for a specified font.
    * All fontGlyphs for a specified font have the same height.
    */
   public int getGlyphHeight() {
      return glyphHeight;
   }

   /**
    * @return an unmodifiable list of the {@code FontGlyph}s.
    */
   public List<FontGlyph> getGlyphs() {
      return Collections.unmodifiableList(fontGlyphs);
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
         this(FontGlyph.buildGlyphsFromAlphabet(alphabet, font));
      }

      public NonContextual(List<FontGlyph> fontGlyphs) {
         super(fontGlyphs);
         this.headNode = new SearchNode(fontGlyphs);
      }

      @Override
      public List<FontGlyph> detectGlyphsOnOneLine(BufferedImage image, Color fontColor, Point topLeft) {
         List<FontGlyph> glyphsList = new ArrayList<>();
         FontGlyph match;
         while ((match = headNode.findLongestMatch(image, fontColor, topLeft)) != null) {
            glyphsList.add(match);
            topLeft.x += match.getDimension().width;
         }
         return glyphsList;
      }
   }
}
