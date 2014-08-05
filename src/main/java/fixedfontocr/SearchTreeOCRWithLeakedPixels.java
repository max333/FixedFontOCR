package fixedfontocr;

import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.FontGlyphShiftedLeft;
import fixedfontocr.glyph.FontGlyphWithLeakedPixels;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some glyphs are recognized as ContextualFontGlyphs, which mean that those cannot appear anywhere
 * on a line, but must have some specific adjacent FontGlyph on the right, or left, or both.
 */
public class SearchTreeOCRWithLeakedPixels extends SearchTreeOCR {

   protected Set<FontGlyph> standardStartAlphabet;
   protected Set<FontGlyph> startOfLineAlphabet;
   protected Map<Set<FontGlyph>, SearchNode> nodesCache = new HashMap<>();

   public SearchTreeOCRWithLeakedPixels(List<String> alphabet, Font font) {
      this(alphabet, font, true);
   }

   /**
    * 
    * @param doAddLeftShiftedFontGlyphs if true, will also include the FontGlyphShiftedLeft for each letter
    * in the alphabet.  The basic FontGlyph for each letter is always included.
    */
   public SearchTreeOCRWithLeakedPixels(List<String> alphabet, Font font, boolean doAddLeftShiftedFontGlyphs) {
      super(alphabet, font);
      List<FontGlyph> originalGlyphs = FontGlyph.buildGlyphsFromAlphabet(alphabet, font);
      if (doAddLeftShiftedFontGlyphs) {
         List<FontGlyphShiftedLeft> shiftedGlyphs = FontGlyphShiftedLeft.shiftAlphabetLeft(originalGlyphs);
         for (FontGlyphShiftedLeft shiftedGlyph : shiftedGlyphs)
            originalGlyphs.add(shiftedGlyph);
      }
      GeneratorOfFontGlyphsWithLeakedPixels classifier = new GeneratorOfFontGlyphsWithLeakedPixels(originalGlyphs);
      standardStartAlphabet = classifier.getAllGlyphsNotRequiringPrecedingGlyph();
      startOfLineAlphabet = classifier.getAllGlyphsWhichCanStartALine();
   }

   @Override
   public List<FontGlyph> detectGlyphsOnOneLine(BufferedImage image, Color fontColor, Point topLeft) {
      Point topLeftCopy = new Point(topLeft.x, topLeft.y);
      return detectGlyphsOnOneLineRecursive(image, fontColor, topLeftCopy,
              getSearchNode(startOfLineAlphabet), true);
   }

   /**
    * @return null if no glyph is recognized, or if a glyph is recognized, but it is a
    * FontGlyphWithLeakedPixels and it does not respect its conditions (cannot start a line, or must
    * be followed by some specified glyphs).
    */
   protected List<FontGlyph> detectGlyphsOnOneLineRecursive(BufferedImage image, Color fontColor,
           Point topLeft, SearchNode startNode, boolean isStartOfLine) {

      FontGlyph detectedGlyph = startNode.findLongestMatch(image, fontColor, topLeft);
      if (detectedGlyph == null)
         return null;

      List<FontGlyph> followingGlyphs;
      if (detectedGlyph instanceof FontGlyphWithLeakedPixels) {
         FontGlyphWithLeakedPixels contextualGlyph = (FontGlyphWithLeakedPixels) detectedGlyph;
         Set<FontGlyph> successorGlyphs = new HashSet<>();
         successorGlyphs.addAll(contextualGlyph.getPossibleSuccessorGlyphs());
         if (!contextualGlyph.requiresSuccessorGlyph())  // TODO useless since always true
            successorGlyphs.addAll(standardStartAlphabet);

         topLeft.x += detectedGlyph.getDimension().width;
         followingGlyphs = detectGlyphsOnOneLineRecursive(image, fontColor, topLeft, getSearchNode(successorGlyphs), false);

         if (contextualGlyph.requiresSuccessorGlyph() && (followingGlyphs == null || followingGlyphs.isEmpty()))
            return null;
      } else {
         topLeft.x += detectedGlyph.getDimension().width;
         followingGlyphs = detectGlyphsOnOneLineRecursive(image, fontColor, topLeft, getSearchNode(standardStartAlphabet), false);
      }
      List<FontGlyph> matchedGlyphs = new ArrayList<>();
      matchedGlyphs.add(detectedGlyph);
      if (followingGlyphs != null)
         matchedGlyphs.addAll(followingGlyphs);
      return matchedGlyphs;
   }

   protected SearchNode getSearchNode(Set<FontGlyph> successorGlyphs) {
      if (!nodesCache.containsKey(successorGlyphs))
         createNodeAndAddToCache(successorGlyphs);
      return nodesCache.get(successorGlyphs);
   }

   private void createNodeAndAddToCache(Set<FontGlyph> successorGlyphs) {
      SearchNode node = new SearchNode(successorGlyphs);
      nodesCache.put(successorGlyphs, node);
   }
}
