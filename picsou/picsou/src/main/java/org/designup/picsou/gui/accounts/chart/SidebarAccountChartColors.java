package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

public class SidebarAccountChartColors implements Disposable {

  private HistoDailyColors dailyColors;
  private HistoLineColors accountColors;

  public SidebarAccountChartColors(Directory directory) {
    accountColors = new HistoLineColors(
      "sidebar.histo.account.line.positive",
      "sidebar.histo.account.line.negative",
      "sidebar.histo.account.fill.positive",
      "sidebar.histo.account.fill.negative",
      directory);

    dailyColors = new HistoDailyColors(
      accountColors,
      "sidebar.histo.account.daily.current",
      "sidebar.histo.account.daily.current.annotation",
      "sidebar.histo.account.inner.label.positive",
      "sidebar.histo.account.inner.label.negative",
      "sidebar.histo.account.inner.label.line",
      "sidebar.histo.account.inner.rollover.day",
      "sidebar.histo.account.inner.selected.day",
      directory
    );
  }

  public HistoDailyColors getDailyColors() {
    return dailyColors;
  }

  public void dispose() {
    accountColors.dispose();
    dailyColors.dispose();
  }
}
