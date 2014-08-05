package fixedfontocr;

import java.io.IOException;
import fixedfontocr.utilities.Utilities;

/**
 *
 */
public class SampleImageViewer {

    public static void main(String[] args) throws IOException, InterruptedException {
        SampleImage sampleImage = new SampleImage();
        Utilities.displayImage(Utilities.enlargeImage(sampleImage.getImage(), 5));
    }
}
