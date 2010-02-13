package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.series.evolution.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.AccountHistoChartUpdater;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class AccountEvolutionView extends View {
  public AccountEvolutionView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountEvolutionView.splits", repository, directory);

    HistoChartBuilder histoChartBuilder = new HistoChartBuilder(repository, directory, selectionService, 6, 6);
    new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId) {
        histoChartBuilder.showAllAccountsHisto(currentMonthId);
      }
    };

    builder.add("histoChart", histoChartBuilder.getChart());

    parentBuilder.add("accountEvolutionView", builder);
  }
}
