package org.designup.picsou.gui.accounts;

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

public class MainAccountsChartView extends View {

  private HistoChartBuilder histoChartBuilder;

  public MainAccountsChartView(GlobRepository repository, Directory directory) {
    super(repository, directory);

    histoChartBuilder = createChartBuilder(true, true, repository, directory);
  }

  public static HistoChartBuilder createChartBuilder(boolean drawLabels, boolean clickable, final GlobRepository repository, final Directory directory) {
    HistoChartBuilder histoChartBuilder = new HistoChartBuilder(drawLabels, clickable, repository, directory, directory.get(SelectionService.class), 12, 12);
    new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId) {
        histoChartBuilder.showMainAccountsHisto(currentMonthId);
      }
    };
    final NavigationService navigationService = directory.get(NavigationService.class);
    histoChartBuilder.addListener(new HistoChartListener() {
      public void columnsClicked(Set<Integer> ids) {
        navigationService.gotoBudget();
      }
    });
    return histoChartBuilder;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("mainAccountsHistoChart", histoChartBuilder.getChart());
  }

}
