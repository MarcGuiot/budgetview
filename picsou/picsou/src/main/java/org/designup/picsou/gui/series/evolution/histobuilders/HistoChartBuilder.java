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
  private HistoDiffColors seriesAmountColors;
  private HistoDiffColors accountAndThresholdColors;

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
      "histo.income.line",
      "histo.expenses.line",
      "histo.balance.fill",
      directory
    );

    incomeColors = new HistoDiffColors(
      "histo.balance.income.line.planned",
      "histo.income.line",
      "histo.balance.income.fill",
      directory
    );

    expensesColors = new HistoDiffColors(
      "histo.balance.expenses.line.planned",
      "histo.expenses.line",
      "histo.expenses.fill",
      directory
    );

    seriesAmountColors = new HistoDiffColors(
      "histo.seriesAmount.line.planned",
      "histo.seriesAmount.line",
      "histo.seriesAmount.fill",
      directory
    );

    uncategorizedColors = new HistoLineColors(
      "histo.expenses.line",
      "histo.expenses.fill",
      "histo.expenses.fill",
      directory
    );

    accountColors = new HistoLineColors(
      "histo.account.line",
      "histo.fill",
      "histo.expenses.overrun",
      directory
    );

    accountAndThresholdColors = new HistoDiffColors(
      "histo.account.threshold",
      "histo.account.line",
      "histo.fill",
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

  public void showMainBalanceHisto(int currentMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("mainBalance");

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double income = budgetStat.get(BudgetStat.INCOME_SUMMARY);
        Double expense = budgetStat.get(BudgetStat.EXPENSE_SUMMARY);
        builder.add(monthId, income, expense != null ? -expense : null, monthId == currentMonthId);
      }
      else {
        builder.addEmpty(monthId, monthId == currentMonthId);
      }
    }

    builder.showBarLine(balanceColors,
                        "mainBalance",
                        Colors.toString(balanceColors.getReferenceLineColor()),
                        Colors.toString(balanceColors.getActualLineColor()));
  }

  public void showBudgetAreaHisto(BudgetArea budgetArea, int currentMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("budgetArea");

    DoubleField plannedField = BudgetStat.getPlanned(budgetArea);
    DoubleField actualField = BudgetStat.getObserved(budgetArea);
    builder.setInverted(!budgetArea.isIncome());
    builder.setActualHiddenInTheFuture();

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double planned = budgetStat.get(plannedField);
        Double actual = budgetStat.get(actualField);
        builder.add(monthId, planned, actual, monthId == currentMonthId);
      }
      else {
        builder.addEmpty(monthId, monthId == currentMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.showBarLine(colors,
                        "budgetArea",
                        budgetArea.getLabel(),
                        Colors.toString(colors.getReferenceLineColor()),
                        Colors.toString(colors.getActualLineColor()));
  }

  public void showUncategorizedHisto(int currentMonthId) {
    HistoLineDatasetBuilder builder = createLineDataset("uncategorized");

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      builder.add(monthId, value, monthId == currentMonthId);
    }

    builder.apply(uncategorizedColors, "uncategorized");
  }

  public void showSeriesHisto(Integer seriesId, int currentMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("series");

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    builder.setInverted(!budgetArea.isIncome());
    builder.setActualHiddenInTheFuture();

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (stat != null) {
        Double planned = stat.get(SeriesStat.PLANNED_AMOUNT, 0);
        Double actual = stat.get(SeriesStat.AMOUNT);
        builder.add(monthId, planned, actual, monthId == currentMonthId);
      }
      else {
        builder.addEmpty(monthId, monthId == currentMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.showBarLine(colors,
                        "series",
                        series.get(Series.NAME),
                        Colors.toString(colors.getReferenceLineColor()),
                        Colors.toString(colors.getActualLineColor()));
  }

  private HistoDiffDatasetBuilder createDiffDataset(String tooktipKey) {
    return new HistoDiffDatasetBuilder(histoChart, histoChartLabel, repository, tooktipKey);
  }

  public void showMainAccountsHisto(int currentMonthId) {
    HistoLineDatasetBuilder builder = createLineDataset("mainAccounts");

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION) : 0.0;
      builder.add(monthId, value, monthId == currentMonthId);
    }

    builder.apply(accountColors, "mainAccounts");
  }

  public void showMainAccountsWithThresholdHisto(int currentMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("mainAccounts");
    builder.setActualHiddenInTheFuture();

    Double threshold = AccountPositionThreshold.getValue(repository);

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION) : 0.0;
      builder.add(monthId, threshold, value, monthId == currentMonthId);
    }

    builder.showDoubleLine(accountAndThresholdColors, true, "mainAccounts");
  }

  public void showSavingsAccountsHisto(int currentMonthId) {
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == currentMonthId);
    }

    dataset.apply(accountColors, "savingsAccounts");
  }

  public void showSavingsAccountHisto(int currentMonthId, int accountId) {
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIdsToShow(currentMonthId)) {
      Glob stat = SavingsBudgetStat.find(monthId, accountId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == currentMonthId);
    }

    dataset.apply(accountColors, "savingsAccounts");
  }

  public void showSeriesBudget(Integer seriesId, int currentMonthId, Set<Integer> selectedMonths) {

    Glob series = repository.find(Key.create(Series.TYPE, seriesId));
    if (series == null) {
      histoChart.clear();
      return;
    }

    HistoDiffDatasetBuilder dataset = createDiffDataset("series");

    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    dataset.setInverted(!budgetArea.isIncome());

    List<Integer> monthsToShow = getMonthIdsToShow(currentMonthId);

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

    dataset.showBarLine(seriesAmountColors,
                        "series",
                        series.get(Series.NAME),
                        Colors.toString(seriesAmountColors.getReferenceLineColor()),
                        Colors.toString(seriesAmountColors.getActualLineColor()));
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

  private List<Integer> getMonthIdsToShow(Integer currentMonthId) {
    List<Integer> result = new ArrayList<Integer>();
    for (Integer monthId : Month.range(Month.previous(currentMonthId, monthsBack),
                                       Month.next(currentMonthId, monthsLater))) {
      if (repository.contains(Key.create(Month.TYPE, monthId))) {
        result.add(monthId);
      }
    }
    return result;
  }
}
