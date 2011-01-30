package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountsChartView extends AccountsChartView {

  public SavingsAccountsChartView(GlobRepository repository, Directory directory) {
    super(repository, directory, "savingsHistoChart");

    histoChartBuilder = createChartBuilder(true, true, false, true, MONTHS_BACK, MONTHS_FORWARD, repository, directory);
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showSavingsAccountsHisto(currentMonthId, resetPosition);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoSavings();
  }
}
