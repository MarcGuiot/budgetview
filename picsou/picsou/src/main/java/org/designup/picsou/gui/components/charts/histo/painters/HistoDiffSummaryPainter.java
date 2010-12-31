package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;

import java.awt.*;

public class HistoDiffSummaryPainter implements HistoPainter {

  private HistoDiffDataset dataset;
  private boolean showReference;
  private HistoDiffColors colors;
  private BasicStroke referenceLineStroke;

  private static final int PADDING = 4;

  public HistoDiffSummaryPainter(HistoDiffDataset dataset, boolean showReference, HistoDiffColors colors) {
    this.dataset = dataset;
    this.showReference = showReference;
    this.colors = colors;
    this.referenceLineStroke = new BasicStroke(2);
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

      Double actual = dataset.getActualValue(i);
      boolean isRollover = (currentRollover != null) && (currentRollover == i);
      boolean isSelected = dataset.isSelected(i);

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, getFillAlpha(isSelected, isRollover)));
      g2.setColor(colors.getActualLineColor());
      g2.fillRect(left, metrics.barTop(actual),
                  width, metrics.barHeight(actual));
    }

    if (showReference) {
      g2.setComposite(AlphaComposite.Src);
      g2.setColor(colors.getReferenceLineColor());
      g2.setStroke(referenceLineStroke);

      int previousReferenceY = metrics.y(dataset.getReferenceValue(0));
      for (int i = 0; i < dataset.size(); i++) {
        int left = metrics.left(i);
        Double reference = dataset.getReferenceValue(i);
        int referenceY = metrics.y(reference);
        if (previousReferenceY != referenceY) {
          g2.drawLine(left, previousReferenceY, left, referenceY);
        }
        g2.drawLine(left, referenceY, metrics.right(i), referenceY);
      }
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
