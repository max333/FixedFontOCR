package fixedfontocr.utilities;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImageViewer extends JFrame {

   private BufferedImage image;

   public ImageViewer(BufferedImage bimage) {
      this.image = bimage;
      JPanel imagePanel = new JPanel() {

         @Override
         protected void paintComponent(Graphics g) {
            g.drawImage(image, 0, 0, null);
         }

         @Override
         public Dimension getPreferredSize() {
            return new Dimension(image.getWidth(), image.getHeight());
         }
      };
      Dimension imagePanelDim = imagePanel.getPreferredSize();
      Dimension dim = new Dimension((int) imagePanelDim.getWidth() + 20, (int) imagePanelDim.getHeight() + 60);
      //this.getContentPane().setPreferredSize(dim);
      Container pane = getContentPane();
      pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
      pane.add(imagePanel);
      pane.validate();
   }

   public void startFromSwingEDT() {
      SwingUtilities.invokeLater(new Runnable() {

         @Override
         public void run() {
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            validate();
            pack();
            setResizable(true);
            setVisible(true);
         }
      });
   }
}
