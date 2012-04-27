package fixedfontocr.utilities;

import fixedfontocr.ContextualSearchTreeOCR;
import fixedfontocr.SearchTreeOCR;
import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.Glyph;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

   private static void printOutAllAvailableFonts() {
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      String[] fonts = ge.getAvailableFontFamilyNames();
      for (String font : fonts)
         System.out.println(font);
   }

   ////////////////////////////////////////////////////////////////////////////////
   public static void main(String[] args) throws AWTException, IOException {
      //FontGlyph.setRenderingHints(new RenderingHints(Collections.singletonMap(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)));

      //Font font = new Font("monospaced", Font.BOLD, 13);
      //Font font = new Font("Verdana", Font.PLAIN, 16);
      //Font font = new Font("MS Reference Sans Serif", Font.PLAIN, 10);
      //Font font = new Font("MS Sans Serif", Font.PLAIN, 12); // does not exist use some default.
      Font font = new Font("Microsoft Sans Serif", Font.PLAIN, 10);

      boolean doDisplayFluff = false;
      String targetString = "j1lfajF_Lfpf*j&$j($)}{|/NOooI234'\"'ff";
      BufferedImage image = FontGlyph.makeImage(targetString, font, doDisplayFluff);
      //displayImage(image);
      //displayImage(FontGlyph.enlargeImage(image, 7));
      //ImageIO.write(image, "png", new File("MS-Sans-Serif-12-Max.png"));

      // Build the search nodes for recognizing the glyphs.
      List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
      SearchTreeOCR searchTree = new ContextualSearchTreeOCR(alphabet, font);

      searchTree.checkIfAllSymbolsCanBeRecognized(alphabet);
      
      // Single line match.
      String lineMatch = searchTree.detectCharactersOnOneLine(image, new Point(0, 0));
      System.out.println("Target line:      " + targetString);
      System.out.println("Interpreted line: " + lineMatch);
      System.out.println("is " + (lineMatch.equals(targetString) ? "SUCCESS" : "FAIL"));

      // Multi-line match.
      List<String> lines = new ArrayList<>(Arrays.asList(targetString, "Some line...", " ...and another"));
      BufferedImage multiLineImage = FontGlyph.makeMultiLineImage(lines, font, SearchTreeOCR.defaultPixelsBetweenLines);
      displayImage(multiLineImage);
      List<String> matchedLines = searchTree.detectCharactersOnMultipleLines(multiLineImage, new Point(0, 0));
      for (int i = 0; i < lines.size(); i++) {
         System.out.println("target line      :   " + lines.get(i));
         System.out.println("interpreted line :   " + ((i < matchedLines.size()) ? matchedLines.get(i) : "null"));
      }
   }
}
