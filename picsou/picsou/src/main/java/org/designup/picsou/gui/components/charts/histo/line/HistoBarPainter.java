package org.designup.picsou.gui.components.charts.histo.line;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.gui.histochart.HistoChartMetrics;
import com.budgetview.shared.gui.histochart.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.*;
import org.globsframework.model.Key;

import java.awt.*;
import java.util.Set;

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

  public Set<Key> getObjectKeysAt(int x, int y) {
    return dataset.getKeys();
  }

  public void paint(Graphics2D g2, HistoChartMetrics metrics, HistoChartConfig config, HistoRollover rollover) {

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
