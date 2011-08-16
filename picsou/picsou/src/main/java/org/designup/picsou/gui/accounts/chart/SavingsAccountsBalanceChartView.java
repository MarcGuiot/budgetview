package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountsBalanceChartView extends AccountsChartView {

  public SavingsAccountsBalanceChartView(HistoChartRange range, HistoChartConfig config, GlobRepository repository, Directory directory) {
    super(range, config, "savingsBalanceHistoChart", repository, directory);
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showSavingsBalanceHisto(currentMonthId, resetPosition);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoBudget();
  }
}
