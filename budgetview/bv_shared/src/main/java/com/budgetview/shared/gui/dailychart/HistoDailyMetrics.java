package com.budgetview.shared.gui.dailychart;

import com.budgetview.shared.gui.histochart.HistoChartMetrics;

public class HistoDailyMetrics {

  private static final int INNER_LABEL_PADDING = 5;

  private HistoChartMetrics metrics;

  public HistoDailyMetrics(HistoChartMetrics metrics) {
    this.metrics = metrics;
  }

  public int left(int i) {
    return metrics.left(i);
  }

  public int right(int i) {
    return metrics.right(i);
  }

  public int y(double value) {
    return metrics.y(value);
  }

  public int columnTop() {
    return metrics.columnTop();
  }

  public int columnHeight() {
    return metrics.columnHeight();
  }

  public boolean isDrawingInnerLabels() {
    return metrics.isDrawingInnerLabels();
  }

  public int minX(int minDay, int dayCount, int index) {
    return metrics.left(index) + (metrics.columnWidth() * (minDay + 1)) / dayCount;
  }

  public int innerLabelX(int index, int minX, String text) {
    int textWidth = metrics.labelTextWidth(text);
    int midColumnX = metrics.left(index) + metrics.columnWidth() / 2;

    if ((minX > midColumnX) && (minX + textWidth / 2 + INNER_LABEL_PADDING > metrics.right(index))) {
      return metrics.right(index) - textWidth - INNER_LABEL_PADDING;

    }
    else if ((minX < midColumnX) && (minX - textWidth / 2 - INNER_LABEL_PADDING < metrics.left(index))) {
      return metrics.left(index) + INNER_LABEL_PADDING;
    }
    return minX - textWidth / 2;
  }

  public int innerLabelLineX(int index, int minX, String text) {
    int textWidth = metrics.labelTextWidth(text);
    return innerLabelX(index, minX, text) + textWidth / 2;
  }

  public int innerLabelLineY() {
    return metrics.innerLabelY() - 10;
  }

  public int innerLabelY() {
    return metrics.innerLabelY();
  }

  public int currentDayLineTop() {
    return metrics.columnTop();
  }

  public int currentDayLineBottom() {
    return metrics.usableColumnBottom();
  }

  public int currentDayXOffset() {
    return 4;
  }
  
  public int currentDayY() {
    return currentDayLineTop() + 12;
  }
}
