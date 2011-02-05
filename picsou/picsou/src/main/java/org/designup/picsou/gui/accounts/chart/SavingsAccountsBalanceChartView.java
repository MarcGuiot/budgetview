package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilderConfig;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountsBalanceChartView extends AccountsChartView {

  public SavingsAccountsBalanceChartView(GlobRepository repository, Directory directory) {
    super(repository, directory, "savingsBalanceHistoChart");

    histoChartBuilder = createChartBuilder(new HistoChartBuilderConfig(true, true, false, true, MONTHS_BACK, MONTHS_FORWARD, false),
                                           repository, directory);
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showSavingsBalanceHisto(currentMonthId, resetPosition);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoBudget();
  }
}
