package org.designup.picsou.gui.components.charts.histo;

import java.awt.*;

public interface HistoPainter {

  void paint(Graphics2D g, HistoChartMetrics metrics, Integer rolloverColumnIndex);

  HistoDataset getDataset();

  public static final HistoPainter NULL = new HistoPainter() {
    public void paint(Graphics2D g, HistoChartMetrics metrics, Integer rolloverColumnIndex) {
    }

    public HistoDataset getDataset() {
      return HistoDataset.NULL;
    }
  };
}
