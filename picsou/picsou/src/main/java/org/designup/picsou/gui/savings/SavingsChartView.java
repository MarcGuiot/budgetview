package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.series.evolution.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.evolution.HistoChartBuilder;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SavingsChartView extends View {

  private HistoChartBuilder histoChartBuilder;

  protected SavingsChartView(GlobRepository repository, Directory directory) {
    super(repository, directory);

    histoChartBuilder = new HistoChartBuilder(repository, directory, selectionService, 12, 12);
    new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId) {
        histoChartBuilder.showSavingsAccountsHisto(currentMonthId);
      }
    };
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("histoChart", histoChartBuilder.getChart());
  }

}
