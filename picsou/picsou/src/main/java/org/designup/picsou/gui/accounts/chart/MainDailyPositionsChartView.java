package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartRange;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class MainDailyPositionsChartView extends AccountsChartView {

  private boolean showFullMonthLabels = false;

  public MainDailyPositionsChartView(HistoChartRange range, GlobRepository repository, Directory directory) {
    super(range, repository, directory, "mainAccountsHistoChart");
  }

  public MainDailyPositionsChartView(GlobRepository repository, Directory directory, String componentName,
                                     HistoChartConfig config, HistoChartRange range) {
    super(repository, directory, componentName, config, range);
  }

  public void setShowFullMonthLabels(boolean show) {
    this.showFullMonthLabels = show;
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showMainDailyHisto(currentMonthId, showFullMonthLabels);
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoBudget();
  }
}
