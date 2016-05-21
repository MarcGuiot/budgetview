package com.budgetview.gui.components.charts.histo;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.gui.histochart.HistoChartMetrics;
import com.budgetview.shared.gui.histochart.HistoDataset;
import org.globsframework.model.Key;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

public interface HistoPainter {

  void paint(Graphics2D g, HistoChartMetrics metrics, HistoChartConfig config, HistoRollover rollover);

  HistoDataset getDataset();

  public static final HistoPainter NULL = new HistoPainter() {
    public void paint(Graphics2D g, HistoChartMetrics metrics, HistoChartConfig config, HistoRollover rollover) {
    }

    public HistoDataset getDataset() {
      return HistoDataset.NULL;
    }

    public Set<Key> getObjectKeysAt(int x, int y) {
      return Collections.emptySet();
    }
  };

  Set<Key> getObjectKeysAt(int x, int y);
}
