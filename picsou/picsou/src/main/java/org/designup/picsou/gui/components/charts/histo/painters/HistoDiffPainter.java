package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;

import java.awt.*;

public class HistoDiffPainter implements HistoPainter {

  private HistoDiffDataset dataset;
  private HistoDiffColors colors;
  private boolean showActualInTheFuture;
  private static final BasicStroke LINE_STROKE = new BasicStroke(2);

  public HistoDiffPainter(HistoDiffDataset dataset, HistoDiffColors colors) {
    this(dataset, colors, true);
  }

  public HistoDiffPainter(HistoDiffDataset dataset, HistoDiffColors colors, boolean showActualInTheFuture) {
    this.dataset = dataset;
    this.colors = colors;
    this.showActualInTheFuture = showActualInTheFuture;
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public void paint(Graphics2D g2, HistoChartMetrics metrics, Integer currentRollover) {

    if (dataset.size() == 0) {
      return;
    }

    int previousReferenceY = metrics.y(dataset.getReferenceValue(0));
    int previousActualY = metrics.y(dataset.getActualValue(0));

    for (int i = 0; i < dataset.size(); i++) {
      int left = metrics.left(i);
      int right = metrics.right(i);

      Double reference = dataset.getReferenceValue(i);
      int referenceY = metrics.y(reference);

      Double actual = dataset.getActualValue(i);
      int actualY = metrics.y(actual);

      int width = right - left;

      boolean isRollover = (currentRollover != null) && (currentRollover == i);

      if (showActual(i)) {
        if (reference >= actual) {

          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, isRollover ? 0.9f : 0.6f));
          g2.setColor(colors.getReferenceOverrunColor());
          g2.fillRect(left, referenceY, width, actualY - referenceY);

          g2.setColor(colors.getFillColor(dataset.isSelected(i)));
          g2.fillRect(left, actualY, width, metrics.y(0) - actualY);
        }
        else {

          g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, isRollover ? 0.9f : 0.6f));
          g2.setColor(colors.getActualOverrunColor());
          g2.fillRect(left, actualY, width, referenceY - actualY);

          g2.setColor(colors.getFillColor(dataset.isSelected(i)));
          g2.fillRect(left, referenceY, width, metrics.y(0) - referenceY);
        }
      }

      g2.setComposite(AlphaComposite.Src);
      g2.setColor(colors.getReferenceLineColor());
      g2.setStroke(LINE_STROKE);
      g2.drawLine(left, previousReferenceY, left, referenceY);
      g2.drawLine(left, referenceY, right, referenceY);
      previousReferenceY = referenceY;

      if (showActual(i)) {
        g2.setColor(colors.getActualLineColor());
        g2.setStroke(LINE_STROKE);
        if (previousActualY != actualY) {
          g2.drawLine(left, previousActualY, left, actualY);
        }
        g2.drawLine(left, actualY, right, actualY);
        previousActualY = actualY;
      }
    }
  }

  private boolean showActual(int i) {
    return !dataset.isFuture(i) || showActualInTheFuture;
  }
}
