package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.painters.HistoLineColors;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
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

  public void showMainBalanceHisto(int selectedMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("mainBalance");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      boolean isCurrentMonth = isCurrentMonth(monthId);
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double income = budgetStat.get(BudgetStat.INCOME_SUMMARY);
        Double expense = budgetStat.get(BudgetStat.EXPENSE_SUMMARY);
        builder.add(monthId, income, expense != null ? -expense : null, isCurrentMonth, monthId == selectedMonthId);
      }
      else {
        builder.addEmpty(monthId, isCurrentMonth, monthId == selectedMonthId);
      }
    }

    builder.showBarLine(balanceColors,
                        "mainBalance",
                        Colors.toString(balanceColors.getFillColor()),
                        Colors.toString(balanceColors.getLineColor()));
  }

  public void showBudgetAreaHisto(BudgetArea budgetArea, int selectedMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("budgetArea");

    DoubleField plannedField = BudgetStat.getPlanned(budgetArea);
    DoubleField actualField = BudgetStat.getObserved(budgetArea);
    builder.setInverted(!budgetArea.isIncome());

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      boolean isCurrentMonth = isCurrentMonth(monthId);
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double planned = budgetStat.get(plannedField);
        Double actual = budgetStat.get(actualField);
        builder.add(monthId, planned, actual, isCurrentMonth, monthId == selectedMonthId);
      }
      else {
        builder.addEmpty(monthId, isCurrentMonth, monthId == selectedMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.showBarLine(colors,
                        "budgetArea",
                        budgetArea.getLabel(),
                        Colors.toString(colors.getFillColor()),
                        Colors.toString(colors.getLineColor()));
  }

  public void showUncategorizedHisto(int selectedMonthId) {
    HistoLineDatasetBuilder builder = createLineDataset("uncategorized");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.apply(uncategorizedColors, "uncategorized");
  }

  public void showSeriesHisto(Integer seriesId, int selectedMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("series");

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    builder.setInverted(!budgetArea.isIncome());

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      boolean isCurrentMonth = isCurrentMonth(monthId);
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (stat != null) {
        Double planned = stat.get(SeriesStat.PLANNED_AMOUNT, 0);
        Double actual = stat.get(SeriesStat.AMOUNT);
        builder.add(monthId, planned, actual, isCurrentMonth, monthId == selectedMonthId);
      }
      else {
        builder.addEmpty(monthId, isCurrentMonth, monthId == selectedMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.showBarLine(colors,
                        "series",
                        series.get(Series.NAME),
                        Colors.toString(colors.getFillColor()),
                        Colors.toString(colors.getLineColor()));
  }

  public void showMainAccountsHisto(int selectedMonthId) {
    HistoLineDatasetBuilder builder = createLineDataset("mainAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.MIN_POSITION) : 0.0;
      builder.add(monthId, value, monthId == selectedMonthId);
    }

    builder.apply(accountColors, "mainAccounts");
  }

  public void showMainAccountsSummary(int selectedMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("mainSummary");

    Double threshold = AccountPositionThreshold.getValue(repository);

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      boolean isCurrentMonth = isCurrentMonth(monthId);
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.MIN_POSITION) : 0.0;
      builder.add(monthId, threshold, value, isCurrentMonth, monthId == selectedMonthId);
    }

    builder.showSummary(summaryColors, true, "mainAccounts");
  }

  public void showSavingsAccountsSummary(int selectedMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("savingsSummary");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      boolean isCurrentMonth = isCurrentMonth(monthId);
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      builder.add(monthId, 0.00, value, isCurrentMonth, monthId == selectedMonthId);
    }

    builder.showSummary(summaryColors, false,  "mainAccounts");
  }

  public void showSavingsAccountsHisto(int selectedMonthId) {
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == selectedMonthId);
    }

    dataset.apply(accountColors, "savingsAccounts");
  }

  public void showSavingsAccountHisto(int selectedMonthId, int accountId) {
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(selectedMonthId)) {
      Glob stat = SavingsBudgetStat.find(monthId, accountId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == selectedMonthId);
    }

    dataset.apply(accountColors, "savingsAccounts");
  }

  public void showSeriesBudget(Integer seriesId, int selectedMonthId, Set<Integer> selectedMonths) {

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
      boolean isCurrentMonth = isCurrentMonth(monthId);
      Double planned = adjust(seriesBudget.get(SeriesBudget.AMOUNT, 0.), multiplier);
      Double actual = adjust(seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT), multiplier);
      dataset.add(monthId, planned, actual, isCurrentMonth, selectedMonths.contains(monthId));
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
    return new HistoLineDatasetBuilder(histoChart, histoChartLabel, repository,
                                       "seriesEvolution.chart.histo." + tooltipKey + ".tooltip");
  }

  private HistoDiffDatasetBuilder createDiffDataset(String tooktipKey) {
    return new HistoDiffDatasetBuilder(histoChart, histoChartLabel, repository, tooktipKey);
  }

  private boolean isCurrentMonth(int monthId) {
    return CurrentMonth.isCurrentMonth(monthId, repository);
  }

  private List<Integer> getMonthIdsToShow(Integer selectedMonthId) {
    List<Integer> result = new ArrayList<Integer>();
    for (Integer monthId : Month.range(Month.previous(selectedMonthId, monthsBack),
                                       Month.next(selectedMonthId, monthsLater))) {
      if (repository.contains(Key.create(Month.TYPE, monthId))) {
        result.add(monthId);
      }
    }
    return result;
  }
}
