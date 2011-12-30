package org.designup.picsou.gui.series.analysis.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
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
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class HistoChartBuilder {
  private HistoChart histoChart;
  private JLabel histoChartLabel;
  private HistoDiffLegendPanel histoChartLegend;
  private GlobRepository repository;

  private HistoDiffColors balanceColors;
  private HistoDiffColors incomeAndExpensesColors;
  private HistoLineColors uncategorizedColors;
  private HistoLineColors accountColors;
  private HistoDailyColors accountDailyColors;
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
    histoChartLegend = new HistoDiffLegendPanel(repository, directory);

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

    incomeAndExpensesColors = new HistoDiffColors(
      "histo.income.line",
      "histo.expenses.line",
      "histo.income.fill",
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
      directory
    );

    accountColors = new HistoLineColors(
      "histo.account.line.positive",
      "histo.account.line.negative",
      "histo.account.fill.positive",
      "histo.account.fill.negative",
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

  public void showMainDailyHisto(int selectedMonthId, boolean showFullMonthLabels, String daily) {
    showDailyHisto(selectedMonthId, showFullMonthLabels, Matchers.transactionsForMainAccounts(repository), DaySelection.EMPTY, daily);
  }

  public void showSavingsDailyHisto(int selectedMonthId, boolean showFullMonthLabels) {
    showDailyHisto(selectedMonthId, showFullMonthLabels, Matchers.transactionsForSavingsAccounts(repository), DaySelection.EMPTY, "daily");
  }

  public void showAccountDailyHisto(int selectedMonthId, boolean showFullMonthLabels, Set<Integer> accountIds, DaySelection daySelection, String daily) {

    HistoDailyDatasetBuilder builder = createDailyDataset(daily, showFullMonthLabels);
    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);

    Map<Integer, Double> lastValueForAccounts = new HashMap<Integer, Double>();
    for (Integer accountId : accountIds) {
      GlobMatcher accountMatcher = Matchers.transactionsForAccount(accountId);
      Double lastValue = getLastValue(accountMatcher, monthIdsToShow, Transaction.ACCOUNT_POSITION);
      if (lastValue == null) {
        Glob account = repository.get(Key.create(Account.TYPE, accountId));
        lastValue = account.get(Account.POSITION);
      }
      lastValueForAccounts.put(accountId, lastValue);
    }
    
    for (int monthId : monthIdsToShow) {
      int maxDay = Month.getLastDayNumber(monthId);
      Double[] minValuesForAll = new Double[maxDay];
      Double[][] values = new Double[maxDay][accountIds.size() + 1];
      int accountIndex = 0;
      for (Integer accountId : accountIds) {

        GlobMatcher accountMatcher = Matchers.transactionsForAccount(accountId);

        GlobList transactions =
          repository.getAll(Transaction.TYPE,
                            and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                                accountMatcher))
            .sort(TransactionComparator.ASCENDING_ACCOUNT);

        if (!transactions.isEmpty()) {
          maxDay = Math.max(maxDay, transactions.getSortedSet(Transaction.POSITION_DAY).last());
        }

        Double[] minValuesForAccount = new Double[maxDay];
        Double lastValue = lastValueForAccounts.get(accountId);
        Double newLastValue = getDailyValues(monthId, transactions, lastValue, maxDay, minValuesForAccount, Transaction.ACCOUNT_POSITION);

        for (int dayIndex = 0; dayIndex < maxDay; dayIndex++) {
          values[dayIndex][accountIndex] = minValuesForAccount[dayIndex];
          if (minValuesForAll[dayIndex] == null) {
            minValuesForAll[dayIndex] = minValuesForAccount[dayIndex];
          }
          else if (minValuesForAccount[dayIndex] != null) {
            minValuesForAll[dayIndex] += minValuesForAccount[dayIndex];
          }
        }

        lastValueForAccounts.put(accountId, newLastValue);
        
        accountIndex++;
      }

      builder.add(monthId, minValuesForAll, monthId == selectedMonthId, daySelection.getValues(monthId, maxDay));
    }

    builder.apply(accountDailyColors, "daily");

  }

  public void showDailyHisto(int selectedMonthId, boolean showFullMonthLabels, GlobMatcher accountMatcher, DaySelection daySelection, String daily) {
    HistoDailyDatasetBuilder builder = createDailyDataset(daily, showFullMonthLabels);

    List<Integer> monthIdsToShow = getMonthIdsToShow(selectedMonthId);
    if (monthIdsToShow.isEmpty()) {
      return;
    }

    Double lastValue = getLastValue(accountMatcher, monthIdsToShow, Transaction.SUMMARY_POSITION);

    for (int monthId : monthIdsToShow) {
      GlobList transactions =
        repository.getAll(Transaction.TYPE,
                          and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                              accountMatcher))
          .sort(TransactionComparator.ASCENDING_ACCOUNT);

      int maxDay = Month.getLastDayNumber(monthId);
      if (!transactions.isEmpty()) {
        maxDay = Math.max(maxDay, transactions.getSortedSet(Transaction.POSITION_DAY).last());
      }

      Double[] minValues = new Double[maxDay];
      lastValue = getDailyValues(monthId, transactions, lastValue, maxDay, minValues, Transaction.SUMMARY_POSITION);

      builder.add(monthId, minValues, monthId == selectedMonthId, daySelection.getValues(monthId, maxDay));
    }

    builder.apply(accountDailyColors, "daily");
  }

  private Double getDailyValues(int monthId, GlobList transactions, Double previousLastValue, int maxDay,
                                Double[] minValues, DoubleField positionField) {
    Double lastValue = previousLastValue;
    Double[] lastValues = new Double[maxDay];
    for (Glob transaction : transactions) {
      int day = transaction.get(Transaction.POSITION_DAY) - 1;
      lastValues[day] = transaction.get(positionField);
      if (minValues[day] == null) {
        minValues[day] = transaction.get(positionField);
      }
      else {
        minValues[day] = Math.min(transaction.get(positionField, Double.MAX_VALUE), minValues[day]);
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

    for (int i = minValues.length - 2; i >= 0; i--) {
      if (minValues[i] == null) {
        minValues[i] = minValues[i + 1];
      }
    }

    return lastValue;
  }

  private Double getLastValue(GlobMatcher accountMatcher, List<Integer> monthIdsToShow, DoubleField positionField) {
    GlobList previousTransactions =
      repository.getAll(Transaction.TYPE,
                        and(fieldStrictlyLessThan(Transaction.POSITION_MONTH, monthIdsToShow.get(0)),
                            accountMatcher))
        .sort(TransactionComparator.ASCENDING_ACCOUNT);
    if (!previousTransactions.isEmpty()) {
      return previousTransactions.getLast().get(positionField);
    }

    GlobList nextTransactions =
      repository.getAll(Transaction.TYPE,
                        and(fieldGreaterOrEqual(Transaction.POSITION_MONTH, monthIdsToShow.get(0)),
                            accountMatcher))
        .sort(TransactionComparator.ASCENDING_ACCOUNT);
    if (nextTransactions.isEmpty()) {
      return null;
    }
    Glob firstTransaction = nextTransactions.getFirst();
    return Amounts.diff(firstTransaction.get(positionField), firstTransaction.get(Transaction.AMOUNT));
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

    builder.showDiff(balanceColors,
                     "income", "expenses",
                     "mainBalance");
  }

  public void showBudgetAreaHisto(Set<BudgetArea> budgetAreas, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    HistoDiffDatasetBuilder builder = createDiffDataset("budgetArea");

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

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.showLine(uncategorizedColors, "uncategorized");
  }

  public void showSeriesHisto(Set<Integer> seriesIds, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      range.reset();
    }

    HistoDiffDatasetBuilder builder = createDiffDataset("series");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      double totalActual = 0.00;
      double totalPlanned = 0.00;
      for (Integer seriesId : seriesIds) {
        Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
        if (stat != null) {
          totalPlanned += stat.get(SeriesStat.PLANNED_AMOUNT, 0.00);
          totalActual += stat.get(SeriesStat.AMOUNT, 0.00);
        }
      }
      builder.add(monthId, totalPlanned, totalActual, monthId == selectedMonthId);
    }

    String messageKey;
    String messageArg;
    if (seriesIds.size() == 1) {
      messageKey = "series";
      Integer firstSeriesId = seriesIds.iterator().next();
      Glob series = repository.get(Key.create(Series.TYPE, firstSeriesId));
      messageArg = series.get(Series.NAME);
    }
    else {
      messageKey = "series.multi";
      messageArg = Integer.toString(seriesIds.size());
    }

    builder.showDiff(incomeAndExpensesColors,
                     "planned", "actual",
                     messageKey, messageArg);
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
    HistoLineDatasetBuilder builder = createLineDataset("savingsAccounts");

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
}
