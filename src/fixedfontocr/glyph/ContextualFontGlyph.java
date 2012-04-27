package fixedfontocr.glyph;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A FontGlyph which is conditional (contextual) on neighboring glyphs being of certain values.
 * This can happen if a glyph is leaking pixels in the bounding box of neighboring glyphs,
 * or if a glyph is receiving those leaked pixels.
 */
public class ContextualFontGlyph extends FontGlyph {

   protected Set<FontGlyph> possibleSuccessorGlyphs;
   // TODO requiresPreceding will always be true since include case of leakerToLeft starting line in FontGlyphClassifier.generateAllCompoundsGlyph
   protected boolean requiresPrecedingGlyph;
   // TODO similar to above
   protected boolean requiresSuccessorGlyph;
   protected boolean canStartLine;

   
   public ContextualFontGlyph(FontGlyph mainGlyph, Set<FontGlyph> possibleGlyphsOnLeft, 
           Set<FontGlyph> possibleGlyphsOnRight) {
      super(mainGlyph);
      this.requiresPrecedingGlyph = !possibleGlyphsOnLeft.contains(null);
      this.requiresSuccessorGlyph = !possibleGlyphsOnRight.contains(null);
      Set<FontGlyph> possibleGlyphsOnRight_copy = new HashSet<>();
      possibleGlyphsOnRight_copy.addAll(possibleGlyphsOnRight);
      possibleGlyphsOnRight_copy.remove(null);
      this.possibleSuccessorGlyphs = Collections.unmodifiableSet(possibleGlyphsOnRight_copy);
   }
  
   /**
    * If the glyph was obtained by leaking pixels from the left, this gives the list of the possible
    * glyphs on the left leaking such pixels.
    * 
    * If requiresSuccessorGlyph is true, this set will contain at least one value.
    * If requiresSuccessorGlyph is false, this glyph can be followed by any of the standard 
    * starting glyphs, and also by the glyphs returned by this method.
    */
   public Set<FontGlyph> getPossibleSuccessorGlyphs() {
      return Collections.unmodifiableSet(possibleSuccessorGlyphs);
   }

   /**
    * false if there is at least one combination giving this glyph which does not require a
    * preceding character to the left. A ContextualFontGlyph for which this property is true could
    * not start a line and could only follow a specific glyph(s).
    */
   public boolean requiresPrecedingGlyph() {
      return requiresPrecedingGlyph;
   }

   /**
    * If true, only the glyphs specified by getPossibleSuccessorGlyphs are valid successors.
    * If false, the glyphs specified by getPossibleSuccessorGlyphs are valid successors, but
    * so are all the standard starting glyphs.
    */
   public boolean requiresSuccessorGlyph() {
      return requiresSuccessorGlyph;
   }

   public boolean canStartLine() {
      return canStartLine;
   }
}
