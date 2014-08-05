package fixedfontocr.glyph;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A font glyphs which has active pixels outside its bounding box.
 */
public class LeakingFontGlyph extends FontGlyph {

   protected static int paddingForLeakedPixelSearch = 6;
   protected List<Point> leakedPixelsToLeft;
   protected List<Point> leakedPixelsToRight;

   /**
    * Keeping this constructor protected since it builds a LeakingFontGlyph even when it is not
    * leaking any pixels.
    */
   protected LeakingFontGlyph(FontGlyph glyph) {
      super(glyph);
      BufferedImage wideImage = FontGlyph.makeImage(glyph.getGeneratingString(), glyph.getFont(), paddingForLeakedPixelSearch, paddingForLeakedPixelSearch, false);
      Dimension narrowDim = glyph.getDimension();
      // First, scan up and down outside the glyph and throws an Exception if a pixel is found.
      for (int iy = 0; iy < paddingForLeakedPixelSearch; iy++)
         checkNoPixelOnLine(wideImage, iy, glyph.getGeneratingString());
      for (int iy = paddingForLeakedPixelSearch + narrowDim.height; iy < wideImage.getHeight(); iy++)
         checkNoPixelOnLine(wideImage, iy, glyph.getGeneratingString());

      int yStart = paddingForLeakedPixelSearch;
      int yHeight = narrowDim.height;
      this.leakedPixelsToLeft = findPixelsInsideOriginalHeight(wideImage,
              0, paddingForLeakedPixelSearch,
              yStart, yHeight);

      this.leakedPixelsToRight = findPixelsInsideOriginalHeight(wideImage,
              paddingForLeakedPixelSearch + narrowDim.width, paddingForLeakedPixelSearch,
              yStart, yHeight);
   }

   /**
    * @return null if the symbol is not leaking pixels outside its bounding box.
    */
   public static LeakingFontGlyph createLeakingFontGlyphIfLeaking(FontGlyph glyph) {
      LeakingFontGlyph leakingGlyph = new LeakingFontGlyph(glyph);
      if (leakingGlyph.leakedPixelsToLeft.isEmpty() && leakingGlyph.leakedPixelsToRight.isEmpty())
         return null;
      return leakingGlyph;
   }

   /**
    * @return only the symbols that do leak.
    */
   public static List<LeakingFontGlyph> processAlphabet(Collection<FontGlyph> glyphs) {
      List<LeakingFontGlyph> leakers = new ArrayList<>();
      for (FontGlyph glyph : glyphs) {
         LeakingFontGlyph lfGlyph = createLeakingFontGlyphIfLeaking(glyph);
         if (lfGlyph != null)
            leakers.add(lfGlyph);
      }
      return leakers;
   }

   protected static void checkNoPixelOnLine(BufferedImage image, int iy, String symbol) {
      for (int ix = 0; ix < image.getWidth(); ix++) {
         if (image.getRGB(ix, iy) == FontGlyph.DEFAULT_FOREGROUND_COLOR.getRGB())
            throw new IllegalStateException("The character " + symbol
                    + " is leaking pixels above/below its bounding box.");
      }
   }

   /**
    * The pixels in the returned list are re-centered to the original coordinate system (top left of
    * the original glyph). An empty list is returned if there are no pixels in the specified region.
    */
   protected static List<Point> findPixelsInsideOriginalHeight(BufferedImage image, int xStart, int width,
           int yStart, int height) {
      List<Point> pixels = new ArrayList<>();
      for (int ix = xStart; ix < xStart + width; ix++)
         for (int iy = yStart; iy < yStart + height; iy++)
            if (image.getRGB(ix, iy) == FontGlyph.DEFAULT_FOREGROUND_COLOR.getRGB())
               pixels.add(new Point(ix - paddingForLeakedPixelSearch, iy - paddingForLeakedPixelSearch));
      return pixels;
   }

   /**
    *
    * @return can be an empty list, but never null.
    */
   public List<Point> getLeakedPixelsToLeft() {
      return Collections.unmodifiableList(leakedPixelsToLeft);
   }

   /**
    *
    * @return can be an empty list, but never null.
    */
   public List<Point> getLeakedPixelsToRight() {
      return Collections.unmodifiableList(leakedPixelsToRight);
   }

   public static int getPaddingForLeakedPixelSearch() {
      return paddingForLeakedPixelSearch;
   }
}
