package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartRange;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountsChartView extends AccountsChartView {

  public SavingsAccountsChartView(HistoChartRange range, GlobRepository repository, Directory directory) {
    super(range, repository, directory, "savingsHistoChart");
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showSavingsAccountsHisto(currentMonthId, resetPosition);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoSavings();
  }
}
