package com.budgetview.desktop.components.charts.histo.button;

import com.budgetview.shared.gui.histochart.HistoChartMetrics;

import java.awt.*;

public class HistoButtonMetrics {

  private HistoChartMetrics metrics;
  private int rowCount;

  private static final int HORIZONTAL_MARGIN = 2;
  private static final int HORIZONTAL_LABEL_MARGIN = 4;
  private static final int VERTICAL_LABEL_MARGIN = 2;

  private int buttonHeight;
  private boolean canDrawLabels;
  private int buttonTopOffset;
  private int labelYOffset;

  public HistoButtonMetrics(HistoChartMetrics metrics, FontMetrics buttonFontMetrics, int rowCount) {
    this.metrics = metrics;
    this.rowCount = rowCount;

    int labelHeight = buttonFontMetrics.getAscent() + buttonFontMetrics.getDescent();
    int labelHeightWithMargin = labelHeight + 2 * VERTICAL_LABEL_MARGIN;
    int blockHeight = rowCount == 0 ? 0 : metrics.columnHeight() / rowCount;
    this.canDrawLabels = blockHeight > labelHeightWithMargin;
    this.labelYOffset = blockHeight / 2 - labelHeight / 2 + buttonFontMetrics.getAscent();
    this.buttonHeight = canDrawLabels ? labelHeightWithMargin : blockHeight;
    this.buttonTopOffset = blockHeight / 2 - buttonHeight / 2;
  }

  public Rectangle buttonRectangle(HistoButtonBlock block) {
    return new Rectangle(buttonX(block), buttonY(block), buttonWidth(block), buttonHeight);
  }

  private int buttonX(HistoButtonBlock block) {
    return metrics.left(block.minIndex) + HORIZONTAL_MARGIN;
  }

  private int buttonWidth(HistoButtonBlock block) {
    return metrics.right(block.maxIndex) - metrics.left(block.minIndex) - 2 * HORIZONTAL_MARGIN;
  }

  private int buttonY(HistoButtonBlock block) {
    return blockTop(block.row) + buttonTopOffset;
  }

  public boolean canDrawLabels() {
    return canDrawLabels;
  }

  public int labelX(HistoButtonBlock block) {
    return buttonX(block) + HORIZONTAL_LABEL_MARGIN;
  }

  public int labelY(HistoButtonBlock block) {
    return blockTop(block.row) + labelYOffset;
  }

  private int blockTop(int row) {
    return metrics.columnTop() + (row * metrics.columnHeight()) / rowCount;
  }
}
