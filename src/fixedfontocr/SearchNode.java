package fixedfontocr;

import fixedfontocr.glyph.FontGlyph;
import fixedfontocr.glyph.Glyph;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds a search tree recursively for an alphabet of Glyphs. When creating the head node, the
 * first step is to find the glyph with the narrowest width (minWidth). The glyphs are then
 * classified according to their sub-glyph made of their first minWidth columns. Glyphs which have
 * identical starting sub-glyphs of width minWidth are grouped together in a new SearchNode. If a
 * glyph has a unique sub-glyph, it is put in a new SearchNode with only one glyph.
 *
 * <p> All the newly created Nodes are stored in a HashMap in the head SearchNode such that when
 * given some target glyph, we can look at its minWidth starting columns and determine which
 * sub-SearchNode (if any) might contain the target glyph.
 *
 * <p> The node creation process is recursive since the newly created Nodes in the head SearchNode
 * can themselves create new Nodes. The minimal width for a sub-SearchNode is the smallest width of
 * the subset of glyphs that make the alphabet for that node.
 *
 * <p> When trying to match some target glyph to the glyphs in the original alphabet, the search
 * proceeds through a sequence of sub-Nodes until it stops. It returns the longest match found, if
 * any.
 *
 * <p> For the special case where a glyph from the alphabet not only matches the sub-glyph of the
 * specified width, but also has a total width that is equal to the specified width, that glyph is
 * not put in a new SearchNode, but is stored inside the current SearchNode as a special matching
 * value and can be retrieved with node.getExactMatch(). The matching value is unique. A SearchNode
 * may or may not have a matching value; just as a SearchNode may or may not have sub-Nodes. However
 * it would make no sense to have a SearchNode without a matching value or sub-Nodes.
 *
 */
public class SearchNode {

   protected FontGlyph exactMatch;
   protected int searchGlyphWidth;
   protected int alreadySkippedColumns;
   protected Map<Glyph, SearchNode> mapToSubNodes;
   protected int lineHeight;

   public SearchNode(Collection<FontGlyph> glyphs) {
      this(glyphs, 0);
   }

   /**
    * Classifies the {@code glyphs} from their shortest common sub-glyph and automatically build
    * more nodes for them, which can them be accessed from {@code findNextNode}.
    *
    * <p> If one of the nodes has a width that is the same as {@code alreadySkippedColumns}, that
    * means it is an exact match. Such a node is stored such that it can be retrieved with {@code getExactMatch}.
    *
    * @param glyphs
    * @param alreadySkippedColumns
    */
   public SearchNode(Collection<FontGlyph> glyphs, int alreadySkippedColumns) {
      this.alreadySkippedColumns = alreadySkippedColumns;
      this.searchGlyphWidth = findMinimalWidth(glyphs, alreadySkippedColumns);
      if (glyphs.isEmpty())
         throw new IllegalArgumentException("Must have some glyphs.");
      lineHeight = glyphs.iterator().next().getDimension().height;
      mapToSubNodes = new HashMap<>();
      Map<Glyph, List<FontGlyph>> mapToListGlyphs = new HashMap<>();
      for (FontGlyph glyph : glyphs) {
         if (glyph.getDimension().width == alreadySkippedColumns) {
            exactMatch = glyph;
         } else {
            Glyph subGlyph = glyph.getSubGlyph(alreadySkippedColumns, searchGlyphWidth);
            if (!mapToListGlyphs.containsKey(subGlyph))
               mapToListGlyphs.put(subGlyph, new ArrayList<FontGlyph>());
            mapToListGlyphs.get(subGlyph).add(glyph);
         }
      }
      if (false) {
         Map<Integer, Integer> listLengthCounter = new HashMap<>();
         for (List<FontGlyph> list : mapToListGlyphs.values()) {
            if (!listLengthCounter.containsKey(list.size()))
               listLengthCounter.put(list.size(), 1);
            else
               listLengthCounter.put(list.size(), listLengthCounter.get(list.size()) + 1);
         }
         for (int lenght : listLengthCounter.keySet())
            System.out.printf("# of shared %d glyph beginnings (size %d, prev %d):  %d%n", lenght, searchGlyphWidth, alreadySkippedColumns, listLengthCounter.get(lenght));
         //System.out.println("");
      }
      for (Glyph subGlyph : mapToListGlyphs.keySet()) {
         SearchNode nodeSubGlyph = new SearchNode(mapToListGlyphs.get(subGlyph), alreadySkippedColumns + searchGlyphWidth);
         mapToSubNodes.put(subGlyph, nodeSubGlyph);
      }
   }

   /**
    *
    * @return null if no exact match.
    */
   public FontGlyph getExactMatch() {
      return exactMatch;
   }

   /**
    * @return null if found no matching glyph.
    */
   public SearchNode findNextNode(Glyph subGlyph) {
      return mapToSubNodes.get(subGlyph);
   }

   /**
    * @return null if found no matching glyph.
    */
   public FontGlyph findLongestMatch(BufferedImage image, Color fontColor, Point topLeft_) {
      Point topLeft = new Point(topLeft_.x, topLeft_.y);
      FontGlyph longestMatch = null;
      SearchNode nextNode = this;
      SearchNode currentNode;
      while ((currentNode = nextNode) != null) {
         if (currentNode.getExactMatch() != null)
            longestMatch = currentNode.getExactMatch();
         int subGlyphWidth = currentNode.searchGlyphWidth;
         if (image.getWidth() < (topLeft.x + subGlyphWidth)) {
            nextNode = null; // running out of image to find longer matches.
         } else {
            Glyph subGlyph = new Glyph(image, fontColor, topLeft, new Dimension(subGlyphWidth, lineHeight));
            nextNode = currentNode.findNextNode(subGlyph); // could be null
            topLeft.x += subGlyphWidth;
         }
      }
      return longestMatch; // might be null
   }

   /**
    * @return the width (in pixels) of the narrowest glyph.
    */
   public static int findMinimalWidth(Collection<FontGlyph> glyphs, int alreadySkippedColumns) {
      int minimalGlyphWidth = Integer.MAX_VALUE;
      for (FontGlyph glyph : glyphs) {
         minimalGlyphWidth = Math.min(minimalGlyphWidth, glyph.getDimension().width - alreadySkippedColumns);
      }
      return minimalGlyphWidth;
   }
}
