package fixedfontocr.glyph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A fixed arrangement of pixels within a box of a specified dimension. The pixels have no color,
 * but are simply active or inactive. Only the active pixels are stored in an array. The pixels must
 * be ordered increasingly in x, and the pixels in each column are ordered increasingly in y.
 *
 * <p> The hash code is computed during construction and cached.
 */
public class Glyph {

   public static final int IMAGE_TYPE = BufferedImage.TYPE_INT_RGB;
   public static final Color DEFAULT_FOREGROUND_COLOR = Color.BLACK;
   public static final Color DEFAULT_BACKGROUND_COLOR = Color.WHITE;
   protected Dimension dimension;
   protected List<Point> activePixels;
   protected int cachedHashCode;

   /**
    * The activePixels are reordered.
    */
   public Glyph(Dimension dimension, List<Point> activePixels) {
      this.dimension = dimension;
      List<Point> tempPixels = new ArrayList<>();
      tempPixels.addAll(activePixels);
      sortPixelsInYThenInX(tempPixels);
      this.activePixels = Collections.unmodifiableList(tempPixels);
      this.cachedHashCode = precomputeHashCode();
   }

   public Glyph(Glyph glyph) {
      this.dimension = glyph.dimension;
      this.activePixels = glyph.activePixels;
      this.cachedHashCode = glyph.cachedHashCode;
   }

   /**
    * The active pixels are those of color {@code Glyph.DEFAULT_FOREGROUND_COLOR}.
    */
   public Glyph(BufferedImage image, Color activeColor) {
      this(image, activeColor, new Point(0, 0), new Dimension(image.getWidth(), image.getHeight()));
   }

   /**
    * Build a Glyph from the sub-image of dimension {@code dimension} starting at {@code start}.
    */
   public Glyph(BufferedImage image, Color activeColor, Point start, Dimension dimension) {
      this.dimension = dimension;
      activePixels = new ArrayList<>();
      for (int ix = start.x; ix < start.x + dimension.width; ix++) {
         for (int iy = start.y; iy < start.y + dimension.height; iy++) {
            if (image.getRGB(ix, iy) == activeColor.getRGB())
               activePixels.add(new Point(ix - start.x, iy - start.y));
         }
      }
      this.cachedHashCode = precomputeHashCode();
   }

   protected Glyph() {
   }

   public Dimension getDimension() {
      return dimension;
   }

   public List<Point> getActivePixels() {
      return Collections.unmodifiableList(activePixels);
   }

   public BufferedImage asImage() {
      int width = dimension.width;
      int height = dimension.height;
      BufferedImage image = new BufferedImage(width, height, IMAGE_TYPE);
      Graphics2D g = image.createGraphics();
      g.setColor(DEFAULT_BACKGROUND_COLOR);
      g.drawRect(0, 0, width, height);
      g.dispose();
      for (Point pixel : activePixels)
         image.setRGB(pixel.x, pixel.y, DEFAULT_FOREGROUND_COLOR.getRGB());
      return image;
   }

   /**
    * Gets a sub-glyph of the same height, but of the specified width, starting at the specified
    * column.
    */
   public Glyph getSubGlyph(int startColumn, int width) {
      List<Point> chosenPixels = new ArrayList<>();
      for (Point pixel : activePixels) {
         if (pixel.getX() >= startColumn) {
            if (pixel.getX() >= startColumn + width)
               break;
            chosenPixels.add(new Point(pixel.x - startColumn, pixel.y));
         }
      }
      return new Glyph(new Dimension(width, this.getDimension().height), chosenPixels);
   }
   protected static Comparator<Point> comparatorX = new Comparator<Point>() {

      @Override
      public int compare(Point o1, Point o2) {
         return o1.x - o2.x;
      }
   };
   protected static Comparator<Point> comparatorY = new Comparator<Point>() {

      @Override
      public int compare(Point o1, Point o2) {
         return o1.y - o2.y;
      }
   };

   public static void sortPixelsInYThenInX(List<Point> points) {
      Collections.sort(points, comparatorY);
      Collections.sort(points, comparatorX);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null)
         return false;
      if (!(obj instanceof Glyph))
         return false;
      final Glyph other = (Glyph) obj;
      if (!Objects.equals(this.activePixels, other.activePixels))
         return false;
      return true;
   }

   @Override
   public int hashCode() {
      return cachedHashCode;
   }

   private int precomputeHashCode() {
      int hash = 7;
      hash = 47 * hash + Objects.hashCode(this.activePixels);
      return hash;
   }
}
