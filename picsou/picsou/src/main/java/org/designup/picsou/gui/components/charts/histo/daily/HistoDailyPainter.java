package org.designup.picsou.gui.components.charts.histo.daily;

import org.designup.picsou.gui.components.charts.histo.HistoChartMetrics;
import org.designup.picsou.gui.components.charts.histo.HistoDataset;
import org.designup.picsou.gui.components.charts.histo.HistoPainter;
import org.designup.picsou.gui.components.charts.histo.HistoRollover;
import org.designup.picsou.gui.components.charts.histo.utils.HorizontalBlocksClickMap;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.model.Key;

import java.awt.*;

public class HistoDailyPainter implements HistoPainter {

  private HistoDailyDataset dataset;
  private HistoDailyColors colors;
  private HorizontalBlocksClickMap clickMap = new HorizontalBlocksClickMap();

  public HistoDailyPainter(HistoDailyDataset dataset, HistoDailyColors colors) {
    this.dataset = dataset;
    this.colors = colors;
  }

  public HistoDataset getDataset() {
    return dataset;
  }

  public Key getObjectKeyAt(int x, int y) {
    return clickMap.getKey(x, y);
  }

  public void paint(Graphics2D g2, HistoChartMetrics chartMetrics, HistoRollover rollover) {

    HistoDailyMetrics metrics = new HistoDailyMetrics(chartMetrics);

    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (dataset.size() == 0) {
      return;
    }

    Double previousValue = null;
    Integer previousY = null;
    int maxX = -1;

    clickMap.reset(metrics.columnTop(), metrics.columnTop() + metrics.columnHeight());

    int y0 = metrics.y(0);
    HistoDailyBlockPainter blockPainter = new HistoDailyBlockPainter(g2, colors, y0);

    for (int i = 0; i < dataset.size(); i++) {

      Double[] values = dataset.getValues(i);
      if (values.length == 0) {
        continue;
      }

      int left = metrics.left(i);
      int right = metrics.right(i);
      int width = right - left;
      int previousX = left;

      for (int dayIndex = 0; dayIndex < values.length; dayIndex++) {
        Double value = values[dayIndex];
        if (value == null) {
          continue;
        }

        int x = left + (width * (dayIndex + 1)) / values.length;
        int y = metrics.y(value);
        if (previousY == null) {
          previousY = y;
          previousValue = value;
        }

        boolean current = dataset.isCurrent(i);
        boolean future = dataset.isFuture(i, dayIndex);
        boolean selected = dataset.isSelected(i);
        boolean isRollover = rollover.isOnColumn(i);
        int blockWidth = width / values.length;

        Key dayKey = dataset.getKey(i, dayIndex);
        clickMap.add(dayKey, previousX);
        g2.setComposite(AlphaComposite.Src);
        if (rollover.isOnObject(dayKey)) {
          g2.setColor(colors.getRolloverDayColor());
          g2.fillRect(previousX, metrics.columnTop() + 1, blockWidth, metrics.columnHeight() - 1);
        }
        else if (dataset.isDaySelected(i, dayIndex)) {
          g2.setColor(colors.getSelectedDayColor());
          g2.fillRect(previousX, metrics.columnTop() + 1, blockWidth, metrics.columnHeight() - 1);
        }

        if (dataset.isCurrent(i, dayIndex)) {
          g2.setColor(colors.getCurrentDayColor());
          g2.drawLine(x, metrics.currentDayLineTop(), x, metrics.currentDayLineBottom());
        }

        if (Math.signum(previousValue) == Math.signum(value)) {
          blockPainter.draw(previousX, previousY, x, y, value >= 0, current, future, selected, isRollover);
        }
        else {
          int x0 = previousX + (int)(blockWidth * Math.abs(previousValue) / (Math.abs(previousValue) + Math.abs(value)));
          blockPainter.draw(previousX, previousY, x0, y0, previousValue >= 0, current, future, selected, isRollover);
          blockPainter.draw(x0, y0, x, y, value >= 0, current, future, selected, isRollover);
        }

        previousX = x;
        maxX = x + blockWidth;
        previousY = y;
        previousValue = value;
      }

      drawMinLabel(g2, dataset, i, metrics);
    }

    blockPainter.complete();

    clickMap.complete(maxX);
  }

  private void drawMinLabel(Graphics2D g2, HistoDailyDataset dataset, int index, HistoDailyMetrics metrics) {
    int minDay = dataset.getMinDay(index);
    Double minValue = dataset.getValue(index, minDay);
    if ((minValue == null) || !metrics.isDrawingInnerLabels()) {
      return;
    }

    String text = getMinText(index, minValue);
    int minX = metrics.minX(minDay, dataset.getValues(index).length, index);

    g2.setStroke(new BasicStroke(1));
    g2.setComposite(AlphaComposite.Src);
    g2.setColor(colors.getInnerLabelColor(minValue));
    g2.drawString(text, metrics.innerLabelX(index, minX, text), metrics.innerLabelY());

    g2.drawLine(metrics.innerLabelLineX(index, minX, text),
                metrics.innerLabelLineY(),
                minX,
                metrics.y(minValue));
  }

  private String getMinText(int index, Double minValue) {
    return Formatting.toStandardValueString(minValue);
  }
}