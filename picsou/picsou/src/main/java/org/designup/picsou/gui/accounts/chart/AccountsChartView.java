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

public abstract class AccountsChartView extends View {
  public static final int MONTHS_BACK = 3;
  public static final int MONTHS_FORWARD = 9;
  protected HistoChartBuilder histoChartBuilder;
  private String componentName;

  public AccountsChartView(GlobRepository repository, Directory directory,
                           String mainAccountsHistoChart) {
    super(repository, directory);
    this.histoChartBuilder = createChartBuilder(true, true, repository, directory);
    this.componentName = mainAccountsHistoChart;
  }

  public HistoChartBuilder createChartBuilder(boolean drawLabels, boolean clickable, final GlobRepository repository, final Directory directory) {
    final HistoChartBuilder histoChartBuilder = new HistoChartBuilder(drawLabels, clickable, repository, directory,
                                                                directory.get(SelectionService.class),
                                                                MONTHS_BACK, MONTHS_FORWARD);
    final AccountHistoChartUpdater updater = new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
        updateChart(histoChartBuilder, currentMonthId, true);
      }
    };
    final NavigationService navigationService = directory.get(NavigationService.class);
    histoChartBuilder.addDoubleClickListener(new HistoChartListener() {
      public void columnsClicked(Set<Integer> ids) {
        processDoubleClick(navigationService);
      }

      public void scroll(int count) {
      }
    });
    histoChartBuilder.addListener(new HistoChartListener() {
      public void columnsClicked(Set<Integer> ids) {
      }

      public void scroll(int count) {
        updateChart(histoChartBuilder, updater.getCurrentMonthId(), false);
      }
    });
    return histoChartBuilder;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(componentName, histoChartBuilder.getChart());
  }

  protected abstract void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition);

  protected abstract void processDoubleClick(NavigationService navigationService);
}
