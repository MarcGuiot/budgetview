package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;
import org.designup.picsou.gui.description.Formatting;

import java.awt.*;

public class HistoDailyPainter implements HistoPainter {

  private HistoDailyDataset dataset;
  private HistoDailyColors colors;

  public static final BasicStroke DEFAULT_LINE_STROKE = new BasicStroke(1);
  public static final BasicStroke FUTURE_LINE_STROKE =
    new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, new float[]{2, 3}, 0);

  public HistoDailyPainter(HistoDailyDataset dataset, HistoDailyColors colors) {
    this.dataset = dataset;
    this.colors = colors;
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public void paint(Graphics2D g2, HistoChartMetrics chartMetrics, Integer rolloverColumnIndex) {

    HistoDailyMetrics metrics = new HistoDailyMetrics(chartMetrics);
    
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (dataset.size() == 0) {
      return;
    }

    g2.setStroke(DEFAULT_LINE_STROKE);

    Double previousValue = null;
    Integer previousY = null;

    for (int i = 0; i < dataset.size(); i++) {

      Double[] values = dataset.getValues(i);
      if (values.length == 0) {
        continue;
      }

      int left = metrics.left(i);
      int right = metrics.right(i);
      int width = right - left;
      int previousX = left;
      int y0 = metrics.y(0);

      colors.line.setVerticalDividerStyle(g2);
      g2.drawLine(right, metrics.columnTop(), right, metrics.columnTop() + metrics.columnHeight());

      for (int day = 0; day < values.length; day++) {
        Double value = values[day];
        if (value == null) {
          continue;
        }

        int x = left + (width * (day + 1)) / values.length;
        int y = metrics.y(value);
        if (previousY == null) {
          previousY = y;
          previousValue = value;
        }

        boolean current = dataset.isCurrent(i);
        boolean future = dataset.isFuture(i, day);
        boolean selected = dataset.isSelected(i);
        boolean rollover = (rolloverColumnIndex != null) && (rolloverColumnIndex == i);
        int blockWidth = width / values.length;

        if (dataset.isDaySelected(i, day)) {
          g2.setColor(colors.getSelectedDayColor());
          g2.fillRect(previousX, metrics.columnTop() + 1, blockWidth, metrics.columnHeight() - 1);
        }

        if (dataset.isCurrent(i, day)) {
          g2.setColor(colors.getCurrentDayColor());
          g2.drawLine(x, metrics.currentDayLineTop(), x, metrics.currentDayLineBottom());
        }

        if (Math.signum(previousValue) == Math.signum(value)) {
          drawBlock(g2, previousX, previousY, x, y, y0, value >= 0, current, future, selected, rollover);
        }
        else {
          int x0 = previousX + (int)(blockWidth * Math.abs(previousValue) / (Math.abs(previousValue) + Math.abs(value)));
          drawBlock(g2, previousX, previousY, x0, y0, y0, previousValue >= 0, current, future, selected, rollover);
          drawBlock(g2, x0, y0, x, y, y0, value >= 0, current, future, selected, rollover);
        }

        previousX = x;
        previousY = y;
        previousValue = value;
      }

      drawMinLabel(g2, dataset, i, metrics);
    }
  }

  private void drawBlock(Graphics2D g2, int previousX, Integer previousY, int x, int y, int y0,
                         boolean positive, boolean current, boolean future, boolean selected, boolean rollover) {

    colors.line.setFillStyle(g2, positive, current, future, selected, rollover);
    Polygon polygon = new Polygon();
    polygon.addPoint(previousX, previousY);
    polygon.addPoint(x, y);
    polygon.addPoint(x, y0);
    polygon.addPoint(previousX, y0);
    g2.fill(polygon);

    g2.setStroke(future ? FUTURE_LINE_STROKE : DEFAULT_LINE_STROKE);
    colors.line.setLineStyle(g2, positive, future);
    g2.drawLine(previousX, previousY, x, y);
  }

  private void drawMinLabel(Graphics2D g2, HistoDailyDataset dataset, int index, HistoDailyMetrics metrics) {
    int minDay = dataset.getMinDay(index);
    Double minValue = dataset.getValue(index, minDay);
    if ((minValue == null) || !metrics.isDrawingInnerLabels()) {
      return;
    }

    String text = getMinText(index, minValue);
    int minX = metrics.minX(minDay, dataset.getValues(index).length, index);

    g2.setStroke(new BasicStroke(1));
    g2.setComposite(AlphaComposite.Src);
    g2.setColor(colors.getInnerLabelColor(minValue));
    g2.drawString(text, metrics.innerLabelX(index, minX, text), metrics.innerLabelY());

    g2.drawLine(metrics.innerLabelLineX(index, minX, text),
                metrics.innerLabelLineY(),
                minX,
                metrics.y(minValue));
  }

  private String getMinText(int index, Double minValue) {
    return Formatting.toMinimumValueString(minValue);
  }
}