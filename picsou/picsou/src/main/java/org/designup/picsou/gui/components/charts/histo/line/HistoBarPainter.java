package org.designup.picsou.gui.components.charts.histo.line;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;

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

  public void paint(Graphics2D g2, HistoChartMetrics metrics, Integer currentRollover) {

    if (dataset.size() == 0) {
      return;
    }

    for (int i = 0; i < dataset.size(); i++) {
      int left = metrics.left(i) + PADDING;
      int width = metrics.columnWidth() - 2 * PADDING;

      Double value = dataset.getValue(i);
      boolean isRollover = (currentRollover != null) && (currentRollover == i);
      boolean isSelected = dataset.isSelected(i);

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, getFillAlpha(isSelected, isRollover)));
      g2.setColor(value >=0 ? colors.getPositiveFillColor() : colors.getNegativeFillColor());
      g2.fillRect(left, metrics.barTop(value), width, metrics.barHeight(value));
      g2.setComposite(AlphaComposite.Src);
    }
  }

  private float getFillAlpha(boolean selected, boolean rollover) {
    if (selected) {
      return 0.9f;
    }
    if (rollover) {
      return 0.75f;
    }
    return 0.3f;
  }
}
