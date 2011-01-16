package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;

import java.awt.*;

public class HistoDailyPainter implements HistoPainter {

  private HistoDailyDataset dataset;
  private HistoLineColors colors;

  public static final BasicStroke LINE_STROKE = new BasicStroke(1);

  public HistoDailyPainter(HistoDailyDataset dataset, HistoLineColors colors) {
    this.dataset = dataset;
    this.colors = colors;
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public void paint(Graphics2D g2, HistoChartMetrics metrics, Integer currentRollover) {

    if (dataset.size() == 0) {
      return;
    }

    g2.setStroke(LINE_STROKE);

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

      colors.setVerticalDividerStyle(g2);
      g2.drawLine(right, metrics.columnTop(), right, metrics.columnTop() + metrics.columnHeight());

      for (int j = 0; j < values.length; j++) {
        Double value = values[j];
        if (value == null) {
          continue;
        }

        int x = left + (width * (j + 1)) / values.length;
        int y = metrics.y(value);
        if (previousY == null) {
          previousY = y;
          previousValue = value;
        }

        boolean current = dataset.isCurrent(i);
        boolean future = dataset.isFuture(i, j);
        boolean selected = dataset.isSelected(i);
        boolean rollover = (currentRollover != null) && (currentRollover == i);

        if (Math.signum(previousValue) == Math.signum(value)) {
          drawBlock(g2, previousX, previousY, x, y, y0, value >= 0, current, future, selected, rollover);
        }
        else {
          int blockWidth = width / values.length;
          int x0 = previousX + (int)(blockWidth * Math.abs(previousValue) / (Math.abs(previousValue) + Math.abs(value)));
          drawBlock(g2, previousX, previousY, x0, y0, y0, previousValue >= 0, current, future, selected, rollover);
          drawBlock(g2, x0, y0, x, y, y0, value >= 0, current, future, selected, rollover);
        }

        previousX = x;
        previousY = y;
        previousValue = value;
      }
    }
  }

  private void drawBlock(Graphics2D g2, int previousX, Integer previousY, int x, int y, int y0,
                         boolean positive, boolean current, boolean future, boolean selected, boolean rollover) {

    colors.setFillStyle(g2, positive, current, future, selected, rollover);
    Polygon polygon = new Polygon();
    polygon.addPoint(previousX,previousY);
    polygon.addPoint(x,y);
    polygon.addPoint(x,y0);
    polygon.addPoint(previousX,y0);
    g2.fill(polygon);

    colors.setLineStyle(g2, positive, future);
    g2.drawLine(previousX, previousY, x, y);
  }
}