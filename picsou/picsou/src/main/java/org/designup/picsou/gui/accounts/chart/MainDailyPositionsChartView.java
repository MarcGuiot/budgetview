package org.designup.picsou.gui.accounts.chart;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoRollover;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.components.highlighting.HighlightingService;
import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.GlobSelectionBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class MainDailyPositionsChartView extends PositionsChartView {

  private boolean showFullMonthLabels = false;
  private String tooltipKey;
  private Set<Integer> accountIdSet = new HashSet<Integer>();

  public MainDailyPositionsChartView(HistoChartRange range, HistoChartConfig config, String componentName,
                                     final GlobRepository repository, final Directory directory, String tooltipKey) {
    super(range, config, componentName, repository, directory);
    this.tooltipKey = tooltipKey;
  }

  public void installHighlighting() {
    HistoChart chart = histoChartBuilder.getChart();
    chart.addListener(new ChartListener(repository, chart, directory));
  }

  public void setAccount(Glob account) {
    accountIdSet.clear();
    accountIdSet.add(account.get(Account.ID));
    update();
  }

  public void clearAccount() {
    accountIdSet.clear();
    update();
  }

  public void setShowFullMonthLabels(boolean show) {
    this.showFullMonthLabels = show;
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showAccountDailyHisto(currentMonthId, showFullMonthLabels, accountIdSet, DaySelection.EMPTY, tooltipKey);
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
    if (accountIdSet.isEmpty()) {
      return super.getTransactions(monthId, day);
    }
    return repository.getAll(Transaction.TYPE,
                             and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                                 fieldEquals(Transaction.POSITION_DAY, day),
                                 fieldIn(Transaction.ACCOUNT, accountIdSet)));
  }
}
