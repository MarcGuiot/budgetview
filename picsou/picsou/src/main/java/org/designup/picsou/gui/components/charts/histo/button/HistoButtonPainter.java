package org.designup.picsou.gui.components.charts.histo.button;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;
import org.designup.picsou.gui.components.charts.histo.HistoRollover;
import org.designup.picsou.gui.components.charts.histo.utils.BasicClickMap;
import org.globsframework.model.Key;

import java.awt.*;

public class HistoButtonPainter implements HistoPainter {
  private HistoButtonDataset dataset;
  private HistoButtonColors colors;
  private BasicClickMap clickMap = new BasicClickMap();

  private static final int ARC_WIDTH = 5;
  private static final int ARC_HEIGHT = 10;

  public HistoButtonPainter(HistoButtonDataset dataset, HistoButtonColors colors) {
    this.dataset = dataset;
    this.colors = colors;
  }

  public void paint(Graphics2D g2, HistoChartMetrics chartMetrics, HistoRollover rollover) {

    clickMap.reset();

    HistoButtonMetrics metrics = new HistoButtonMetrics(chartMetrics, dataset.getRowCount());

    for (HistoButtonBlock block : dataset.getBlocks()) {
      Rectangle rectangle = metrics.buttonRectangle(block);
      boolean isRolloverOnBlock = rollover.isOnObject(block.key);

      g2.setPaint(new GradientPaint(0, rectangle.y, colors.getBgTopColor(isRolloverOnBlock),
                                    0, rectangle.y + rectangle.height, colors.getBgBottomColor(isRolloverOnBlock)));
      g2.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, ARC_WIDTH, ARC_HEIGHT);

      g2.setColor(colors.getBorderColor(isRolloverOnBlock));
      g2.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, ARC_WIDTH, ARC_HEIGHT);

      if (metrics.canDrawLabels()) {
        g2.setColor(colors.getLabelShadowColor());
        g2.drawString(block.label, metrics.labelX(block) + 1, metrics.labelY(block) + 1);
        g2.setColor(colors.getLabelColor(isRolloverOnBlock));
        g2.drawString(block.label, metrics.labelX(block), metrics.labelY(block));
      }

      clickMap.add(rectangle, block.key);
    }
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public Key getObjectKeyAt(int x, int y) {
    return clickMap.getKey(x, y);
  }
}