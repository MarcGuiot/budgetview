package org.designup.picsou.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;

public class StackChart extends JPanel {

  private StackChartElement[] elements = new StackChartElement[0];
  private double total = 0;
  private String longestLabel;
  private int[] blockHeights;
  private Color[] colors;

  private static final int LABEL_HORIZONTAL_MARGIN = 5;
  private static final int LABEL_VERTICAL_MARGIN = 2;

  public StackChart() {
    colors = new Color[]{Color.blue, Color.red, Color.green};
    setMinimumSize(new Dimension(150, 40));
  }

  public void setValues(SortedSet<StackChartElement> elements) {
    this.elements = elements.toArray(new StackChartElement[elements.size()]);
    this.total = 0;
    this.longestLabel = "";
    for (StackChartElement element : this.elements) {
      total += element.getValue();
      if (element.getLabel().length() > longestLabel.length()) {
        longestLabel = element.getLabel();
      }
    }

    clearSizes();
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, width, height);
    }

    FontMetrics metrics = g2.getFontMetrics();
    int fontHeight = metrics.getMaxAscent() - metrics.getMaxDescent();
    if (blockHeights == null) {
      computeSizes(height, fontHeight);
    }

    int percentX = width - metrics.stringWidth("99%");

    int blockX = metrics.stringWidth(longestLabel) + LABEL_HORIZONTAL_MARGIN;
    int blockWidth = percentX - LABEL_HORIZONTAL_MARGIN - blockX;

    int bottom = height;
    int colorIndex = 0;
    for (int i = 0; i < elements.length; i++) {
      int blockHeight = blockHeights[i];
      if (blockHeight == 0) {
        break;
      }

      int top = bottom - blockHeight;
      g2.setColor(colors[colorIndex % colors.length]);
      g2.fillRect(blockX, top, blockWidth, blockHeight);

      g2.setColor(Color.BLACK);
      int textX = blockX - LABEL_HORIZONTAL_MARGIN - metrics.stringWidth(elements[i].getLabel());
      int textY = bottom - (blockHeight / 2) + (metrics.getAscent() / 2) - metrics.getDescent();
      g2.drawString(elements[i].getLabel(), textX, textY);

      String percentLabel = (int)(100 * elements[i].getValue() / total) + "%";
      g2.drawString(percentLabel, percentX, textY);
      bottom = top;
      colorIndex++;
    }

    if (bottom > 0) {
      g2.setColor(Color.gray);
      g2.fillRect(blockX, 0, blockWidth, bottom);
    }
  }

  private void clearSizes() {
    this.blockHeights = null;
    this.longestLabel = "";
  }

  private void computeSizes(int height, int fontHeight) {
    blockHeights = new int[this.elements.length];
    for (int i = 0; i < elements.length; i++) {
      double percentage = elements[i].getValue() / total;
      int blockHeight = (int)(percentage * height);
      if (blockHeight < (fontHeight + LABEL_VERTICAL_MARGIN)) {
        blockHeights[i] = 0;
      }
      else {
        blockHeights[i] = blockHeight;
        if (elements[i].getLabel().length() > longestLabel.length()) {
          longestLabel = elements[i].getLabel();
        }
      }
    }
  }

  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    clearSizes();
  }

  public void setFont(Font font) {
    super.setFont(font);
    clearSizes();
  }

}
