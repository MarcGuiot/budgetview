package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.series.evolution.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SavingsAccountsChartView extends AccountsChartView {

  public SavingsAccountsChartView(GlobRepository repository, Directory directory) {
    super(repository, directory, "savingsHistoChart");

    histoChartBuilder = createChartBuilder(true, true, repository, directory);
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId) {
    histoChartBuilder.showSavingsAccountsHisto(currentMonthId);
  }

  protected void processClick(NavigationService navigationService) {
    navigationService.gotoSavings();
  }
}
