package org.designup.picsou.gui.components.charts.histo.button;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.gui.histochart.HistoChartMetrics;
import com.budgetview.shared.gui.histochart.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.*;
import org.designup.picsou.gui.components.charts.histo.utils.BasicClickMap;
import org.globsframework.model.Key;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

public class HistoButtonPainter implements HistoPainter {
  private HistoButtonDataset dataset;
  private FontMetrics buttonFontMetrics;
  private HistoButtonColors colors;
  private BasicClickMap clickMap = new BasicClickMap();

  private static final int ARC_WIDTH = 5;
  private static final int ARC_HEIGHT = 10;

  public HistoButtonPainter(HistoButtonDataset dataset, FontMetrics buttonFontMetrics, HistoButtonColors colors) {
    this.dataset = dataset;
    this.buttonFontMetrics = buttonFontMetrics;
    this.colors = colors;
  }

  public void paint(Graphics2D g2, HistoChartMetrics chartMetrics, HistoChartConfig config, HistoRollover rollover) {

    clickMap.reset();

    HistoButtonMetrics metrics = new HistoButtonMetrics(chartMetrics, buttonFontMetrics, dataset.getRowCount());

    g2.setFont(buttonFontMetrics.getFont());

    for (HistoButtonBlock block : dataset.getBlocks()) {
      Rectangle rectangle = metrics.buttonRectangle(block);
      boolean isRolloverOnBlock = rollover.isOnObject(block.key);

      g2.setClip(rectangle.x, rectangle.y, rectangle.width + 1, rectangle.height + 1);

      g2.setPaint(new GradientPaint(0, rectangle.y, colors.getBgTopColor(isRolloverOnBlock),
                                    0, rectangle.y + rectangle.height, colors.getBgBottomColor(isRolloverOnBlock)));
      g2.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, ARC_WIDTH, ARC_HEIGHT);

      g2.setColor(colors.getBorderColor(isRolloverOnBlock));
      g2.drawRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height, ARC_WIDTH, ARC_HEIGHT);

      if (metrics.canDrawLabels()) {
        g2.setClip(rectangle.x, rectangle.y, rectangle.width - 2, rectangle.height);
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

  public Set<Key> getObjectKeysAt(int x, int y) {
    Key key = clickMap.getKey(x, y);
    if (key == null) {
      return Collections.emptySet();
    }
    return Collections.singleton(key);
  }
}
