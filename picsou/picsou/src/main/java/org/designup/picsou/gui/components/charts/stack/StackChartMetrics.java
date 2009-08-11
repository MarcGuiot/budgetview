package org.designup.picsou.gui.components.charts.stack;

import org.designup.picsou.utils.Lang;

import java.awt.*;
import java.util.ArrayList;

public class StackChartMetrics {

  private static int OUTER_HORIZONTAL_MARGIN = 10;
  private static int TEXT_BAR_MARGIN = 10;
  private static final int VERTICAL_TEXT_MARGIN = 4;

  private int panelHeight;
  private int panelWidth;
  private FontMetrics labelFontMetrics;
  private FontMetrics barTextFontMetrics;
  private double maxValue;
  private int minTextHeight;
  private int barWidth;

  public StackChartMetrics(int panelHeight,
                           int panelWidth,
                           FontMetrics labelFontMetrics,
                           FontMetrics barTextFontMetrics,
                           double maxValue) {
    this.panelHeight = panelHeight;
    this.panelWidth = panelWidth;
    this.labelFontMetrics = labelFontMetrics;
    this.barTextFontMetrics = barTextFontMetrics;
    this.maxValue = maxValue;
    int fontHeight = labelFontMetrics.getMaxAscent() - labelFontMetrics.getMaxDescent();
    this.minTextHeight = fontHeight + VERTICAL_TEXT_MARGIN;
    this.barWidth = Math.min(40, 10 + labelFontMetrics.stringWidth("100%"));
  }

  public StackChartBlock[] computeBlocks(StackChartDataset dataset) {

    if (maxValue == 0) {
      return new StackChartBlock[0];
    }

    java.util.List<StackChartBlock> blocks = new ArrayList<StackChartBlock>();

    int remainingHeight = panelHeight;
    int remainingPercentage = 100;
    for (int i = 0; i < dataset.size(); i++) {
      double value = dataset.getValue(i);
      int height = (int)((value / maxValue) * panelHeight);
      remainingHeight -= height;

      String label;
      int percentage;
      if (height >= minTextHeight) {
        label = dataset.getLabel(i);
        percentage = (int)Math.round(value * 100 / dataset.getTotal());
        remainingPercentage -= percentage;
      }
      else {
        if (i < dataset.size() - 1) {
          label = Lang.get("stackChart.other");
        }
        else {
          label = dataset.getLabel(i);
        }
        percentage = remainingPercentage;
        remainingPercentage = 0;
        height += remainingHeight;
        remainingHeight = 0;
      }

      int labelTextY = remainingHeight + height / 2 + labelFontMetrics.getMaxAscent() / 2;
      int barTextY = remainingHeight + height / 2 + barTextFontMetrics.getMaxAscent() / 2;

      String percentageText = dataset.size() > 1 ? Integer.toString(percentage) + "%" : "";

      blocks.add(new StackChartBlock(remainingHeight,
                                     height,
                                     label,
                                     percentageText,
                                     labelTextY,
                                     barTextY));

      if (remainingPercentage == 0) {
        break;
      }
    }

    return blocks.toArray(new StackChartBlock[blocks.size()]);
  }

  public int barWidth() {
    return barWidth;
  }

  public StackChartLayout leftLayout() {
    return new StackChartLayout() {
      public int barTextX(String text) {
        return panelWidth / 2 - barWidth / 2 - labelFontMetrics.stringWidth(text) / 2;
      }

      public int labelTextX(String text) {
        return panelWidth / 2 - barWidth - TEXT_BAR_MARGIN - labelFontMetrics.stringWidth(text);
      }

      public int barX() {
        return panelWidth / 2 - barWidth;
      }
    };
  }

  public StackChartLayout rightLayout() {
    return new StackChartLayout() {
      public int barTextX(String text) {
        return panelWidth / 2 + barWidth / 2 - labelFontMetrics.stringWidth(text) / 2;
      }

      public int labelTextX(String text) {
        return panelWidth / 2 + barWidth + TEXT_BAR_MARGIN;
      }

      public int barX() {
        return panelWidth / 2;
      }
    };
  }

  public StackChartLayout centerLayout(String longestLabel) {

    final int labelWidth = labelFontMetrics.stringWidth(longestLabel);

    return new StackChartLayout() {
      public int barTextX(String text) {
        return barX() + (barWidth - labelFontMetrics.stringWidth(text)) / 2;
      }

      public int labelTextX(String text) {
        return barX() + barWidth + TEXT_BAR_MARGIN;
      }

      public int barX() {
        return panelWidth / 2 - (barWidth + TEXT_BAR_MARGIN + labelWidth) / 2;
      }

    };
  }
}
