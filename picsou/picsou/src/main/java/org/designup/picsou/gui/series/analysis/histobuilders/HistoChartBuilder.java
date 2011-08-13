package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.List;
import java.util.Set;

import static org.designup.picsou.gui.utils.Matchers.transactionsForMainAccounts;
import static org.globsframework.model.utils.GlobMatchers.*;

public class HistoChartBuilder {
  private HistoChart histoChart;
  private JLabel histoChartLabel;
  private GlobRepository repository;

  private HistoDiffColors balanceColors;
  private HistoDiffColors incomeColors;
  private HistoDiffColors expensesColors;
  private HistoLineColors uncategorizedColors;
  private HistoLineColors accountColors;
  private HistoDailyColors accountDailyColors;
  private HistoLineColors accountBalanceColors;
  private HistoDiffColors seriesColors;

  private HistoChartRange range;

  public HistoChartBuilder(HistoChartConfig config,
                           final HistoChartRange range,
                           final GlobRepository repository,
                           final Directory directory,
                           final SelectionService parentSelectionService) {
    this.range = range;
    this.repository = repository;
    this.histoChart = new HistoChart(config, directory);
    this.histoChart.addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection selection, Key objectKey) {
        GlobList months = new GlobList();
        for (Integer monthId : selection.getColumnIds()) {
          months.add(repository.get(Key.create(Month.TYPE, monthId)));
        }
        parentSelectionService.select(months, Month.TYPE);
      }

      public void scroll(int count) {
        range.scroll(count);
      }
    });
    histoChartLabel = new JLabel();

    initColors(directory);
  }

  public void addListener(HistoChartListener listener) {
    histoChart.addListener(listener);
  }

  private void initColors(Directory directory) {
    balanceColors = new HistoDiffColors(
      "histo.balance.line",
      "histo.balance.fill",
      directory
    );

    incomeColors = new HistoDiffColors(
      "histo.income.line",
      "histo.income.fill",
      directory
    );

    expensesColors = new HistoDiffColors(
      "histo.expenses.line",
      "histo.expenses.fill",
      directory
    );

    seriesColors = new HistoDiffColors(
      "histo.series.line",
      "histo.series.fill",
      directory
    );

    uncategorizedColors = new HistoLineColors(
      "histo.uncategorized.line",
      "histo.uncategorized.line",
      "histo.uncategorized.fill.positive",
      "histo.uncategorized.fill.negative",
      "histo.vertical.divider",
      directory
    );

    accountColors = new HistoLineColors(
      "histo.account.line.positive",
      "histo.account.line.negative",
      "histo.account.fill.positive",
      "histo.account.fill.negative",
      "histo.vertical.divider",
      directory
    );

    accountDailyColors = new HistoDailyColors(
      accountColors,
      "histo.account.daily.current",
      "histo.account.inner.label.positive",
      "histo.account.inner.label.negative",
      "histo.account.inner.rollover.day",
      "histo.account.inner.selected.day",
      directory
    );

    accountBalanceColors = new HistoLineColors(
      "histo.account.balance.line.positive",
      "histo.account.balance.line.negative",
      "histo.account.balance.fill.positive",
      "histo.account.balance.fill.negative",
      "histo.vertical.divider",
      directory
    );
  }

  public HistoChart getChart() {
    return histoChart;
  }

  public JLabel getLabel() {
    return histoChartLabel;
  }

  public void setSnapToScale(boolean value) {
    histoChart.setSnapToScale(value);
  }

  public void clear() {
    histoChart.clear();
  }

  public void showMainDailyHisto(int selectedMonthId, boolean showFullMonthLabels) {
    showAccountDailyHisto(selectedMonthId, showFullMonthLabels, transactionsForMainAccounts(repository), DaySelection.EMPTY);
  }

  public void showDailyHisto(int selectedMonthId, boolean showFullMonthLabels, Set<Integer> accountIds, DaySelection daySelection) {
    GlobMatcher matcher;
    if (accountIds == null) {
      matcher = Matchers.transactionsForMainAccounts(repository);
    }
    else {
      matcher = Matchers.transactionsForAccounts(accountIds, repository);
    }
    showAccountDailyHisto(selectedMonthId, showFullMonthLabels, matcher, daySelection);
  }

  private void showAccountDailyHisto(int selectedMonthId, boolean showFullMonthLabels, GlobMatcher accountMatcher, DaySelection daySelection) {
    HistoDailyDatasetBuilder builder = createDailyDataset("daily", showFullMonthLabels);

    Double lastValue = null;
    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      GlobList transactions =
        repository.getAll(Transaction.TYPE,
                          and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                              accountMatcher))
          .sort(TransactionComparator.ASCENDING_ACCOUNT);

      int maxDay = Month.getLastDayNumber(monthId);
      if (!transactions.isEmpty()) {
        maxDay = Math.max(maxDay, transactions.getSortedSet(Transaction.POSITION_DAY).last());
      }

      Double[] lastValues = new Double[maxDay];
      Double[] minValues = new Double[maxDay];
      for (Glob transaction : transactions) {
        int day = transaction.get(Transaction.POSITION_DAY) - 1;
        lastValues[day] = transaction.get(Transaction.SUMMARY_POSITION);
        if (minValues[day] == null) {
          minValues[day] = transaction.get(Transaction.SUMMARY_POSITION);
        }
        else {
          minValues[day] = Math.min(transaction.get(Transaction.SUMMARY_POSITION, Double.MAX_VALUE), minValues[day]);
        }
      }

      for (int i = 0; i < minValues.length; i++) {
        if (minValues[i] == null) {
          minValues[i] = lastValue;
        }
        else {
          lastValue = lastValues[i];
        }
      }

      if (lastValue == null) {
        Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
        if (stat != null) {
          lastValue = stat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
          for (int i = 0; i < minValues.length; i++) {
            minValues[i] = lastValue;
          }
        }
      }

      builder.add(monthId, minValues, monthId == selectedMonthId, daySelection.getValues(monthId, maxDay));
    }

    builder.apply(accountDailyColors, "daily");
  }

  public void showMainBalanceHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }
    HistoDiffDatasetBuilder builder = createDiffDataset("mainBalance");

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

    builder.showBarLine(balanceColors,
                        "mainBalance",
                        Colors.toString(balanceColors.getFillColor()),
                        Colors.toString(balanceColors.getLineColor()));
  }

  public void showBudgetAreaHisto(BudgetArea budgetArea, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    HistoDiffDatasetBuilder builder = createDiffDataset("budgetArea");

    DoubleField plannedField = BudgetStat.getPlanned(budgetArea);
    DoubleField actualField = BudgetStat.getObserved(budgetArea);
    builder.setInverted(!budgetArea.isIncome() && budgetArea != BudgetArea.SAVINGS);

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double planned = budgetStat.get(plannedField);
        Double actual = budgetStat.get(actualField);
        builder.add(monthId, planned, actual, monthId == selectedMonthId);
      }
      else {
        builder.addEmpty(monthId, monthId == selectedMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.showBarLine(colors,
                        "budgetArea",
                        budgetArea.getLabel(),
                        Colors.toString(colors.getFillColor()),
                        Colors.toString(colors.getLineColor()));
  }

  public void showUncategorizedHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }
    HistoLineDatasetBuilder builder = createLineDataset("uncategorized");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.showLine(uncategorizedColors, "uncategorized");
  }

  public void showSeriesHisto(Integer seriesId, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }
    HistoDiffDatasetBuilder builder = createDiffDataset("series");

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    builder.setInverted(!budgetArea.isIncome() && budgetArea != BudgetArea.SAVINGS);

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (stat != null) {
        Double planned = stat.get(SeriesStat.PLANNED_AMOUNT, 0);
        Double actual = stat.get(SeriesStat.AMOUNT);
        builder.add(monthId, planned, actual, monthId == selectedMonthId);
      }
      else {
        builder.addEmpty(monthId, monthId == selectedMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.showBarLine(colors,
                        "series",
                        series.get(Series.NAME),
                        Colors.toString(colors.getFillColor()),
                        Colors.toString(colors.getLineColor()));
  }

  public void showMainAccountsHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    HistoLineDatasetBuilder builder = createLineDataset("mainAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.MIN_POSITION) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.showLine(accountColors, "mainAccounts");
  }

  public void showSavingsAccountsHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == selectedMonthId);
    }

    dataset.showBars(accountColors, "savingsAccounts");
  }

  public void showSavingsBalanceHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }
    HistoLineDatasetBuilder dataset = createLineDataset("savingsBalance");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.BALANCE) : 0.0;
      dataset.add(monthId, value, monthId == selectedMonthId);
    }

    dataset.showBars(accountBalanceColors, "savingsBalance");
  }

  public void showSavingsAccountHisto(int selectedMonthId, int accountId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.find(monthId, accountId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == selectedMonthId);
    }

    dataset.showLine(accountColors, "savingsAccounts");
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

    HistoDiffDatasetBuilder dataset = createDiffDataset("series");

    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    dataset.setInverted(!budgetArea.isIncome() && budgetArea != BudgetArea.SAVINGS);

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
      Double planned = adjust(seriesBudget.get(SeriesBudget.AMOUNT, 0.), multiplier);
      Double actual = adjust(seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT), multiplier);
      dataset.add(monthId, planned, actual, selectedMonths.contains(monthId));
    }

    dataset.showBarLine(seriesColors,
                        "series",
                        series.get(Series.NAME),
                        Colors.toString(seriesColors.getFillColor()),
                        Colors.toString(seriesColors.getLineColor()));
  }

  private Double adjust(Double value, double multiplier) {
    if (value == null) {
      return value;
    }
    return value * multiplier;
  }

  private HistoLineDatasetBuilder createLineDataset(String tooltipKey) {
    return new HistoLineDatasetBuilder(histoChart, histoChartLabel, repository, tooltipKey);
  }

  private HistoDiffDatasetBuilder createDiffDataset(String tooktipKey) {
    return new HistoDiffDatasetBuilder(histoChart, histoChartLabel, repository, tooktipKey);
  }

  private HistoDailyDatasetBuilder createDailyDataset(String tooktipKey, boolean showFullMonthLabels) {
    return new HistoDailyDatasetBuilder(histoChart, histoChartLabel, repository, tooktipKey, showFullMonthLabels);
  }

  private List<Integer> getMonthIdsToShow(Integer selectedMonthId) {
    return range.getMonthIds(selectedMonthId);
  }
}
