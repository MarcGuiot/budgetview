package org.designup.picsou.gui.components.charts.stack;

import javax.swing.*;
import java.awt.*;

public class StackChart extends JPanel {

  private StackChartDataset leftDataset;
  private StackChartDataset rightDataset;
  private StackChartColors colors;

  private Font labelFont;
  private Font barTextFont;

  private static final BasicStroke SELECTION_STROKE = new BasicStroke(2);
  private static final BasicStroke BORDER_STROKE = new BasicStroke(1);

  public StackChart() {
    setMinimumSize(new Dimension(150, 40));
    setPreferredSize(new Dimension(150, 1000));
    labelFont = getFont().deriveFont(9f);
    barTextFont = labelFont;
  }

  public void update(StackChartDataset dataset,
                     StackChartColors colors) {
    this.leftDataset = dataset;
    this.rightDataset = null;
    this.colors = colors;
    repaint();
  }

  public void update(StackChartDataset leftDataset,
                     StackChartDataset rightDataset,
                     StackChartColors colors) {
    this.leftDataset = leftDataset;
    this.rightDataset = rightDataset;
    this.colors = colors;
    repaint();
  }

  public void clear() {
    update(null, null, null);
    repaint();
  }

  public StackChartDataset getLeftDataset() {
    return leftDataset;
  }

  public StackChartDataset getRightDataset() {
    return rightDataset;
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

    if ((leftDataset != null) && (rightDataset != null)) {
      StackChartMetrics metrics =
        new StackChartMetrics(height, width,
                              g2.getFontMetrics(labelFont), g2.getFontMetrics(barTextFont),
                              Math.max(leftDataset.getTotal(), rightDataset.getTotal()));
      paintBlocks(g2, metrics,
                  metrics.computeBlocks(leftDataset),
                  metrics.leftLayout(),
                  colors, colors.getLeftBarColor());
      paintBlocks(g2, metrics,
                  metrics.computeBlocks(rightDataset),
                  metrics.rightLayout(),
                  colors, colors.getRightBarColor());
    }
    else if (leftDataset != null) {
      StackChartMetrics metrics =
        new StackChartMetrics(height, width,
                              g2.getFontMetrics(labelFont), g2.getFontMetrics(barTextFont),
                              leftDataset.getTotal());
      paintBlocks(g2, metrics,
                  metrics.computeBlocks(leftDataset),
                  metrics.centerLayout(leftDataset.getLongestLabel()),
                  colors, colors.getLeftBarColor());
    }

    if (colors != null) {
      g2.setStroke(BORDER_STROKE);
      g2.setColor(colors.getBorderColor());
      g2.drawLine(0, height, width, height);
    }
  }

  private void paintBlocks(Graphics2D g2,
                           StackChartMetrics metrics,
                           StackChartBlock[] blocks,
                           StackChartLayout layout,
                           StackChartColors colors,
                           Color barColor) {
    float alpha = 1.0f;
    for (StackChartBlock block : blocks) {
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
      g2.setColor(barColor);
      g2.fillRect(layout.barX(), block.blockY, metrics.barWidth(), block.blockHeight);

      g2.setComposite(AlphaComposite.Src);

      if (block.selected) {
        g2.setColor(colors.getSelectionBorderColor());
        g2.setStroke(SELECTION_STROKE);
        g2.drawRect(layout.barX(), block.blockY, metrics.barWidth(), block.blockHeight);

      }

      g2.setColor(colors.getBarTextColor());
      g2.setFont(barTextFont);
      g2.drawString(block.barText, layout.barTextX(block.barText), block.barTextY);

      g2.setColor(block.selected ? colors.getSelectionTextColor() : colors.getLabelColor());
      g2.setFont(labelFont);
      g2.drawString(block.label, layout.labelTextX(block.label), block.labelTextY);

      alpha *= 0.7f;
    }
  }
}
