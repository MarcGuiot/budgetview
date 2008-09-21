package org.designup.picsou.gui.components;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;

public class StackChart extends JPanel {

  private StackChartElement[] elements = new StackChartElement[0];
  private double total = 0;
  private String longestLabel;
  private int[] blockHeights;
  private Color color1 = Color.RED;
  private Color color2 = Color.BLUE;
  private Color color3 = Color.BLACK;
  private Color color4 = Color.YELLOW;
  private Color color5 = Color.CYAN;
  private Color color6 = Color.PINK;
  private Color labelColor = Color.BLACK;
  private Color percentLabelColor = Color.BLACK;
  private Color remainderColor = Color.GRAY;

  private static final int COLOR_COUNT = 6;
  private static final int LABEL_HORIZONTAL_MARGIN = 5;
  private static final int LABEL_VERTICAL_MARGIN = 2;

  public StackChart() {
    setMinimumSize(new Dimension(200, 40));
    setPreferredSize(new Dimension(200, 1000));
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

    int blockX = 0;
    int blockWidth = percentX - LABEL_HORIZONTAL_MARGIN;

    int bottom = height;
    int colorIndex = 0;
    for (int i = 0; i < elements.length; i++) {
      int blockHeight = blockHeights[i];
      if (blockHeight == 0) {
        break;
      }

      int top = bottom - blockHeight;
      g2.setColor(getColor(colorIndex));
      g2.fillRect(blockX, top, blockWidth, blockHeight);

      g2.setColor(labelColor);
      int textX = (blockWidth / 2) - (metrics.stringWidth(elements[i].getLabel()) / 2);
      int textY = bottom - (blockHeight / 2) + (metrics.getAscent() / 2) - metrics.getDescent();
      g2.drawString(elements[i].getLabel(), textX, textY);

      String percentLabel = (int)(100 * elements[i].getValue() / total) + "%";
      g2.setColor(percentLabelColor);
      g2.drawString(percentLabel, percentX, textY);
      bottom = top;
      colorIndex = (colorIndex + 1) % COLOR_COUNT;
    }

    if (bottom > 0) {
      g2.setColor(remainderColor);
      g2.fillRect(blockX, 0, blockWidth, bottom);
    }
  }

  private Color getColor(int colorIndex) {
    switch (colorIndex) {
      case 0: return color1;
      case 1: return color2;
      case 2: return color3;
      case 3: return color4;
      case 4: return color5;
      case 5: return color6;
     }
    return Color.RED;
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

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  public void setPercentLabelColor(Color percentLabelColor) {
    this.percentLabelColor = percentLabelColor;
  }

  public void setColor1(Color color1) {
    this.color1 = color1;
  }

  public void setColor2(Color color2) {
    this.color2 = color2;
  }

  public void setColor3(Color color3) {
    this.color3 = color3;
  }

  public void setColor4(Color color4) {
    this.color4 = color4;
  }

  public void setColor5(Color color5) {
    this.color5 = color5;
  }

  public void setColor6(Color color6) {
    this.color6 = color6;
  }

  public void setRemainderColor(Color remainderColor) {
    this.remainderColor = remainderColor;
  }
}
