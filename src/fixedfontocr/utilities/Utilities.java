package fixedfontocr.utilities;

import fixedfontocr.glyph.Glyph;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

public class Utilities {

   private static BufferedImage captureScreen() throws AWTException {
      Robot screenCaptureRobot = new Robot();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      return screenCaptureRobot.createScreenCapture(new Rectangle(screenSize));
   }

   public static void displayImage(BufferedImage image) {
      ImageViewer imageViewer = new ImageViewer(image);
      imageViewer.startFromSwingEDT();
   }

   /**
    * A utility method that blows up an image by {@code factor}. It is useful when examining looking
    * at glyphs pixel-by-pixel.
    */
   public static BufferedImage enlargeImage(BufferedImage image, int factor) {
      BufferedImage largeImage = new BufferedImage(image.getWidth() * factor, image.getHeight() * factor, Glyph.IMAGE_TYPE);
      Graphics2D g = largeImage.createGraphics();
      g.drawImage(image, 0, 0, largeImage.getWidth(), largeImage.getHeight(), null);
      return largeImage;
   }

   public static void printOutAllAvailableFonts() {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      String[] fonts = ge.getAvailableFontFamilyNames();
      for (String font : fonts)
         System.out.println(font);
   }
}
