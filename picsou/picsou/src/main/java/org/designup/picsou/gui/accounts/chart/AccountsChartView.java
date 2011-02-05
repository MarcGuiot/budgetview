package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.components.charts.histo.utils.ScrollGroup;
import org.designup.picsou.gui.components.charts.histo.utils.Scrollable;
import org.designup.picsou.gui.series.evolution.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilderConfig;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public abstract class AccountsChartView extends View implements Scrollable {
  public static final int MONTHS_BACK = 3;
  public static final int MONTHS_FORWARD = 9;
  protected HistoChartBuilder histoChartBuilder;
  protected String componentName;
  private AccountHistoChartUpdater updater;

  public AccountsChartView(GlobRepository repository, Directory directory, String componentName) {
    this(repository, directory, componentName,
         new HistoChartBuilderConfig(true, true, false, true, MONTHS_BACK, MONTHS_FORWARD, false));
  }

  public AccountsChartView(GlobRepository repository, Directory directory, String componentName,
                           HistoChartBuilderConfig config) {
    super(repository, directory);
    this.histoChartBuilder = createChartBuilder(config, repository, directory);
    this.componentName = componentName;
  }

  public void register(ScrollGroup group) {
    histoChartBuilder.register(group);
    group.add(this);
  }

  public HistoChartBuilder createChartBuilder(HistoChartBuilderConfig config, final GlobRepository repository, final Directory directory) {
    final HistoChartBuilder histoChartBuilder =
      new HistoChartBuilder(config,
                            repository, directory, directory.get(SelectionService.class));
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
        AccountsChartView.this.scroll(count);
      }
    });
    return histoChartBuilder;
  }

  public void scroll(int units) {
    updateChart(histoChartBuilder, updater.getCurrentMonthId(), false);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(componentName, histoChartBuilder.getChart());
  }

  protected abstract void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition);

  protected abstract void processDoubleClick(NavigationService navigationService);
}
