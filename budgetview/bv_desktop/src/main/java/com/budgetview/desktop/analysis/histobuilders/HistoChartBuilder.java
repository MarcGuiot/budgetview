package com.budgetview.desktop.analysis.histobuilders;

import com.budgetview.desktop.accounts.position.DailyAccountPositionComputer;
import com.budgetview.desktop.accounts.position.DailyAccountPositionValues;
import com.budgetview.desktop.analysis.histobuilders.range.HistoChartRange;
import com.budgetview.desktop.card.NavigationPopup;
import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.HistoChartColors;
import com.budgetview.desktop.components.charts.histo.HistoChartListener;
import com.budgetview.desktop.components.charts.histo.HistoSelection;
import com.budgetview.desktop.components.charts.histo.daily.HistoDailyColors;
import com.budgetview.desktop.components.charts.histo.diff.HistoDiffColors;
import com.budgetview.desktop.components.charts.histo.diff.HistoDiffLegendPanel;
import com.budgetview.desktop.components.charts.histo.line.HistoLineColors;
import com.budgetview.desktop.components.charts.histo.utils.HistoChartListenerAdapter;
import com.budgetview.desktop.description.Labels;
import com.budgetview.desktop.model.*;
import com.budgetview.desktop.series.utils.SeriesOrGroup;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.desktop.utils.DaySelection;
import com.budgetview.model.*;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

public class HistoChartBuilder implements Disposable {
  private HistoChart histoChart;
  private JLabel histoChartLabel;
  private HistoDiffLegendPanel histoChartLegend;
  private GlobRepository repository;

  private HistoDiffColors balanceColors;
  private HistoDiffColors incomeAndExpensesColors;
  private HistoLineColors incomeAndExpensesLineColors;
  private HistoLineColors uncategorizedColors;
  private HistoLineColors accountColors;
  private HistoDiffColors seriesColors;

  private HistoChartRange range;
  private DisposableGroup disposables = new DisposableGroup();

  public HistoChartBuilder(HistoChartConfig config,
                           HistoChartColors colors,
                           final HistoChartRange range,
                           final GlobRepository repository,
                           final Directory directory,
                           final SelectionService parentSelectionService) {
    this.range = range;
    this.repository = repository;
    this.histoChart = new HistoChart(config, colors);
    this.disposables.add(histoChart);
    final NavigationPopup popup = new NavigationPopup(histoChart, repository, directory, parentSelectionService);
    this.histoChart.addListener(new ChartListener(parentSelectionService, popup));

    histoChartLabel = new JLabel();
    histoChartLegend = new HistoDiffLegendPanel(repository, directory);
    disposables.add(histoChartLegend);

    initColors(directory);
  }

  public void setRange(HistoChartRange newRange) {
    this.range = newRange;
  }

  public void addListener(HistoChartListener listener) {
    histoChart.addListener(listener);
  }

  public void dispose() {
    disposables.dispose();
    histoChart = null;
    histoChartLabel = null;
    histoChartLegend = null;
  }

  public boolean isDisposed() {
    return histoChart == null;
  }

  private void initColors(Directory directory) {
    balanceColors = disposables.add(new HistoDiffColors(
      "histo.balance.line",
      "histo.balance.fill",
      directory
    ));

    incomeAndExpensesColors = disposables.add(new HistoDiffColors(
      "histo.income.line",
      "histo.expenses.line",
      "histo.income.fill",
      "histo.expenses.fill",
      directory
    ));

    seriesColors = disposables.add(new HistoDiffColors(
      "histo.series.line",
      "histo.series.fill",
      directory
    ));

    uncategorizedColors = disposables.add(new HistoLineColors(
      "histo.uncategorized.line",
      "histo.uncategorized.line",
      "histo.uncategorized.fill.positive",
      "histo.uncategorized.fill.negative",
      directory
    ));

    incomeAndExpensesLineColors = disposables.add(new HistoLineColors(
      "histo.income.line",
      "histo.expenses.line",
      "histo.income.fill",
      "histo.expenses.fill",
      directory
    ));

    accountColors = disposables.add(new HistoLineColors(
      "histo.account.line.positive",
      "histo.account.line.negative",
      "histo.account.fill.positive",
      "histo.account.fill.negative",
      directory
    ));
  }

  public HistoChart getChart() {
    return histoChart;
  }

  public JLabel getLabel() {
    return histoChartLabel;
  }

  public JPanel getLegend() {
    return histoChartLegend.getPanel();
  }

  public void setSnapToScale(boolean value) {
    histoChart.setSnapToScale(value);
  }

  public void clear() {
    histoChart.clear();
  }

  public void showAccountDailyHisto(final int selectedMonthId, boolean showFullMonthLabels, Set<Integer> accountIds, final DaySelection daySelection, String daily, HistoDailyColors accountDailyColors) {

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    final HistoDailyDatasetBuilder builder = createDailyDataset(monthIdsToShow.size(), daily, showFullMonthLabels);

    DailyAccountPositionComputer computer = new DailyAccountPositionComputer(repository);
    final DailyAccountPositionValues positionValues = computer.getAccountDailyValues(monthIdsToShow, selectedMonthId, showFullMonthLabels, accountIds, daySelection, daily);

    positionValues.apply(new DailyAccountPositionValues.Functor() {
      public void processPositions(int monthId, Double[] minValues, boolean monthSelected, boolean[] daysSelected) {
        builder.add(monthId, minValues, monthSelected, daysSelected);
      }
    });

    builder.apply(accountDailyColors, "daily");
  }

  public void showDailyHisto(int selectedMonthId, Integer accountId, DaySelection daySelection, String daily, final DoubleField position, HistoDailyColors colors) {
    showDailyHisto(selectedMonthId, false, TransactionMatchers.transactionsForAccount(accountId), daySelection, daily, position, colors);
  }

  public void showDailyHisto(int selectedMonthId, boolean showFullMonthLabels, GlobMatcher accountMatcher, DaySelection daySelection, String daily, final DoubleField position, HistoDailyColors accountDailyColors) {
    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    final HistoDailyDatasetBuilder builder = createDailyDataset(monthIdsToShow.size(), daily, showFullMonthLabels);

    if (monthIdsToShow.isEmpty()) {
      return;
    }

    DailyAccountPositionComputer computer = new DailyAccountPositionComputer(repository);
    final DailyAccountPositionValues positionValues = computer.getDailyValues(monthIdsToShow, selectedMonthId, accountMatcher, daySelection, position);

    positionValues.apply(new DailyAccountPositionValues.Functor() {
      public void processPositions(int monthId, Double[] minValues, boolean monthSelected, boolean[] daysSelected) {
        builder.add(monthId, minValues, monthSelected, daysSelected);
      }
    });

    builder.apply(accountDailyColors, "daily");
  }

  public void showMainBalanceHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoDiffDatasetBuilder builder = createDiffDataset(monthIdsToShow.size(), "mainBalance");
    builder.setKey(BudgetArea.ALL.getKey());

    for (int monthId : monthIdsToShow) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double income = budgetStat.get(BudgetStat.INCOME_SUMMARY);
        Double expense = budgetStat.get(BudgetStat.EXPENSE_SUMMARY);
        builder.add(monthId, income, expense != null ? -expense : null, monthId == selectedMonthId);
      }
      else {
        builder.addEmpty(monthId, monthId == selectedMonthId);
      }
    }

    builder.showDiff(balanceColors,
                     "income", "expenses",
                     "mainBalance");
  }

  public void showBudgetAreaHisto(Set<BudgetArea> budgetAreas, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoDiffDatasetBuilder builder = createDiffDataset(monthIdsToShow.size(), "budgetArea");
    builder.setKeys(BudgetArea.getKeys(budgetAreas));

    for (int monthId : monthIdsToShow) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        double totalPlanned = 0;
        double totalActual = 0;
        for (BudgetArea budgetArea : budgetAreas) {
          totalPlanned += budgetStat.get(BudgetStat.getPlanned(budgetArea), 0);
          totalActual += budgetStat.get(BudgetStat.getObserved(budgetArea), 0);
        }
        builder.add(monthId, totalPlanned, totalActual, monthId == selectedMonthId);
      }
    }

    String messageKey;
    String messageArg;
    if (budgetAreas.size() == 1) {
      messageKey = "budgetArea";
      messageArg = Labels.get(budgetAreas.iterator().next());
    }
    else {
      messageKey = "budgetArea.multi";
      messageArg = Integer.toString(budgetAreas.size());
    }

    builder.showDiff(incomeAndExpensesColors,
                     "planned", "actual",
                     messageKey, messageArg);
  }

  public void showUncategorizedHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoLineDatasetBuilder builder = createLineDataset(monthIdsToShow.size(), "uncategorized");
    builder.setKey(Series.UNCATEGORIZED_SERIES);

    for (int monthId : monthIdsToShow) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.showBars(uncategorizedColors, "uncategorized");
  }

  public void showSeriesHisto(Set<SeriesOrGroup> seriesOrGroups, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoDiffDatasetBuilder builder = createDiffDataset(monthIdsToShow.size(), "series");
    builder.setKeys(SeriesOrGroup.createKeys(seriesOrGroups));

    for (int monthId : monthIdsToShow) {
      double totalActual = 0.00;
      double totalPlanned = 0.00;
      for (SeriesOrGroup seriesOrGroup : seriesOrGroups) {
        Glob stat = repository.find(seriesOrGroup.createSeriesStatKey(monthId));
        if (stat != null) {
          totalPlanned += stat.get(SeriesStat.PLANNED_AMOUNT, 0.00);
          totalActual += stat.get(SeriesStat.ACTUAL_AMOUNT, 0.00);
        }
      }
      builder.add(monthId, totalPlanned, totalActual, monthId == selectedMonthId);
    }

    String messageKey;
    String messageArg;
    if (seriesOrGroups.size() == 1) {
      messageKey = "series";
      messageArg = seriesOrGroups.iterator().next().getName(repository);
    }
    else {
      messageKey = "series.multi";
      messageArg = Integer.toString(seriesOrGroups.size());
    }

    builder.showDiff(incomeAndExpensesColors,
                     "planned", "actual",
                     messageKey, messageArg);
  }

  public void showSubSeriesHisto(Set<Integer> subSeriesIds, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoLineDatasetBuilder builder = createLineDataset(monthIdsToShow.size(), "subSeries");
    builder.setKeys(GlobUtils.createKeys(SubSeries.TYPE, subSeriesIds));

    for (int monthId : monthIdsToShow) {
      double actual = 0.00;
      for (Integer subSeriesId : subSeriesIds) {
        Glob stat = repository.find(SubSeriesStat.createKey(subSeriesId, monthId));
        if (stat != null) {
          actual += stat.get(SubSeriesStat.ACTUAL_AMOUNT, 0.00);
        }
      }
      builder.add(monthId, actual, monthId == selectedMonthId);
    }

    String messageKey;
    String messageArg;
    if (subSeriesIds.size() == 1) {
      messageKey = "subSeries";
      Integer firstSubSeriesId = subSeriesIds.iterator().next();
      Glob subSeries = repository.get(Key.create(SubSeries.TYPE, firstSubSeriesId));
      messageArg = subSeries.get(SubSeries.NAME);
    }
    else {
      messageKey = "subSeries.multi";
      messageArg = Integer.toString(subSeriesIds.size());
    }

    builder.invertIfNeeded();
    builder.showBars(incomeAndExpensesLineColors, messageKey, messageArg);
  }

  public void showMainAccountsHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoLineDatasetBuilder builder = createLineDataset(monthIdsToShow.size(), "mainAccounts");
    builder.setKey(Account.MAIN_SUMMARY_KEY);

    for (int monthId : monthIdsToShow) {
      Glob stat = repository.find(Key.create(AccountStat.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID,
                                             AccountStat.MONTH, monthId));

      Double value = stat != null ? stat.get(AccountStat.MIN_POSITION, 0.) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.showBars(accountColors, "mainAccounts");
  }

  public void showSavingsAccountsHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoLineDatasetBuilder builder = createLineDataset(monthIdsToShow.size(), "savingsAccounts");
    builder.setKey(Account.SAVINGS_SUMMARY_KEY);

    for (int monthId : monthIdsToShow) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.showBars(accountColors, "savingsAccounts");
  }

  public void showSeriesBudget(Integer seriesId, int selectedMonthId, Set<Integer> selectedMonths, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    Glob series = repository.find(Key.create(Series.TYPE, seriesId));
    if (series == null) {
      histoChart.clear();
      return;
    }

    List<Integer> monthsIdsToShow = getMonthIdsToShow(selectedMonthId);
    HistoDiffDatasetBuilder builder = createDiffDataset(monthsIdsToShow.size(), "series");
    builder.setKey(Key.create(Series.TYPE, seriesId));

    GlobList list =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .getGlobs()
        .filterSelf(GlobMatchers.isTrue(SeriesBudget.ACTIVE), repository)
        .sort(SeriesBudget.MONTH);

    double multiplier = Account.getMultiplierForInOrOutputOfTheAccount(series);

    for (Glob seriesBudget : list) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      if (!monthsIdsToShow.contains(monthId)) {
        continue;
      }
      Double planned = adjust(seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0.), multiplier);
      Double actual = adjust(seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT), multiplier);
      builder.add(monthId, planned, actual, selectedMonths.contains(monthId));
    }

    builder.showDiff(seriesColors,
                     "planned", "actual",
                     "series", series.get(Series.NAME));
  }

  private Double adjust(Double value, double multiplier) {
    if (value == null) {
      return null;
    }
    return value * multiplier;
  }

  private HistoLineDatasetBuilder createLineDataset(int monthCount, String tooltipKey) {
    return new HistoLineDatasetBuilder(histoChart, histoChartLabel, repository, tooltipKey, HistoLabelUpdater.get(histoChart, monthCount)) {
      protected void updateLegend() {
        histoChartLegend.hide();
      }
    };
  }

  private HistoDiffDatasetBuilder createDiffDataset(int monthCount, String tooktipKey) {
    return new HistoDiffDatasetBuilder(histoChart, histoChartLabel, histoChartLegend, repository, tooktipKey, HistoLabelUpdater.get(histoChart, monthCount));
  }

  private HistoDailyDatasetBuilder createDailyDataset(int monthCount, String tooktipKey, boolean showFullMonthLabels) {
    return new HistoDailyDatasetBuilder(histoChart, histoChartLabel, repository, tooktipKey, HistoLabelUpdater.get(histoChart, monthCount, showFullMonthLabels)) {
      protected void updateLegend() {
        histoChartLegend.hide();
      }
    };
  }

  private List<Integer> getMonthIdsToShow(Integer selectedMonthId) {
    return range.getMonthIds(selectedMonthId);
  }

  private class ChartListener extends HistoChartListenerAdapter {
    private SelectionService parentSelectionService;
    private final NavigationPopup popup;

    public ChartListener(SelectionService parentSelectionService, NavigationPopup popup) {
      this.parentSelectionService = parentSelectionService;
      this.popup = popup;
    }

    public void processClick(HistoSelection selection, Set<Key> objectKeys) {
      GlobList months = new GlobList();
      for (Integer monthId : selection.getColumnIds()) {
        months.add(repository.get(Key.create(Month.TYPE, monthId)));
      }
      parentSelectionService.select(months, Month.TYPE);
    }

    public void processRightClick(HistoSelection selection, Set<Key> objectKeys, Point mouseLocation) {
      popup.show(selection.getColumnIds(), objectKeys);
    }

    public void scroll(int count) {
      range.scroll(count);
    }
  }
}
