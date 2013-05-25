package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.HistoChartColors;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.series.analysis.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartRangeListener;
import org.designup.picsou.model.Day;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public abstract class PositionsChartView extends View implements HistoChartRangeListener, Disposable {
  protected HistoChartBuilder histoChartBuilder;
  protected String componentName;
  private AccountHistoChartUpdater updater;

  public PositionsChartView(HistoChartRange range, HistoChartConfig config,
                            String componentName, GlobRepository repository, Directory directory) {
    super(repository, directory);
    createChartBuilder(config, range, repository, directory);
    this.componentName = componentName;
    range.addListener(this);
  }

  private void createChartBuilder(HistoChartConfig config,
                                  final HistoChartRange range,
                                  final GlobRepository repository,
                                  final Directory directory) {
    histoChartBuilder = new HistoChartBuilder(config, createChartColors(directory), range,
                                              repository, directory,
                                              directory.get(SelectionService.class));
    updater = new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
      protected void update(Integer currentMonthId, boolean resetPosition) {
        updateChart(histoChartBuilder, currentMonthId, true);
      }
    };
    final NavigationService navigationService = directory.get(NavigationService.class);
    histoChartBuilder.addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection selection, Set<Key> objectKeys) {
        PositionsChartView.this.processClick(selection, objectKeys, navigationService);
      }

      public void processDoubleClick(Integer columnIndex, Set<Key> objectKeys) {
        PositionsChartView.this.processDoubleClick(navigationService);
      }
    });
  }

  protected HistoChartColors createChartColors(Directory directory) {
    return new HistoChartColors(directory);
  }

  public void update() {
    updater.update(false);
  }

  public void rangeUpdated() {
    Integer currentMonthId = updater.getCurrentMonthId();
    if (currentMonthId != null) {
      updateChart(histoChartBuilder, currentMonthId, false);
    }
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add(componentName, getChart());
  }

  public void registerComponents(RepeatCellBuilder builder) {
    builder.add(componentName, getChart());
  }

  public HistoChart getChart() {
    HistoChart chart = histoChartBuilder.getChart();
    chart.setName(componentName);
    return chart;
  }

  protected abstract void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition);

  protected void processClick(HistoSelection selection, Set<Key> objectKeys, NavigationService navigationService) {

  }

  protected void processDoubleClick(NavigationService navigationService) {

  }

  public void dispose() {
    updater.dispose();
    histoChartBuilder.dispose();
  }

  public static void showTransactions(GlobList transactions, Integer monthId, SelectionService selectionService, GlobRepository repository, Directory directory) {
    Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
    if (!selectedMonthIds.contains(monthId)) {
      selectionService.select(repository.get(Key.create(Month.TYPE, monthId)));
    }

    selectionService.select(transactions, Transaction.TYPE);

    directory.get(NavigationService.class).gotoDataWithPlannedTransactions();
  }

  public static GlobList getTransactions(Key objectKey, GlobRepository repository) {
    Integer monthId = objectKey.get(Day.MONTH);
    Integer day = objectKey.get(Day.DAY);
    return getTransactions(monthId, day, repository);
  }


  protected static GlobList getTransactions(Integer monthId, Integer day, GlobRepository repository) {
    return repository.getAll(Transaction.TYPE,
                             and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                                 fieldEquals(Transaction.POSITION_DAY, day)));
  }

}