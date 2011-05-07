package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartRange;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountsBalanceChartView extends AccountsChartView {

  public SavingsAccountsBalanceChartView(HistoChartRange range, GlobRepository repository, Directory directory) {
    super(range, repository, directory, "savingsBalanceHistoChart");
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showSavingsBalanceHisto(currentMonthId, resetPosition);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoBudget();
  }
}
