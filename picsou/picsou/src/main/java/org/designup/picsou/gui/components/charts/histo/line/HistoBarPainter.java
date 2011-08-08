package org.designup.picsou.gui.components.charts.histo.line;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;
import org.designup.picsou.gui.components.charts.histo.HistoRollover;
import org.globsframework.model.Key;

import java.awt.*;

public class HistoBarPainter implements HistoPainter {

  private HistoLineDataset dataset;
  private HistoLineColors colors;

  private static final int PADDING = 4;

  public HistoBarPainter(HistoLineDataset dataset, HistoLineColors colors) {
    this.dataset = dataset;
    this.colors = colors;
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public Key getObjectKeyAt(int x, int y) {
    return null;
  }

  public void paint(Graphics2D g2, HistoChartMetrics metrics, HistoRollover rollover) {

    if (dataset.size() == 0) {
      return;
    }

    for (int i = 0; i < dataset.size(); i++) {
      int left = metrics.left(i) + PADDING;
      int width = metrics.columnWidth() - 2 * PADDING;

      Double value = dataset.getValue(i);
      boolean isRollover = rollover.isOnColumn(i);

      colors.setFillStyle(g2, value >= 0,
                          dataset.isCurrent(i), dataset.isFuture(i),
                          dataset.isSelected(i), isRollover);
      g2.fillRect(left, metrics.barTop(value), width, metrics.barHeight(value));
    }
  }
}
