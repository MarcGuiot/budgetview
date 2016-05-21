package com.budgetview.gui.components.charts.histo.utils;

import com.budgetview.gui.analysis.histobuilders.range.HistoChartAdjustableRange;
import com.budgetview.gui.analysis.histobuilders.range.HistoChartRange;
import com.budgetview.gui.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class AutoRangeUpdater {
  public static void install(JPanel panel, HistoChartRange initialRange, GlobRepository repository, HistoChartAdjustableRange... ranges) {
    new AutoRangeUpdater(panel, initialRange, repository, ranges);
  }

  private HistoChartRange currentRange;

  private AutoRangeUpdater(final JPanel panel, HistoChartRange initialRange, final GlobRepository repository, final HistoChartAdjustableRange[] ranges) {
    this.currentRange = initialRange;
    panel.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent componentEvent) {
        Config config = getConfig(panel.getWidth());
        currentRange = new ScrollableHistoChartRange(config.monthsBack, config.monthsForward, false, repository);
        for (HistoChartAdjustableRange adjustableRange : ranges) {
          adjustableRange.setRange(currentRange);
        }
      }
    });
  }

  static Config getConfig(int width) {
    int cols = Math.max(4, (width - 70) / 45);
    int back = (int) Math.min(cols * 0.3, 3);
    return new Config(back, cols - back);
  }

  static class Config {
    final int monthsBack;
    final int monthsForward;

    public Config(int monthsBack, int monthsForward) {
      this.monthsBack = monthsBack;
      this.monthsForward = monthsForward;
    }
  }
}
