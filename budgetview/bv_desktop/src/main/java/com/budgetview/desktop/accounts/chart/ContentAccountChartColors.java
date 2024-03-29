package com.budgetview.desktop.accounts.chart;

import com.budgetview.desktop.components.charts.histo.daily.HistoDailyColors;
import com.budgetview.desktop.components.charts.histo.line.HistoLineColors;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.directory.Directory;

public class ContentAccountChartColors implements Disposable {
  private HistoLineColors accountColors;
  private HistoDailyColors accountDailyColors;

  public ContentAccountChartColors(Directory directory) {
    accountColors = new HistoLineColors(
      "histo.account.line.positive",
      "histo.account.line.negative",
      "histo.account.fill.positive",
      "histo.account.fill.negative",
      directory
    );

    accountDailyColors = new HistoDailyColors(
      accountColors,
      "histo.account.daily.current",
      "histo.account.daily.current.annotation",
      "histo.account.inner.label.positive",
      "histo.account.inner.label.negative",
      "histo.account.inner.label.line",
      "histo.account.inner.rollover.day",
      "histo.account.inner.selected.day",
      directory
    );
  }

  public HistoDailyColors getAccountDailyColors() {
    return accountDailyColors;
  }

  public void dispose() {
    accountColors.dispose();
    accountDailyColors.dispose();
  }
}
