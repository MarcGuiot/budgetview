package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.ArrayList;
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
  private HistoDiffColors seriesColors;
  private HistoDiffColors summaryColors;
  private HistoDailyColors dailyColors;
  
  private int scrollMonth;
  private int monthsBack;
  private int monthsLater;

  public HistoChartBuilder(boolean drawLabels,
                           boolean clickable,
                           final GlobRepository repository,
                           final Directory directory,
                           final SelectionService parentSelectionService,
                           int monthsBack, int monthsLater) {
    this.repository = repository;
    histoChart = new HistoChart(drawLabels, clickable, directory);
    histoChart.addListener(new HistoChartListener() {
      public void columnsClicked(Set<Integer> monthIds) {
        GlobList months = new GlobList();
        for (Integer monthId : monthIds) {
          months.add(repository.get(Key.create(Month.TYPE, monthId)));
        }
        parentSelectionService.select(months, Month.TYPE);
      }

      public void scroll(int count) {
        scrollMonth += count;
      }
    });
    histoChartLabel = new JLabel();

    this.monthsBack = monthsBack;
    this.monthsLater = monthsLater;

    initColors(directory);
  }

  public void addListener(HistoChartListener listener) {
    histoChart.addListener(listener);
  }

  public void addDoubleClickListener(HistoChartListener listener) {
    histoChart.addDoubleClickListener(listener);
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
      "histo.uncategorized.fill.positive",
      "histo.uncategorized.fill.negative",
      directory
    );

    accountColors = new HistoLineColors(
      "histo.account.line",
      "histo.account.fill.positive",
      "histo.account.fill.negative",
      directory
    );

    summaryColors = new HistoDiffColors(
      "histo.summary.line",
      "histo.summary.fill",
      directory
    );
    
    dailyColors = new HistoDailyColors(
      "histo.daily.past.positive.line",
      "histo.daily.past.negative.line",
      "histo.daily.past.positive.fill",
      "histo.daily.past.negative.fill",
      "histo.daily.future.positive.line",
      "histo.daily.future.negative.line",
      "histo.daily.future.positive.fill",
      "histo.daily.future.negative.fill",
      "histo.daily.vertical.divider",
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

  public void showMainDailyHisto(int selectedMonthId) {
    HistoDailyDatasetBuilder builder = createDailyDataset("daily");

    Double lastValue = null;

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {

      GlobList transactions =
        repository.getAll(Transaction.TYPE,
                          and(fieldEquals(Transaction.POSITION_MONTH, monthId),
                              transactionsForMainAccounts(repository)))
          .sort(TransactionComparator.ASCENDING_ACCOUNT);

      int maxDay = Month.getLastDayNumber(monthId);
      if (!transactions.isEmpty()) {
        maxDay = Math.max(maxDay, transactions.getSortedSet(Transaction.POSITION_DAY).last());
      }

      Double[] values = new Double[maxDay];
      for (Glob transaction : transactions) {
        int day = transaction.get(Transaction.POSITION_DAY);
        values[day - 1] = transaction.get(Transaction.SUMMARY_POSITION);
      }

      for (int i = 0; i < values.length; i++) {
        if (values[i] == null) {
          values[i] = lastValue;
        }
        else {
          lastValue = values[i];
        }
      }

      builder.add(monthId, values, monthId == selectedMonthId);
    }
    
    builder.apply(dailyColors, "daily");
  }

  public void showMainBalanceHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      scrollMonth = 0;
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
      scrollMonth = 0;
    }

    HistoDiffDatasetBuilder builder = createDiffDataset("budgetArea");

    DoubleField plannedField = BudgetStat.getPlanned(budgetArea);
    DoubleField actualField = BudgetStat.getObserved(budgetArea);
    builder.setInverted(!budgetArea.isIncome());

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
      scrollMonth = 0;
    }
    HistoLineDatasetBuilder builder = createLineDataset("uncategorized");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.apply(uncategorizedColors, "uncategorized");
  }

  public void showSeriesHisto(Integer seriesId, int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      scrollMonth = 0;
    }
    HistoDiffDatasetBuilder builder = createDiffDataset("series");

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    builder.setInverted(!budgetArea.isIncome());

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
      scrollMonth = 0;
    }

    HistoLineDatasetBuilder builder = createLineDataset("mainAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.MIN_POSITION) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.apply(accountColors, "mainAccounts");
  }

  public void showMainAccountsSummary(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      scrollMonth = 0;
    }
    HistoDiffDatasetBuilder builder = createDiffDataset("mainSummary");

    Double threshold = AccountPositionThreshold.getValue(repository);

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.MIN_POSITION) : 0.0;
      builder.add(monthId, threshold, value, monthId == selectedMonthId);
    }

    builder.showSummary(summaryColors, true, "mainAccounts");
  }

  public void showSavingsAccountsSummary(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      scrollMonth = 0;
    }
    HistoDiffDatasetBuilder builder = createDiffDataset("savingsSummary");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      builder.add(monthId, 0.00, value, monthId == selectedMonthId);
    }

    builder.showSummary(summaryColors, false, "mainAccounts");
  }

  public void showSavingsAccountsHisto(int selectedMonthId, boolean resetPosition) {
    if (resetPosition) {
      scrollMonth = 0;
    }
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == selectedMonthId);
    }

    dataset.apply(accountColors, "savingsAccounts");
  }

  public void showSavingsAccountHisto(int selectedMonthId, int accountId, boolean resetPosition) {
    if (resetPosition) {
      scrollMonth = 0;
    }
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.find(monthId, accountId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == selectedMonthId);
    }

    dataset.apply(accountColors, "savingsAccounts");
  }

  public void showSeriesBudget(Integer seriesId, int selectedMonthId, Set<Integer> selectedMonths, boolean resetPosition) {
    if (resetPosition) {
      scrollMonth = 0;
    }

    Glob series = repository.find(Key.create(Series.TYPE, seriesId));
    if (series == null) {
      histoChart.clear();
      return;
    }

    HistoDiffDatasetBuilder dataset = createDiffDataset("series");

    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    dataset.setInverted(!budgetArea.isIncome());

    List<Integer> monthsToShow = getMonthIdsToShow(selectedMonthId);

    GlobList list =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
        .getGlobs()
        .filterSelf(GlobMatchers.isTrue(SeriesBudget.ACTIVE), repository)
        .sort(SeriesBudget.MONTH);

    double multiplier = Account.computeAmountMultiplier(series, repository);

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

  private HistoDailyDatasetBuilder createDailyDataset(String tooktipKey) {
    return new HistoDailyDatasetBuilder(histoChart, histoChartLabel, repository, tooktipKey);
  }

  private List<Integer> getMonthIdsToShow(Integer selectedMonthId) {
    List<Integer> result = new ArrayList<Integer>();
    int newCenterMonthId = Month.offset(selectedMonthId, scrollMonth);
    int firstMonth = Month.previous(newCenterMonthId, monthsBack);
    int lastMonth = Month.next(newCenterMonthId, monthsLater);
    // On ne veut pas faire de decalage si il n'y a pas assez de mois
    if (scrollMonth < 0) {
      while (!repository.contains(Key.create(Month.TYPE, firstMonth)) && scrollMonth != 0) {
        firstMonth = Month.next(firstMonth);
        lastMonth = Month.next(lastMonth);
        scrollMonth++;
      }
    }
    else if (scrollMonth > 0) {
      while (!repository.contains(Key.create(Month.TYPE, lastMonth)) && scrollMonth != 0) {
        firstMonth = Month.previous(firstMonth);
        lastMonth = Month.previous(lastMonth);
        scrollMonth--;
      }
    }
    for (Integer monthId : Month.range(firstMonth, lastMonth)) {
      Key monthKey = Key.create(Month.TYPE, monthId);
      if (repository.contains(monthKey)) {
        result.add(monthId);
      }
    }
    return result;
  }
}
