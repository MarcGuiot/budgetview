package org.designup.picsou.gui.components.charts.histo.painters;

import org.globsframework.gui.splits.color.Colors;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;

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

  public void paint(Graphics2D g2, HistoChartMetrics metrics) {

    if (dataset.getSize() == 0) {
      return;
    }

    int y0 = metrics.y(0);
    int previousY = metrics.y(dataset.getValue(0));

    for (int i = 0; i < dataset.getSize(); i++) {
      int left = metrics.left(i);
      int right = metrics.right(i);

      Double income = dataset.getValue(i);
      int y = metrics.y(income);

      int width = right - left;

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.6f));
      if (y < y0) {
        g2.setColor(colors.getPositiveFillColor());
        g2.fillRect(left, y, width, y0 - y);
      }
      else {
        g2.setColor(colors.getNegativeFillColor());
        g2.fillRect(left, y0, width, y - y0);
      }

      g2.setComposite(AlphaComposite.Src);
      g2.setColor(colors.getLineColor());
      g2.setStroke(new BasicStroke(2));
      g2.drawLine(left, previousY, left, y);
      g2.drawLine(left, y, right, y);
      previousY = y;
    }
  }
}