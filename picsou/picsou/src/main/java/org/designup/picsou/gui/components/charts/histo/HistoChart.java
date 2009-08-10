package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;


public class HistoChart<T extends HistoDataset> extends JPanel {

  private HistoChartColors colors;
  private HistoPainter painter = HistoPainter.NULL;

  public HistoChart(Directory directory) {
    this.colors = new HistoChartColors(directory);
    setFont(getFont().deriveFont(9f));
  }

  public void update(HistoPainter painter) {
    this.painter = painter;
    repaint();
  }

  public void clear() {
    update(HistoPainter.NULL);
  }

  public HistoDataset getCurrentDataset() {
    return painter.getDataset();
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int chartWidth = getWidth() - 1;
    int chartHeight = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, chartWidth, chartHeight);
    }

    HistoDataset dataset = painter.getDataset();

    HistoChartMetrics metrics =
      new HistoChartMetrics(chartWidth, chartHeight, getFontMetrics(getFont()),
                            dataset.size(), dataset.getMaxNegativeValue(), dataset.getMaxPositiveValue());

    g2.setColor(colors.getChartBgColor());
    g2.fillRect(metrics.chartX(), 0, metrics.chartWidth(), metrics.chartHeight());

    g2.setColor(colors.getChartBorderColor());
    g2.drawRect(metrics.chartX(), 0, metrics.chartWidth(), metrics.chartHeight());

    g2.setStroke(new BasicStroke(1));
    for (int i = 0; i < dataset.size(); i++) {
      String label = dataset.getLabel(i);
      g2.setColor(dataset.isSelected(i) ? colors.getSelectedLabelColor() : colors.getLabelColor());
      g2.drawString(label, metrics.labelX(label, i), metrics.labelY());
    }

    double[] scaleValues = metrics.scaleValues();
    for (double scaleValue : scaleValues) {

      g2.setColor(colors.getScaleLineColor());
      g2.drawLine(metrics.chartX(), metrics.y(scaleValue), chartWidth, metrics.y(scaleValue));

      g2.setColor(colors.getLabelColor());
      String label = Integer.toString((int)scaleValue);
      g2.drawString(label, metrics.scaleX(label), metrics.scaleY(scaleValue));
    }

    painter.paint(g2, metrics);
  }
}
