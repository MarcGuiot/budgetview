package org.designup.picsou.gui.components.charts.histo.painters;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;

import java.awt.*;

public class HistoDiffBarLinePainter implements HistoPainter {

  private static final BasicStroke DEFAULT_LINE_STROKE = new BasicStroke(2);

  private HistoDiffDataset dataset;
  private HistoDiffColors colors;
  private boolean showActualInTheFuture;
  private BasicStroke actualLineStroke;

  private static final int PADDING = 4;

  public HistoDiffBarLinePainter(HistoDiffDataset dataset, HistoDiffColors colors, boolean showActualInTheFuture) {
    this.dataset = dataset;
    this.colors = colors;
    this.showActualInTheFuture = showActualInTheFuture;
    this.actualLineStroke = DEFAULT_LINE_STROKE;
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

      Double reference = dataset.getReferenceValue(i);
      boolean isRollover = (currentRollover != null) && (currentRollover == i);
      boolean isSelected = dataset.isSelected(i);

      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, getFillAlpha(isSelected, isRollover)));
      g2.setColor(colors.getReferenceLineColor());
      g2.fillRect(left, metrics.barTop(reference),
                  width, metrics.barHeight(reference));
      g2.setComposite(AlphaComposite.Src);
    }

    g2.setColor(colors.getActualLineColor());
    g2.setStroke(actualLineStroke);

    int currentY = metrics.y(dataset.getActualValue(0));
    if (dataset.size() == 1) {
      g2.drawLine(metrics.left(0), currentY, metrics.right(0), currentY);
      g2.fillOval(metrics.middleX(0), currentY, 2, 2);
      return;
    }

    int currentX = metrics.middleX(0);
    g2.fillOval(currentX - 3, currentY - 3, 6, 6);
    for (int i = 1; i < dataset.size(); i++) {
      if (showActual(i)) {
        int newX = metrics.middleX(i);
        int newY = metrics.y(dataset.getActualValue(i));
        g2.drawLine(currentX, currentY, newX, newY);
        g2.fillOval(newX - 3, newY - 3, 6, 6);
        currentX = newX;
        currentY = newY;
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

  private boolean showActual(int i) {
    return !dataset.isFuture(i) || showActualInTheFuture;
  }
}
