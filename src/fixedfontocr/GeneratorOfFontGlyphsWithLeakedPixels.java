package fixedfontocr;

import fixedfontocr.glyph.FontGlyphWithLeakedPixels;
import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.LeakingFontGlyph;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * First finds the symbols that leak pixels outside their bounding boxes and then builds all possible
 * glyphs by combining pixels from a glyphs and those leaked by its neighbors.
 */
public class GeneratorOfFontGlyphsWithLeakedPixels {

   // nonContextualGlyphs: Standard glyphs which do not contain extra pixels from their neighbors.
   // Not that a glyph that receives some pixels from neighbors, but those pixels just overlap
   // existing pixels, then the glyph is also moved to contextualGlyphs.
   protected List<FontGlyph> nonContextualGlyphs;
   protected Set<FontGlyphWithLeakedPixels> contextualGlyphs;
   protected Set<FontGlyph> allGlyphs;
   protected Set<FontGlyph> allGlyphsNotRequiringPrecedingGlyph;
   protected Set<FontGlyph> allGlyphsWhichCanStartALine;
   protected Set<LeakingFontGlyph> leakersToLeft; // Some can be both leakersToLeft and Right
   protected Set<LeakingFontGlyph> leakersToRight;
   protected Map<FontGlyph, Set<FontGlyph>> mapGlyphToGlyphOnLeft;
   protected Map<FontGlyph, Set<FontGlyph>> mapGlyphToGlyphOnRight;

   public GeneratorOfFontGlyphsWithLeakedPixels(List<FontGlyph> originalGlyphs) {
      nonContextualGlyphs = new ArrayList<>();
      nonContextualGlyphs.addAll(originalGlyphs);  // but the leakers are removed below

      leakersToLeft = new HashSet<>();
      leakersToRight = new HashSet<>();
      for (LeakingFontGlyph leaker : LeakingFontGlyph.processAlphabet(originalGlyphs)) {
         nonContextualGlyphs.remove(leaker);
         if (!leaker.getLeakedPixelsToLeft().isEmpty())
            leakersToLeft.add(leaker);
         if (!leaker.getLeakedPixelsToRight().isEmpty())
            leakersToRight.add(leaker);
      }

      generateAllCompoundGlyphs();


      Set<FontGlyph> newGlyphs = new HashSet<>();
      newGlyphs.addAll(mapGlyphToGlyphOnLeft.keySet());
      newGlyphs.addAll(mapGlyphToGlyphOnRight.keySet());
      contextualGlyphs = new HashSet<>();
      for (FontGlyph newGlyph : newGlyphs) {
         FontGlyphWithLeakedPixels newContextualGlyph =
                 new FontGlyphWithLeakedPixels(newGlyph,
                 mapGlyphToGlyphOnLeft.get(newGlyph), mapGlyphToGlyphOnRight.get(newGlyph));
         contextualGlyphs.add(newContextualGlyph);
      }

      allGlyphs = new HashSet<>();
      allGlyphs.addAll(nonContextualGlyphs);
      allGlyphs.addAll(contextualGlyphs);
      allGlyphsNotRequiringPrecedingGlyph = new HashSet<>();
      allGlyphsNotRequiringPrecedingGlyph.addAll(nonContextualGlyphs);
      for (FontGlyphWithLeakedPixels glyph : contextualGlyphs)
         if (!glyph.requiresPrecedingGlyph())
            allGlyphsNotRequiringPrecedingGlyph.add(glyph);
      allGlyphsWhichCanStartALine = new HashSet<>();
      allGlyphsWhichCanStartALine.addAll(nonContextualGlyphs);
      for (FontGlyphWithLeakedPixels glyph : contextualGlyphs)
         if (!glyph.canStartLine())
            allGlyphsWhichCanStartALine.add(glyph);
   }

   protected final void generateAllCompoundGlyphs() {
      mapGlyphToGlyphOnLeft = new HashMap<>();
      mapGlyphToGlyphOnRight = new HashMap<>();
      Set<FontGlyph> nonContextualPlusLeakers = new HashSet<>();
      nonContextualPlusLeakers.addAll(nonContextualGlyphs);
      nonContextualPlusLeakers.addAll(leakersToLeft);
      nonContextualPlusLeakers.addAll(leakersToRight);
      HashSet<LeakingFontGlyph> leakersToLeftPlusNull = new HashSet<>();
      leakersToLeftPlusNull.addAll(leakersToLeft);
      leakersToLeftPlusNull.add(null);
      HashSet<LeakingFontGlyph> leakersToRightPlusNull = new HashSet<>();
      leakersToRightPlusNull.addAll(leakersToRight);
      leakersToRightPlusNull.add(null);

      for (LeakingFontGlyph glyphOnLeft : leakersToRightPlusNull)
         for (FontGlyph centerGlyph : nonContextualPlusLeakers)
            for (LeakingFontGlyph glyphOnRight : leakersToLeftPlusNull) {
               FontGlyph modifiedGlyphOnLeft = addPixelsToMiddleGlyph(null, glyphOnLeft, centerGlyph);
               FontGlyph modifiedGlyphOnRight = addPixelsToMiddleGlyph(centerGlyph, glyphOnRight, null);
               FontGlyph modifiedCenterGlyph = addPixelsToMiddleGlyph(glyphOnLeft, centerGlyph, glyphOnRight);
               addToMapsLeftAndRight(modifiedGlyphOnLeft, modifiedCenterGlyph);
               addToMapsLeftAndRight(modifiedCenterGlyph, modifiedGlyphOnRight);
            }
   }

   /**
    * The middleGlyph will have some pixels added in its bounding box if the left/right glyphs leak
    * pixels in the right direction.
    */
   protected FontGlyph addPixelsToMiddleGlyph(FontGlyph leftGlyph, FontGlyph middleGlyph, FontGlyph rightGlyph) {
      if (middleGlyph == null)
         return null;
      List<Point> activePixels = new ArrayList<>();
      activePixels.addAll(middleGlyph.getActivePixels());
      if (leftGlyph != null && (leftGlyph instanceof LeakingFontGlyph))
         for (Point pixel : ((LeakingFontGlyph) leftGlyph).getLeakedPixelsToRight())
            activePixels.add(new Point(pixel.x - leftGlyph.getDimension().width, pixel.y));
      if (rightGlyph != null && (rightGlyph instanceof LeakingFontGlyph))
         for (Point pixel : ((LeakingFontGlyph) rightGlyph).getLeakedPixelsToLeft())
            activePixels.add(new Point(pixel.x + middleGlyph.getDimension().width, pixel.y));

      return new FontGlyph(middleGlyph.getGeneratingString(), middleGlyph.getFont(),
              middleGlyph.getDimension(), activePixels);
   }

   /**
    * One of the two glyphs can be null and the null value will be added to the corresponding map of
    * the other non-null glyph.
    */
   protected void addToMapsLeftAndRight(FontGlyph leftGlyph, FontGlyph rightGlyph) {
      if (leftGlyph != null) {
         if (!mapGlyphToGlyphOnRight.containsKey(leftGlyph))
            mapGlyphToGlyphOnRight.put(leftGlyph, new HashSet<FontGlyph>());
         mapGlyphToGlyphOnRight.get(leftGlyph).add(rightGlyph);
      }
      if (rightGlyph != null) {
         if (!mapGlyphToGlyphOnLeft.containsKey(rightGlyph))
            mapGlyphToGlyphOnLeft.put(rightGlyph, new HashSet<FontGlyph>());
         mapGlyphToGlyphOnLeft.get(rightGlyph).add(leftGlyph);
      }
   }

   public Set<FontGlyph> getAllGlyphs() {
      return Collections.unmodifiableSet(allGlyphs);
   }

   /**
    * All glyphs that can start a line. This includes all FontGlyphs which are not
    * ContextualFontGlyphs, and those ContextualFontGlyphs which only had pixels leaked from the
    * right.
    */
   public Set<FontGlyph> getAllGlyphsNotRequiringPrecedingGlyph() {
      return Collections.unmodifiableSet(allGlyphsNotRequiringPrecedingGlyph);
   }

   public Set<FontGlyph> getAllGlyphsWhichCanStartALine() {
      return Collections.unmodifiableSet(allGlyphsWhichCanStartALine);
   }

   public Set<FontGlyphWithLeakedPixels> getContextualGlyphs() {
      return Collections.unmodifiableSet(contextualGlyphs);
   }

   public List<FontGlyph> getNonContextualGlyphs() {
      return Collections.unmodifiableList(nonContextualGlyphs);
   }
}
