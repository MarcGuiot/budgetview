package com.budgetview.gui.accounts.chart;

import com.budgetview.gui.analysis.histobuilders.range.HistoChartRange;
import com.budgetview.gui.card.NavigationService;
import com.budgetview.gui.components.charts.histo.HistoChart;
import com.budgetview.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import com.budgetview.gui.components.highlighting.HighlightingService;
import com.budgetview.model.*;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.gui.analysis.histobuilders.HistoChartBuilder;
import com.budgetview.gui.components.charts.histo.HistoRollover;
import com.budgetview.gui.components.tips.DetailsTip;
import com.budgetview.gui.utils.DaySelection;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class MainDailyPositionsChartView extends PositionsChartView {

  private boolean showFullMonthLabels = false;
  private String tooltipKey;
  private final SidebarAccountChartColors accountChartColors;
  private GlobMatcher accountMatcher = GlobMatchers.NONE;
  private DisposableGroup disposables = new DisposableGroup();

  public MainDailyPositionsChartView(HistoChartRange range, HistoChartConfig config, String componentName,
                                     final GlobRepository repository, final Directory directory, String tooltipKey) {
    super(range, config, componentName, repository, directory);
    this.tooltipKey = tooltipKey;
    this.accountChartColors = disposables.add(new SidebarAccountChartColors(directory));
  }

  public void installHighlighting() {
    HistoChart chart = histoChartBuilder.getChart();
    chart.addListener(new ChartListener(repository, chart, directory));
  }

  public void setAccount(GlobMatcher accountMatcher) {
    this.accountMatcher = accountMatcher;
    update();
  }

  public void setAccount(Key accountKey) {
    this.accountMatcher = GlobMatchers.keyEquals(accountKey);
    update();
  }

  public void setShowFullMonthLabels(boolean show) {
    this.showFullMonthLabels = show;
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    if (histoChartBuilder.isDisposed()) {
      return;
    }
    Set<Integer> accountIdSet = repository.getAll(Account.TYPE, accountMatcher).getValueSet(Account.ID);
    histoChartBuilder.showAccountDailyHisto(currentMonthId, showFullMonthLabels, accountIdSet, DaySelection.EMPTY, tooltipKey, accountChartColors.getDailyColors());
  }

  protected void processDoubleClick(NavigationService navigationService) {
    navigationService.gotoBudget();
  }

  private class ChartListener extends HistoChartListenerAdapter {
    private final GlobRepository repository;
    private final HistoChart chart;
    private final Directory directory;
    private HighlightingService highlightingService;

    public ChartListener(GlobRepository repository, HistoChart chart, Directory directory) {
      this.repository = repository;
      this.chart = chart;
      this.directory = directory;
      this.highlightingService = directory.get(HighlightingService.class);
    }

    public void rolloverUpdated(HistoRollover rollover) {
      Set<Key> objectKeys = rollover.getObjectKeys();
      if (objectKeys.size() != 1) {
        highlightingService.clear(Series.TYPE);
        return;
      }

      GlobList transactions = getTransactions(objectKeys.iterator().next());
      GlobSelectionBuilder selection = new GlobSelectionBuilder();
      selection.add(new GlobList(), Series.TYPE);
      selection.add(new GlobList(), SeriesGroup.TYPE);
      for (Glob series : transactions.getTargets(Transaction.SERIES, repository)) {
        Glob group = repository.findLinkTarget(series, Series.GROUP);
        if ((group != null) && !group.isTrue(SeriesGroup.EXPANDED)) {
          selection.add(group);
        }
        else {
          selection.add(series);
        }
      }
      highlightingService.select(selection.get());
    }

    public void processDoubleClick(Integer columnIndex, Set<Key> objectKeys) {
      selectTransactions(objectKeys, chart, repository, directory, selectionService);
    }
  }

  public void selectTransactions(Set<Key> objectKeys, HistoChart chart, GlobRepository repository, Directory directory, SelectionService selectionService) {
    if (objectKeys.size() != 1) {
      return;
    }

    Key objectKey = objectKeys.iterator().next();
    GlobList transactions = getTransactions(objectKey);
    if (transactions.isEmpty()) {
      DetailsTip tip =
        new DetailsTip(chart, Lang.get("seriesAnalysis.chart.histo.daily.budgetSummary.noData.tooltip",
                                       Day.getFullLabel(objectKey)), directory);
      tip.show();
      return;
    }

    Integer monthId = objectKey.get(Day.MONTH);
    showTransactions(transactions, monthId, selectionService, repository, directory);
  }

  protected GlobList getTransactions(Integer monthId, Integer day) {
    Set<Integer> accountIdSet = repository.getAll(Account.TYPE, accountMatcher).getValueSet(Account.ID);
    if (accountIdSet.isEmpty()) {
      return super.getTransactions(monthId, day);
    }
    return repository.getAll(Transaction.TYPE,
                             and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                                 fieldEquals(Transaction.POSITION_DAY, day),
                                 fieldIn(Transaction.ACCOUNT, accountIdSet))
    );
  }

  public void dispose() {
    super.dispose();
    disposables.dispose();
  }
}
