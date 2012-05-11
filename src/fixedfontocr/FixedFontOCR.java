package fixedfontocr;

import fixedfontocr.glyph.Glyph;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Parses a text image and outputs the corresponding text. <p> This class is a wrapper around {@code ContextualSearchTreeOCR}:
 * It simplifies the initialization, but it also gives less control.
 */
public class FixedFontOCR {

   protected SearchTreeOCR searchTree;

   /**
    * @param fontStyle plain, bold or italic.
    */
   public FixedFontOCR(String fontName, String fontStyle, int fontSize) {
      Font font = new Font(fontName, convertFontStyle(fontStyle), fontSize);
      List<String> alphabet = SearchTreeOCR.getDefaultAlphabet();
      searchTree = new ContextualSearchTreeOCR(alphabet, font);
   }
   
   /**
    * Font style is set to "plain".
    */
   public FixedFontOCR(String fontName, int fontSize) {
      this(fontName, "plain", fontSize);
   }

   /**
    * Processes the image to extract the text. If multiple text lines are recognized, they are
    * returned as one string with "\n" to demarcate a line break.
    */
   public String recognize(BufferedImage image, Color fontColor, int lineHeight) {
      List<String> lines = 
              searchTree.detectCharactersOnMultipleLines(image, fontColor, lineHeight, new Point(0, 0));
      StringBuilder sb = new StringBuilder();
      for (String line : lines)
         sb.append(line).append("\n");
      sb.deleteCharAt(sb.length() - 1);
      return sb.toString();
   }

   /**
    * Cannot recognize the equivalent of (Font.BOLD|Font.ITALIC).
    */
   protected int convertFontStyle(String fontStyle) {
      switch (fontStyle.toLowerCase()) {
         case "bold":
            return Font.BOLD;
         case "italic":
            return Font.ITALIC;
         case "plain":
            return Font.PLAIN;
         default:
            throw new IllegalArgumentException("The font style must be PLAIN, BOLD or ITALIC.");
      }
   }
}
