package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;

import java.awt.*;

public class HistoDoubleLinePainter implements HistoPainter {

  private HistoDiffDataset dataset;
  private HistoDiffColors colors;

  private BasicStroke actualLineStroke;
  private BasicStroke referenceLineStroke;

  public HistoDoubleLinePainter(HistoDiffDataset dataset, HistoDiffColors colors) {
    this.dataset = dataset;
    this.colors = colors;
    this.actualLineStroke = new BasicStroke(2);
    this.referenceLineStroke = new BasicStroke(1);
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
      boolean isSelected = dataset.isSelected(i);

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, getFillAlpha(isSelected, isRollover)));
      g2.setColor(colors.getFillColor());
      g2.fillRect(left, actualY, width, metrics.y(0) - actualY);

      g2.setComposite(AlphaComposite.Src);
      g2.setColor(colors.getReferenceLineColor());
      g2.setStroke(referenceLineStroke);
      if (previousReferenceY != referenceY) {
      g2.drawLine(left, previousReferenceY, left, referenceY);
      }
      g2.drawLine(left, referenceY, right, referenceY);
      previousReferenceY = referenceY;

      g2.setColor(colors.getActualLineColor());
      g2.setStroke(actualLineStroke);
      if (previousActualY != actualY) {
        g2.drawLine(left, previousActualY, left, actualY);
      }
      g2.drawLine(left, actualY, right, actualY);
      previousActualY = actualY;
    }
  }

  private float getFillAlpha(boolean selected, boolean rollover) {
    if (selected) {
      return 0.9f;
    }
    if (rollover) {
      return 0.75f;
    }
    return 0.5f;
  }
}
