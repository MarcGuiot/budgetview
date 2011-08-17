package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.HistoRollover;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.components.highlighting.HighlightingService;
import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.model.Day;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class MainDailyPositionsChartView extends AccountsChartView {

  private boolean showFullMonthLabels = false;
  private String tooltipKey;

  public MainDailyPositionsChartView(HistoChartRange range, HistoChartConfig config, String componentName,
                                     final GlobRepository repository, final Directory directory, String tooltipKey) {
    super(range, config, componentName, repository, directory);
    this.tooltipKey = tooltipKey;
  }

  public void installHighlighting() {
    HistoChart chart = histoChartBuilder.getChart();
    chart.addListener(new ChartListener(repository, chart, directory));
  }

  public void setShowFullMonthLabels(boolean show) {
    this.showFullMonthLabels = show;
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showMainDailyHisto(currentMonthId, showFullMonthLabels, tooltipKey);
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
      Key objectKey = rollover.getObjectKey();
      if (objectKey == null) {
        highlightingService.clear(Series.TYPE);
        return;
      }

      Integer monthId = objectKey.get(Day.MONTH);
      Integer day = objectKey.get(Day.DAY);
      GlobList transactions = getTransactions(monthId, day, repository);
      GlobList series = transactions.getTargets(Transaction.SERIES, repository);
      highlightingService.select(series, Series.TYPE);
    }

    public void processDoubleClick(Integer columnIndex, Key objectKey) {
      selectTransactions(objectKey, chart, repository, directory, selectionService);
    }
  }

  public static void selectTransactions(Key objectKey, HistoChart chart, GlobRepository repository, Directory directory, SelectionService selectionService) {
    if (objectKey == null) {
      return;
    }

    Integer monthId = objectKey.get(Day.MONTH);
    Integer day = objectKey.get(Day.DAY);
    GlobList transactions = getTransactions(monthId, day, repository);
    if (transactions.isEmpty()) {
      DetailsTip tip = new DetailsTip(chart, "Il n'y aucune opération le " + Day.getFullLabel(objectKey), directory);
      tip.show();
      return;
    }

    Set<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getValueSet(Month.ID);
    if (!selectedMonthIds.contains(monthId)) {
      selectionService.select(repository.get(Key.create(Month.TYPE, monthId)));
    }

    selectionService.select(transactions, Transaction.TYPE);

    directory.get(NavigationService.class).gotoDataWithPlannedTransactions();
  }

  private static GlobList getTransactions(Integer monthId, Integer day, GlobRepository repository) {
    return repository.getAll(Transaction.TYPE,
                             and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                                 fieldEquals(Transaction.POSITION_DAY, day)));
  }
}
