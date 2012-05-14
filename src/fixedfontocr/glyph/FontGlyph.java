package fixedfontocr.glyph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Do not use this class directly, use its subclass ContextualFontGlyph instead, unless you are
 * certain the font you are using does not leak pixels out of the bounding box of the characters.
 *
 * The height of a glyph for a specified font is given by
 * {@code Math.round(lineMetrics.getHeight())}. All characters for a specified font have the same
 * height.
 */
public class FontGlyph extends Glyph {

   protected static RenderingHints renderingHints =
           new RenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
   protected static final Map<Font, LineMetrics> lineMetricsCache = new HashMap<>();
   protected String generatingString;
   protected Font font;
   protected LineMetrics lineMetrics;

   public FontGlyph(String generatingString, Font font) {
      this.generatingString = generatingString;
      this.font = font;
      BufferedImage image = makeImage(generatingString, font);
      this.lineMetrics = FontGlyph.getLineMetrics(font);
      Glyph tempGlyph = new Glyph(image, Glyph.DEFAULT_FOREGROUND_COLOR);
      this.dimension = tempGlyph.dimension;
      this.activePixels = tempGlyph.activePixels;
      this.cachedHashCode = tempGlyph.cachedHashCode;
   }

   public FontGlyph(FontGlyph fGlyph) {
      super(fGlyph);
      this.generatingString = fGlyph.generatingString;
      this.font = fGlyph.font;
      this.lineMetrics = fGlyph.lineMetrics;
   }

   public FontGlyph(String generatingString, Font font, Dimension dimension, List<Point> activePixels) {
      super(dimension, activePixels);  // computes the stored hash code
      this.generatingString = generatingString;
      this.font = font;
      this.lineMetrics = FontGlyph.getLineMetrics(font);
   }

   protected FontGlyph(String generatingString) {
      this(generatingString, null);
   }

   protected FontGlyph() {
   }

   public Font getFont() {
      return font;
   }

   public String getGeneratingString() {
      return generatingString;
   }

   public LineMetrics getLineMetrics() {
      return lineMetrics;
   }

   /**
    * Assumes the image type is of IMAGE_TYPE. Uses caching for efficiency.
    */
   public static LineMetrics getLineMetrics(Font font) {
      if (lineMetricsCache.containsKey(font))
         return lineMetricsCache.get(font);
      // TODO ?? ugly
      BufferedImage image = new BufferedImage(1, 1, IMAGE_TYPE);
      LineMetrics lineMetrics = font.getLineMetrics("a", image.createGraphics().getFontRenderContext());
      lineMetricsCache.put(font, lineMetrics);
      return lineMetrics;
   }

   public static RenderingHints getRenderingHints() {
      return renderingHints;
   }

   public static void setRenderingHints(RenderingHints renderingHints) {
      FontGlyph.renderingHints = renderingHints;
   }

   public static List<FontGlyph> buildGlyphsFromAlphabet(List<String> alphabet, Font font) {
      List<FontGlyph> output = new ArrayList<>(alphabet.size());
      for (String letter : alphabet)
         output.add(new FontGlyph(letter, font));
      return output;
   }

   public static BufferedImage makeImage(String string, Font font) {
      return makeImage(string, font, 0, 0, false);
   }

   public static BufferedImage makeImage(String string, Font font, boolean withDecorations) {
      return makeImage(string, font, 0, 0, withDecorations);
   }

   /**
    * If no value is given for renderingHints, the image is NOT anti-aliased. The
    * background/foreground colors are those DEFAULT_BACKGROUND_COLOR and DEFAULT_FOREGROUND_COLOR.
    */
   public static BufferedImage makeImage(String string, Font font, int paddingX, int paddingY, boolean withDecorations) {
      // TODO ?? ugly
      BufferedImage dummyImage = new BufferedImage(1, 1, IMAGE_TYPE);
      Graphics2D dummyGraphics = dummyImage.createGraphics();
      dummyGraphics.setFont(font);

      dummyGraphics.setRenderingHints(renderingHints);
      FontRenderContext frc = dummyGraphics.getFontRenderContext();
      // TODO what is this height from FontMetrics? ascent + 2 * descent?  It's a descent more than LineMetrics height.
      int standardHeight = dummyGraphics.getFontMetrics(font).getHeight();
      Rectangle2D rectangle = font.getStringBounds(string, frc);
      int width = (int) Math.round(rectangle.getWidth()); // don't use getHeight() however since it includes an empty descent on top.
      LineMetrics lineMetrics = FontGlyph.getLineMetrics(font);
      int height = Math.round(lineMetrics.getHeight());
      int descent = Math.round(lineMetrics.getDescent());
//      System.out.println("   Standard height (inclusive): " + standardHeight);
//      System.out.println("height: " + height);
//      System.out.println("lineMetrics height: " + lineMetrics.getHeight());
//      System.out.println("lineMetrics ascent ): " + lineMetrics.getAscent());
//      System.out.println("descent: " + descent);
//      System.out.println("float descent: " + lineMetrics.getDescent());
//      System.out.println("lineMetrics descent: " + descent);
//      System.out.println("lineMetrics leading (between lines): " + lineMetrics.getLeading());
//      System.out.println("lineMetrics baseLineOffset: " + lineMetrics.getBaselineOffsets()[lineMetrics.getBaselineIndex()]);
      dummyGraphics.dispose();
      dummyImage = null;

      int actualHeight = height - descent;
      BufferedImage image = new BufferedImage(width + 2 * paddingX, actualHeight + 2 * paddingY, IMAGE_TYPE);
      Graphics2D graphics = image.createGraphics();
      graphics.setFont(font);
      graphics.setRenderingHints(renderingHints);
      graphics.setColor(Glyph.DEFAULT_BACKGROUND_COLOR);
      graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
      if (withDecorations) {
         int counterX = 0;
         int counter = 0;
         for (int iy = 0; iy < actualHeight; iy++)
            for (int ix = 0; ix < width; ix++)
               if ((ix + iy) % 2 == 0)
                  image.setRGB(ix + paddingX, iy + paddingY, Color.LIGHT_GRAY.getRGB());
         for (char chara : string.toCharArray()) {
            graphics.setColor((counter % 2 == 0) ? Color.RED : Color.CYAN);
            Rectangle2D rect = font.getStringBounds(Character.toString(chara), frc);
            int wid = (int) Math.round(rect.getWidth());
            //System.out.printf("%s %d%n", chara, wid);
            graphics.drawRect(counterX + paddingX, 0 + paddingY, wid - 1, actualHeight - 1);
            counterX += wid;
            counter++;
         }
      }
      graphics.setColor(Glyph.DEFAULT_FOREGROUND_COLOR);
      graphics.drawString(string, 0 + paddingX, actualHeight - descent + paddingY);
      return image;
   }

   /**
    * The background/foreground colors are those DEFAULT_BACKGROUND_COLOR and
    * DEFAULT_FOREGROUND_COLOR.
    */
   public static BufferedImage makeMultiLineImage(List<String> lines, Font font, int lineHeight) {
      if (lines.isEmpty())
         throw new IllegalArgumentException("Must give some lines to draw.");
      List<BufferedImage> images = new ArrayList<>(lines.size());
      for (String line : lines)
         images.add(makeImage(line, font));
      int glyphHeight = images.get(0).getHeight();
      int nEmptyRowsBetweenLines = lineHeight - glyphHeight;

      int height = lines.size() * lineHeight;
      int width = 0;
      for (BufferedImage subImage : images)
         if (subImage.getWidth() > width)
            width = subImage.getWidth();

      BufferedImage image = new BufferedImage(width, height, Glyph.IMAGE_TYPE);
      Graphics2D graphics = image.createGraphics();
      graphics.setColor(Glyph.DEFAULT_BACKGROUND_COLOR);
      graphics.fillRect(0, 0, width, height);
      int currentHeight = nEmptyRowsBetweenLines;
      for (BufferedImage subImage : images) {
         graphics.drawImage(subImage, null, 0, currentHeight);
         currentHeight += lineHeight;
      }
      return image;
   }

   /////////////////////////////////////////////////////////////////////////////////////////////
   /**
    * Bitmap fonts are barely used anymore, so FontGlyph (for scalable fonts) 
    * should normally be used instead.
    */
   // FontGlyph should have been made an abstract class with subclasses BitmapFontGlyph and
   // ScalableFontGlyph.  But bitmap fonts are so rarely used that this "ugly" subclass to
   // the scalable FontGlyph is used instead.
   public static class Bitmap extends FontGlyph {

      protected String fontName;

      public Bitmap(String generatingString, BufferedImage characterImage, Color fontColor, String fontName) {
         super(generatingString);
         this.fontName = fontName;
         Glyph tempGlyph = new Glyph(characterImage, fontColor);
         this.dimension = tempGlyph.dimension;
         this.activePixels = tempGlyph.activePixels;
         this.cachedHashCode = tempGlyph.cachedHashCode;
      }

      public String getFontName() {
         return fontName;
      }

      @Override
      public Font getFont() {
         throw new UnsupportedOperationException("Use getFontName() instead");
      }

      @Override
      public LineMetrics getLineMetrics() {
         throw new UnsupportedOperationException();
      }
   }
}
