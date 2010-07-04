package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.series.evolution.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsChartView extends View {

  private HistoChartBuilder histoChartBuilder;

  protected SavingsChartView(GlobRepository repository, Directory directory) {
    super(repository, directory);

    histoChartBuilder = createChartBuilder(true, true, repository, directory);
  }

  public static HistoChartBuilder createChartBuilder(boolean drawLabels, boolean clickable, final GlobRepository repository, final Directory directory) {
    HistoChartBuilder histoChartBuilder = new HistoChartBuilder(drawLabels, clickable, repository, directory, directory.get(SelectionService.class), 12, 12);
    new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId) {
        histoChartBuilder.showSavingsAccountsHisto(currentMonthId);
      }
    };
    return histoChartBuilder;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("histoChart", histoChartBuilder.getChart());
  }

}
