package org.designup.picsou.gui.components.charts.histo;

import org.globsframework.model.Key;

import java.awt.*;

public interface HistoPainter {

  void paint(Graphics2D g, HistoChartMetrics metrics, HistoChartConfig config, HistoRollover rollover);

  HistoDataset getDataset();

  public static final HistoPainter NULL = new HistoPainter() {
    public void paint(Graphics2D g, HistoChartMetrics metrics, HistoChartConfig config, HistoRollover rollover) {
    }

    public HistoDataset getDataset() {
      return HistoDataset.NULL;
    }

    public Key getObjectKeyAt(int x, int y) {
      return null;
    }
  };

  Key getObjectKeyAt(int x, int y);
}
