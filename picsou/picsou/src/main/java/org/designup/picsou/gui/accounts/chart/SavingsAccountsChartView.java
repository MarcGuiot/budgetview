package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.analysis.histobuilders.range.HistoChartRange;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsAccountsChartView extends PositionsChartView {

  public SavingsAccountsChartView(HistoChartRange range, HistoChartConfig config, GlobRepository repository, Directory directory) {
    super(range, config, "savingsHistoChart", repository, directory);
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showSavingsDailyHisto(currentMonthId, false);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoBudgetForSavingsAccounts();
  }
}
