package org.designup.picsou.gui.components.charts.stack;

import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Strings;

import java.awt.*;
import java.util.ArrayList;

public class StackChartMetrics {

  private static final int TEXT_BAR_MARGIN = 10;
  private static final int VERTICAL_TEXT_MARGIN = 4;
  private static final int TOP_MARGIN = 10;

  private int panelHeight;
  private FontMetrics selectedLabelFontMetrics;
  private int panelWidth;
  private int drawingHeight;
  private FontMetrics labelFontMetrics;
  private FontMetrics barTextFontMetrics;
  private double maxValue;
  private int minTextHeight;
  private int barWidth;

  public StackChartMetrics(int panelHeight,
                           int panelWidth,
                           FontMetrics labelFontMetrics,
                           FontMetrics selectedLabelFontMetrics,
                           FontMetrics barTextFontMetrics,
                           double maxValue) {
    this.panelHeight = panelHeight;
    this.drawingHeight = panelHeight - TOP_MARGIN;
    this.panelWidth = panelWidth;
    this.labelFontMetrics = labelFontMetrics;
    this.selectedLabelFontMetrics = selectedLabelFontMetrics;
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

    int currentY = panelHeight;
    int remainingPercentage = 100;
    for (int i = 0; i < dataset.size(); i++) {
      double value = dataset.getValue(i);
      int height = (int)((value / maxValue) * drawingHeight);
      currentY -= height;
      int selectionIndex = i;

      String label;
      int percentage;
      if (height >= minTextHeight) {
        label = Strings.cut(dataset.getLabel(i), StackChart.MAX_LABEL_LENGTH);
        percentage = (int)Math.round(value * 100 / dataset.getTotal());
        remainingPercentage -= percentage;
      }
      else {
        if (i < dataset.size() - 1) {
          label = Lang.get("stackChart.other");
          double total = 0;
          for (int j = i + 1;j < dataset.size(); j++) {
            total += dataset.getValue(j);
          }
          int otherHeight = (int)((total / maxValue) * drawingHeight);
          height += otherHeight;
          currentY -= otherHeight;
          selectionIndex = -1;
        }
        else {
          label = Strings.cut(dataset.getLabel(i), StackChart.MAX_LABEL_LENGTH);
          int topY = (int)(drawingHeight * (1 - dataset.getTotal() / maxValue));
          height += currentY - topY;
          currentY = topY;
        }
        percentage = remainingPercentage;
        remainingPercentage = 0;
      }

      int labelTextY = currentY + height / 2 + labelFontMetrics.getMaxAscent() / 2;
      int barTextY = currentY + height / 2 + barTextFontMetrics.getMaxAscent() / 2;

      String percentageText = dataset.size() > 1 ? Integer.toString(percentage) + "%" : "";

      blocks.add(new StackChartBlock(dataset, selectionIndex,
                                     currentY,
                                     height,
                                     label,
                                     percentageText,
                                     labelTextY,
                                     barTextY,
                                     dataset.isSelected(i)));

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
        return panelWidth / 2 - barWidth / 2 - getStringWidth(text) / 2;
      }

      public int labelTextX(String text, boolean selected) {
        return panelWidth / 2 - barWidth - TEXT_BAR_MARGIN - getStringWidth(text, selected);
      }

      public int barX() {
        return panelWidth / 2 - barWidth;
      }

      public int blockX() {
        return 0;
      }

      public int blockWidth() {
        return panelWidth / 2;
      }
    };
  }

  public StackChartLayout rightLayout() {
    return new StackChartLayout() {
      public int barTextX(String text) {
        return panelWidth / 2 + barWidth / 2 - getStringWidth(text) / 2;
      }

      public int labelTextX(String text, boolean selected) {
        return panelWidth / 2 + barWidth + TEXT_BAR_MARGIN;
      }

      public int barX() {
        return panelWidth / 2;
      }

      public int blockX() {
        return panelWidth / 2;
      }

      public int blockWidth() {
        return panelWidth / 2;
      }
    };
  }

  public StackChartLayout centerLayout(String longestLabel) {

    final int labelWidth = getStringWidth(longestLabel, true);

    return new StackChartLayout() {
      public int barTextX(String text) {
        return barX() + (barWidth - getStringWidth(text)) / 2;
      }

      public int labelTextX(String text, boolean selected) {
        return barX() + barWidth + TEXT_BAR_MARGIN;
      }

      public int barX() {
        return panelWidth / 2 - (barWidth + TEXT_BAR_MARGIN + labelWidth) / 2;
      }

      public int blockX() {
        return 0;
      }

      public int blockWidth() {
        return panelWidth;
      }
    };
  }

  private int getStringWidth(String text) {
    return labelFontMetrics.stringWidth(text);
  }

  private int getStringWidth(String text, boolean selected) {
    return selected ? selectedLabelFontMetrics.stringWidth(text) : labelFontMetrics.stringWidth(text);
  }
}
