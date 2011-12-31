package org.designup.picsou.gui.components.charts.histo.line;

import org.designup.picsou.gui.components.charts.histo.*;
import org.globsframework.model.Key;

import java.awt.*;

public class HistoLinePainter implements HistoPainter {

  private HistoLineDataset dataset;
  private HistoLineColors colors;

  public HistoLinePainter(HistoLineDataset dataset, HistoLineColors colors) {
    this.dataset = dataset;
    this.colors = colors;
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public Key getObjectKeyAt(int x, int y) {
    return null;
  }

  public void paint(Graphics2D g2, HistoChartMetrics metrics, HistoChartConfig config, HistoRollover rollover) {

    if (dataset.size() == 0) {
      return;
    }

    int y0 = metrics.y(0);
    int previousY = metrics.y(dataset.getValue(0));

    for (int i = 0; i < dataset.size(); i++) {
      int left = metrics.left(i);
      int right = metrics.right(i);

      Double value = dataset.getValue(i);
      int y = metrics.y(value);

      int width = right - left;

      boolean isRollover = rollover.isOnColumn(i);

      boolean future = dataset.isFuture(i);
      boolean positive = value >= 0;
      colors.setFillStyle(g2, positive, dataset.isCurrent(i), future, dataset.isSelected(i), isRollover);
      if (y < y0) {
        g2.fillRect(left, y, width, y0 - y);
      }
      else {
        g2.fillRect(left, y0, width, y - y0);
      }

      colors.setLineStyle(g2, positive, future);
      g2.setStroke(new BasicStroke(2));
      g2.drawLine(left, previousY, left, y);
      g2.drawLine(left, y, right, y);
      previousY = y;
    }
  }
}