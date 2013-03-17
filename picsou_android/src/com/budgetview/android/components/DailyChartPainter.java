package com.budgetview.android.components;

import android.graphics.Canvas;
import android.graphics.Path;
import android.util.Log;
import com.budgetview.shared.gui.dailychart.HistoDailyMetrics;
import com.budgetview.shared.gui.histochart.HistoChartMetrics;
import com.budgetview.shared.gui.histochart.HistoDataset;
import com.budgetview.shared.utils.AmountFormat;

public class DailyChartPainter {

  private DailyDataset dataset;
  private DailyChartStyles styles;

  public DailyChartPainter(DailyDataset dataset, DailyChartStyles styles) {
    this.dataset = dataset;
    this.styles = styles;
  }

  protected void paint(Canvas canvas, HistoChartMetrics chartMetrics) {

    if (dataset.size() == 0) {
      return;
    }

    Double previousValue = null;
    Integer previousY = null;

    HistoDailyMetrics metrics = new HistoDailyMetrics(chartMetrics);
    int y0 = metrics.y(0);
    HistoDailyBlockPainter blockPainter = new HistoDailyBlockPainter(canvas, y0);

    for (int monthIndex = 0; monthIndex < dataset.size(); monthIndex++) {

      Double[] values = dataset.getValues(monthIndex);
      if (values.length == 0) {
        continue;
      }

      int left = metrics.left(monthIndex);
      int right = metrics.right(monthIndex);
      int width = right - left;
      int previousX = left;

      for (int dayIndex = 0; dayIndex < values.length; dayIndex++) {
        Double value = values[dayIndex];
        if (value == null) {
          continue;
        }

        Log.d("dailyChartPainter", "value= " + monthIndex + " / " + dayIndex + " ==> " + value);

        int x = left + (width * (dayIndex + 1)) / values.length;
        int y = metrics.y(value);
        if (previousY == null) {
          previousY = y;
          previousValue = value;
        }

        boolean current = dataset.isCurrent(monthIndex);
        boolean future = dataset.isFuture(monthIndex, dayIndex);
        boolean selected = dataset.isSelected(monthIndex);
        int blockWidth = width / values.length;

        if (dataset.isCurrent(monthIndex, dayIndex)) {
          canvas.drawLine(x, metrics.currentDayLineTop(), x, metrics.currentDayLineBottom(), styles.getCurrentDayColor());
          drawCurrentDayAnnotation(canvas, metrics, x);
        }

        if (Math.signum(previousValue) == Math.signum(value)) {
          blockPainter.draw(previousX, previousY, x, y, value >= 0, current, future, selected);
        }
        else {
          int x0 = previousX + (int)(blockWidth * Math.abs(previousValue) / (Math.abs(previousValue) + Math.abs(value)));
          blockPainter.draw(previousX, previousY, x0, y0, previousValue >= 0, current, future, selected);
          blockPainter.draw(x0, y0, x, y, value >= 0, current, future, selected);
        }

        previousX = x;
        previousY = y;
        previousValue = value;
      }

      drawMinLabel(canvas, dataset, monthIndex, metrics);
    }

    blockPainter.complete();
  }

  private void drawCurrentDayAnnotation(Canvas canvas, HistoDailyMetrics metrics, int x) {
    int labelX = x + metrics.currentDayXOffset();
    int labelY = metrics.currentDayY();
    String label = dataset.getCurrentDayLabel();
    canvas.drawText(label, labelX, labelY, styles.getCurrentDayAnnotation());
  }

  private void drawMinLabel(Canvas canvas, DailyDataset dataset, int monthIndex, HistoDailyMetrics metrics) {
    int minDayIndex = dataset.getMinDay(monthIndex);
    Double minValue = dataset.getValue(monthIndex, minDayIndex);
    if ((minValue == null) || !metrics.isDrawingInnerLabels()) {
      return;
    }

    String text = getMinText(minValue);
    int minX = metrics.minX(minDayIndex, dataset.getValues(monthIndex).length, monthIndex);

    canvas.drawText(text, metrics.innerLabelX(monthIndex, minX, text), metrics.innerLabelY(), styles.getInnerLabelColor(minValue));

    canvas.drawLine(metrics.innerLabelLineX(monthIndex, minX, text),
                    metrics.innerLabelLineY(),
                    minX,
                    metrics.y(minValue),
                    styles.getInnerLabelColor(minValue));
  }

  private String getMinText(Double minValue) {
    return AmountFormat.toStandardValueString(minValue);
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public class HistoDailyBlockPainter {

    private Canvas canvas;
    private int y0;

    private Path fillPath;
    private Path linePath;

    private Params lastParams;
    private int firstX;
    private int firstY;
    private int lastX;
    private int lastY;

    public HistoDailyBlockPainter(Canvas canvas, int y0) {
      this.canvas = canvas;
      this.y0 = y0;
    }

    public void draw(int previousX, Integer previousY, int x, int y,
                     boolean positive, boolean current, boolean future, boolean selected) {

      Params newParams = new Params(positive, current, future, selected);
      if (lastParams != null && !newParams.equals(lastParams)) {
        flush();
      }
      lastParams = newParams;

      if (fillPath == null) {
        fillPath = new Path();
        fillPath.moveTo(previousX, previousY);
        firstX = previousX;
        firstY = previousY;
      }
      fillPath.lineTo(x, y);

      if (linePath == null) {
        linePath = new Path();
        linePath.moveTo(previousX, previousY);
      }
      linePath.lineTo(x, y);

      lastX = x;
      lastY = y;
    }

    private void flush() {
      if (fillPath == null) {
        return;
      }
      fillPath.lineTo(lastX, y0);
      fillPath.lineTo(firstX, y0);
      fillPath.lineTo(firstX, firstY);
      fillPath.close();
      canvas.drawPath(fillPath, styles.getGraphBackground(lastParams.positive, lastParams.current, lastParams.future));
      fillPath = new Path();
      fillPath.moveTo(lastX, lastY);
      firstX = lastX;

      canvas.drawPath(linePath, styles.getGraphLine(lastParams.positive, lastParams.current, lastParams.future));
      linePath = new Path();
      linePath.moveTo(lastX, lastY);
    }

    public void complete() {
      flush();
    }

    private class Params {
      final boolean positive;
      final boolean current;
      final boolean future;
      final boolean selected;

      private Params(boolean positive, boolean current, boolean future, boolean selected) {
        this.positive = positive;
        this.current = current;
        this.future = future;
        this.selected = selected;
      }

      public boolean equals(Object o) {
        if (this == o) {
          return true;
        }
        if (o == null || getClass() != o.getClass()) {
          return false;
        }

        Params params = (Params)o;

        if (current != params.current) {
          return false;
        }
        if (future != params.future) {
          return false;
        }
        if (positive != params.positive) {
          return false;
        }
        if (selected != params.selected) {
          return false;
        }

        return true;
      }

      public int hashCode() {
        int result = (positive ? 1 : 0);
        result = 31 * result + (current ? 1 : 0);
        result = 31 * result + (future ? 1 : 0);
        result = 31 * result + (selected ? 1 : 0);
        return result;
      }
    }
  }
}
