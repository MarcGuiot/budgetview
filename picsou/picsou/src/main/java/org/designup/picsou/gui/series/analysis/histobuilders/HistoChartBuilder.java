package org.designup.picsou.gui.series.analysis.histobuilders;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.accounts.position.DailyAccountPositionComputer;
import org.designup.picsou.gui.accounts.position.DailyAccountPositionValues;
import org.designup.picsou.gui.card.NavigationPopup;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartColors;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffLegendPanel;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SubSeriesStat;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.utils.SeriesOrGroup;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
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
  private HistoDailyColors accountDailyColors;
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

  public void setRange(HistoChartRange range) {
    this.range = range;
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

    accountDailyColors = disposables.add(new HistoDailyColors(
      accountColors,
      "histo.account.daily.current",
      "histo.account.daily.current.annotation",
      "histo.account.inner.label.positive",
      "histo.account.inner.label.negative",
      "histo.account.inner.label.line",
      "histo.account.inner.rollover.day",
      "histo.account.inner.selected.day",
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

  public void showAccountDailyHisto(final int selectedMonthId, boolean showFullMonthLabels, Set<Integer> accountIds, final DaySelection daySelection, String daily) {

    final HistoDailyDatasetBuilder builder = createDailyDataset(daily, showFullMonthLabels);
    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);

    DailyAccountPositionComputer computer = new DailyAccountPositionComputer(repository);
    final DailyAccountPositionValues positionValues = computer.getAccountDailyValues(monthIdsToShow, selectedMonthId, showFullMonthLabels, accountIds, daySelection, daily);

    positionValues.apply(new DailyAccountPositionValues.Functor() {
      public void processPositions(int monthId, Double[] minValues, boolean monthSelected, boolean[] daysSelected) {
        builder.add(monthId, minValues, monthSelected, daysSelected);
      }
    });

    builder.apply(accountDailyColors, "daily");
  }

  public void showMainDailyHisto(int selectedMonthId, boolean showFullMonthLabels, String daily) {
    showDailyHisto(selectedMonthId, showFullMonthLabels, Matchers.transactionsForMainAccounts(repository), DaySelection.EMPTY, daily, Transaction.SUMMARY_POSITION);
  }

  public void showSavingsDailyHisto(int selectedMonthId, boolean showFullMonthLabels) {
    showDailyHisto(selectedMonthId, showFullMonthLabels, Matchers.transactionsForSavingsAccounts(repository), DaySelection.EMPTY, "daily", Transaction.SUMMARY_POSITION);
  }

  public void showDailyHisto(int selectedMonthId, Integer accountId, DaySelection daySelection, String daily, final DoubleField position, HistoDailyColors colors) {
    showDailyHisto(selectedMonthId, false, GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId), daySelection, daily, position, colors);
  }

  public void showDailyHisto(int selectedMonthId, boolean showFullMonthLabels, GlobMatcher accountMatcher, DaySelection daySelection, String daily, final DoubleField position) {
    showDailyHisto(selectedMonthId, showFullMonthLabels, accountMatcher, daySelection, daily, position, accountDailyColors);
  }

  private void showDailyHisto(int selectedMonthId, boolean showFullMonthLabels, GlobMatcher accountMatcher, DaySelection daySelection, String daily, DoubleField position, HistoDailyColors colors) {
    final HistoDailyDatasetBuilder builder = createDailyDataset(daily, showFullMonthLabels);

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
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

    builder.apply(colors, "daily");
  }

  public void showMainBalanceHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    HistoDiffDatasetBuilder builder = createDiffDataset("mainBalance");
    builder.setKey(BudgetArea.ALL.getKey());

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
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

    HistoDiffDatasetBuilder builder = createDiffDataset("budgetArea");
    builder.setKeys(BudgetArea.getKeys(budgetAreas));

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
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
      messageArg = budgetAreas.iterator().next().getLabel();
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
    HistoLineDatasetBuilder builder = createLineDataset("uncategorized");
    builder.setKey(Series.UNCATEGORIZED_SERIES);

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
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

    HistoDiffDatasetBuilder builder = createDiffDataset("series");
    builder.setKeys(SeriesOrGroup.createKeys(seriesOrGroups));

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
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

    HistoLineDatasetBuilder builder = createLineDataset("subSeries");
    builder.setKeys(GlobUtils.createKeys(SubSeries.TYPE, subSeriesIds));

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
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

    HistoLineDatasetBuilder builder = createLineDataset("mainAccounts");
    builder.setKey(Account.MAIN_SUMMARY_KEY);

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.MIN_POSITION, 0.) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.showBars(accountColors, "mainAccounts");
  }

  public void showSavingsAccountsHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    HistoLineDatasetBuilder builder = createLineDataset("savingsAccounts");
    builder.setKey(Account.SAVINGS_SUMMARY_KEY);

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
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

    HistoDiffDatasetBuilder builder = createDiffDataset("series");
    builder.setKey(Key.create(Series.TYPE, seriesId));

    List<Integer> monthsToShow = getMonthIdsToShow(selectedMonthId);

    GlobList list =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .getGlobs()
        .filterSelf(GlobMatchers.isTrue(SeriesBudget.ACTIVE), repository)
        .sort(SeriesBudget.MONTH);

    double multiplier = Account.getMultiplierForInOrOutputOfTheAccount(series);

    for (Glob seriesBudget : list) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      if (!monthsToShow.contains(monthId)) {
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
      return value;
    }
    return value * multiplier;
  }

  private HistoLineDatasetBuilder createLineDataset(String tooltipKey) {
    return new HistoLineDatasetBuilder(histoChart, histoChartLabel, repository, tooltipKey) {
      protected void updateLegend() {
        histoChartLegend.hide();
      }
    };
  }

  private HistoDiffDatasetBuilder createDiffDataset(String tooktipKey) {
    return new HistoDiffDatasetBuilder(histoChart, histoChartLabel, histoChartLegend, repository, tooktipKey);
  }

  private HistoDailyDatasetBuilder createDailyDataset(String tooktipKey, boolean showFullMonthLabels) {
    return new HistoDailyDatasetBuilder(histoChart, histoChartLabel, repository, tooktipKey, showFullMonthLabels) {
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
