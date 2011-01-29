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
  protected String componentName;

  public AccountsChartView(GlobRepository repository, Directory directory, String componentName) {
    this(repository, directory, componentName, MONTHS_BACK, MONTHS_FORWARD, true, true, true);
  }

  public AccountsChartView(GlobRepository repository, Directory directory, String componentName, int monthsBack, int monthsForward, boolean drawLabels, boolean drawSections, boolean clickable) {
    super(repository, directory);
    this.histoChartBuilder = createChartBuilder(drawLabels, drawSections, clickable, monthsBack, monthsForward, repository, directory);
    this.componentName = componentName;
  }

  public HistoChartBuilder createChartBuilder(boolean drawLabels, boolean drawSections, boolean clickable, int monthsBack, int monthsForward, final GlobRepository repository, final Directory directory) {
    final HistoChartBuilder histoChartBuilder =
      new HistoChartBuilder(drawLabels, drawSections, clickable, repository, directory, directory.get(SelectionService.class),
                            monthsBack, monthsForward);
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
