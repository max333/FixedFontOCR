package fixedfontocr;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Defines the sample image and its text content.
 */
public class SampleImage {
    private final String filename = "test-verdana-16.png";
    private final Font font = new Font("Verdana", Font.PLAIN, 16);
    private final List<String> targetLines = Arrays.asList(
            "This is Verdana 16 with no pixels",
            "between lines.",
            "~!@#$%^&*()_+`-=[]\\{}|;");
    private final BufferedImage image;

    public SampleImage() throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource(filename);
        image = ImageIO.read(url);
    }

    public String getFilename() {
        return filename;
    }

    public Font getFont() {
        return font;
    }

    public List<String> getTargetLines() {
        return targetLines;
    }

    public BufferedImage getImage() {
        return image;
    }
    
    
}
