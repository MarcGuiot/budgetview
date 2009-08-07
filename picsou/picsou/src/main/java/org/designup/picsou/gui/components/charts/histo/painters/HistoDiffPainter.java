package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;

import java.awt.*;

public class HistoDiffPainter implements HistoPainter {

  private HistoDiffDataset dataset;
  private HistoDiffColors colors;

  public HistoDiffPainter(HistoDiffDataset dataset, HistoDiffColors colors) {
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

    int previousReferenceY = metrics.y(dataset.getReferenceValue(0));
    int previousActualY = metrics.y(dataset.getActualValue(0));

    for (int i = 0; i < dataset.getSize(); i++) {
      int left = metrics.left(i);
      int right = metrics.right(i);

      Double reference = dataset.getReferenceValue(i);
      int referenceY = metrics.y(reference);

      Double actual = dataset.getActualValue(i);
      int actualY = metrics.y(actual);

      int width = right - left;

      if (reference >= actual) {

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.6f));
        g2.setColor(colors.getReferenceOverrunColor());
        g2.fillRect(left, referenceY, width, actualY - referenceY);

        g2.setColor(colors.getFillColor(dataset.isSelected(i)));
        g2.fillRect(left, actualY, width, metrics.y(0) - actualY);
      }
      else {

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.6f));
        g2.setColor(colors.getActualOverrunColor());
        g2.fillRect(left, actualY, width, referenceY - actualY);

        g2.setColor(colors.getFillColor(dataset.isSelected(i)));
        g2.fillRect(left, referenceY, width, metrics.y(0) - referenceY);
      }

      g2.setComposite(AlphaComposite.Src);
      g2.setColor(colors.getReferenceLineColor());
      g2.setStroke(getStroke(i));
      g2.drawLine(left, previousReferenceY, left, referenceY);
      g2.drawLine(left, referenceY, right, referenceY);
      previousReferenceY = referenceY;

      g2.setColor(colors.getActualLineColor());
      g2.setStroke(getStroke(i));
      g2.drawLine(left, previousActualY, left, actualY);
      g2.drawLine(left, actualY, right, actualY);
      previousActualY = actualY;

    }
  }

  private BasicStroke getStroke(int index) {
    if (dataset.isFuture(index)) {
      return new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                             0.f, new float[]{4, 4}, 0.f);

    }
    else {
      return new BasicStroke(2);
    }
  }
}
