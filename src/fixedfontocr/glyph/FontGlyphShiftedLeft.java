package fixedfontocr.glyph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Builds a glyph by taking a FontGlyph and moving the pixels one column to the left (and removing
 * the now empty column at the far right). This is useful for handling one basic type of ligature:
 * some pairs of characters are displayed with a column of pixels removed between.
 */
public class FontGlyphShiftedLeft extends FontGlyph {

   protected int nColumnsToRemove = 1;

   public FontGlyphShiftedLeft(FontGlyph fGlyph) {
      super(fGlyph);
      this.dimension = new Dimension(this.dimension.width - nColumnsToRemove, this.dimension.height);

      List<Point> tempPixels = new ArrayList<>(this.activePixels.size());
      for (Point point : this.activePixels)
         tempPixels.add(new Point(point.x - nColumnsToRemove, point.y));
      this.activePixels = Collections.unmodifiableList(tempPixels);
      this.cachedHashCode = precomputeHashCode();
   }
   
   public static List<FontGlyphShiftedLeft> shiftAlphabetLeft(List<FontGlyph> alphabet) {
      List<FontGlyphShiftedLeft> shiftedGlyphs = new ArrayList<>(alphabet.size());
      for (FontGlyph glyph : alphabet)
         shiftedGlyphs.add(new FontGlyphShiftedLeft(glyph));
      return shiftedGlyphs;
   }
}
