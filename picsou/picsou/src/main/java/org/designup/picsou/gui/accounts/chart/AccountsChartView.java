package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.series.analysis.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartRangeListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public abstract class AccountsChartView extends View implements HistoChartRangeListener {
  protected HistoChartBuilder histoChartBuilder;
  protected String componentName;
  private AccountHistoChartUpdater updater;

  public AccountsChartView(HistoChartRange range, GlobRepository repository, Directory directory, String componentName) {
    this(repository, directory, componentName,
         new HistoChartConfig(true, true, false, true),
         range);
  }

  public AccountsChartView(GlobRepository repository, Directory directory, String componentName,
                           HistoChartConfig config,
                           HistoChartRange range) {
    super(repository, directory);
    createChartBuilder(config, range, repository, directory);
    this.componentName = componentName;
    range.addListener(this);
  }

  private void createChartBuilder(HistoChartConfig config,
                                  final HistoChartRange range,
                                  final GlobRepository repository,
                                  final Directory directory) {
    histoChartBuilder = new HistoChartBuilder(config, range,
                                              repository, directory,
                                              directory.get(SelectionService.class));
    updater = new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
        updateChart(histoChartBuilder, currentMonthId, true);
      }
    };
    final NavigationService navigationService = directory.get(NavigationService.class);
    histoChartBuilder.addListener(new HistoChartListenerAdapter() {
      public void doubleClick() {
        processDoubleClick(navigationService);
      }

      public void scroll(int count) {
      }
    });
  }

  protected void update() {
    updater.update(false);
  }

  public void rangeUpdated() {
    Integer currentMonthId = updater.getCurrentMonthId();
    if (currentMonthId != null) {
      updateChart(histoChartBuilder, currentMonthId, false);
    }
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(componentName, histoChartBuilder.getChart());
  }

  protected abstract void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition);

  protected abstract void processDoubleClick(NavigationService navigationService);
}
