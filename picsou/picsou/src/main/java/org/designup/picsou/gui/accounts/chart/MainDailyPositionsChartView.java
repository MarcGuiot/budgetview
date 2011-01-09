package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class MainDailyPositionsChartView extends AccountsChartView {

  public MainDailyPositionsChartView(GlobRepository repository, Directory directory) {
    super(repository, directory, "mainAccountsHistoChart");
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition)  {
    histoChartBuilder.showMainDailyHisto(currentMonthId);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoBudget();
  }
}
