
import fixedfontocr.ContextualSearchTreeOCR;
import fixedfontocr.SearchTreeOCR;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Test on test-verdana-16.png, which was generated from Photoshop.
 * </p>
 * Currently this test does not fully work since Photoshop removes a column of pixels
 * between "Ve" (ligature).
 */
public class TestTextFromPhotoshop {

   public static void main(String[] args) throws IOException {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL url = cl.getResource("test-verdana-16.png");
      BufferedImage testImage = ImageIO.read(url);
      //Utilities.displayImage(Utilities.enlargeImage(testImage, 7));
      
      Font font = new Font("Verdana", Font.PLAIN, 16);
      List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
      SearchTreeOCR searchTree = new ContextualSearchTreeOCR(alphabet, font);
      List<String> lines = 
              searchTree.detectCharactersOnMultipleLines(testImage, Color.BLACK, new Point(0, 0));
      for (String line : lines)
         System.out.println(line);
   }
}
