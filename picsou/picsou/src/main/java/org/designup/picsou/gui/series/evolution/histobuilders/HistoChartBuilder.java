package org.designup.picsou.gui.series.evolution.histobuilders;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.painters.*;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class HistoChartBuilder {
  private HistoChart histoChart;
  private JLabel histoChartLabel;
  private GlobRepository repository;

  private HistoDiffColors balanceColors;
  private HistoDiffColors incomeColors;
  private HistoDiffColors expensesColors;
  private HistoLineColors uncategorizedColors;
  private HistoLineColors accountColors;

  private int monthsBack;
  private int monthsLater;

  public HistoChartBuilder(boolean drawLabels,
                           boolean clickable,
                           final GlobRepository repository,
                           Directory directory,
                           final SelectionService parentSelectionService,
                           int monthsBack, int monthsLater) {
    this.repository = repository;
    histoChart = new HistoChart(drawLabels, clickable, directory);
    histoChart.setListener(new HistoChartListener() {
      public void columnClicked(int monthId) {
        Glob month = repository.get(Key.create(Month.TYPE, monthId));
        parentSelectionService.select(month);
      }
    });
    histoChartLabel = new JLabel();

    this.monthsBack = monthsBack;
    this.monthsLater = monthsLater;

    initColors(directory);
  }

  private void initColors(Directory directory) {
    balanceColors = new HistoDiffColors(
      "histo.income.line",
      "histo.income.overrun",
      "histo.expenses.line",
      "histo.expenses.overrun",
      "histo.balance.fill",
      directory
    );

    incomeColors = new HistoDiffColors(
      "histo.balance.income.line.planned",
      null,
      "histo.income.line",
      "histo.income.overrun",
      "histo.balance.income.fill",
      directory
    );

    expensesColors = new HistoDiffColors(
      "histo.balance.expenses.line.planned",
      null,
      "histo.expenses.line",
      "histo.expenses.overrun",
      "histo.expenses.fill",
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
  }

  public HistoChart getChart() {
    return histoChart;
  }

  public JLabel getLabel() {
    return histoChartLabel;
  }

  public void clear() {
    histoChart.clear();
  }

  public void showMainBalanceHisto(int currentMonthId) {
    HistoDiffDatasetBuilder builder = createDiffDataset("mainBalance");

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double income = budgetStat.get(BudgetStat.INCOME_SUMMARY);
        Double expense = budgetStat.get(BudgetStat.EXPENSE_SUMMARY);
        builder.add(monthId, currentMonthId, income, expense != null ? -expense : null);
      }
      else {
        builder.addEmpty(monthId, currentMonthId);
      }
    }

    builder.apply(balanceColors,
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

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double planned = budgetStat.get(plannedField);
        Double actual = budgetStat.get(actualField);
        builder.add(monthId, currentMonthId, planned, actual);
      }
      else {
        builder.addEmpty(monthId, currentMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.apply(colors,
                  "budgetArea",
                  budgetArea.getLabel(),
                  Colors.toString(colors.getReferenceLineColor()),
                  Colors.toString(colors.getActualLineColor()));
  }

  public void showUncategorizedHisto(int currentMonthId) {
    HistoLineDatasetBuilder builder = createLineDataset("uncategorized");

    for (int monthId : getMonthIds(currentMonthId)) {
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

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (stat != null) {
        Double planned = stat.get(SeriesStat.PLANNED_AMOUNT);
        Double actual = stat.get(SeriesStat.AMOUNT);
        builder.add(monthId, currentMonthId, planned, actual);
      }
      else {
        builder.addEmpty(monthId, currentMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    builder.apply(colors,
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

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION) : 0.0;
      builder.add(monthId, value, monthId == currentMonthId);
    }

    builder.apply(accountColors, "mainAccounts");
  }

  public void showSavingsAccountsHisto(int currentMonthId) {
    HistoLineDatasetBuilder dataset = createLineDataset("savingsAccounts");

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, monthId == currentMonthId);
    }

    dataset.apply(accountColors, "savingsAccounts");
  }

  public void showSeriesBudget(Integer seriesId, int currentMonthId) {
    HistoDiffDatasetBuilder dataset = createDiffDataset("series");

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    dataset.setInverted(!budgetArea.isIncome());

    ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgets =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId);
    List<Integer> months = getMonthIds(currentMonthId);

    for (Glob seriesBudget : seriesBudgets.getGlobs()) {
      Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
      if (!months.contains(monthId)) {
        continue;
      }
      if ((seriesBudgets != null) && (seriesBudget.isTrue(SeriesBudget.ACTIVE))) {
        Double planned = seriesBudget.get(SeriesBudget.AMOUNT);
        Double actual = seriesBudget.get(SeriesBudget.OBSERVED_AMOUNT);
        dataset.add(monthId, currentMonthId, planned, actual);
      }
      else {
        dataset.addEmpty(monthId, currentMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    dataset.apply(colors,
                  "series",
                  series.get(Series.NAME),
                  Colors.toString(colors.getReferenceLineColor()),
                  Colors.toString(colors.getActualLineColor()));
  }


  private HistoLineDatasetBuilder createLineDataset(String tooltipKey) {
    return new HistoLineDatasetBuilder(histoChart, histoChartLabel, repository, 
                                       "seriesEvolution.chart.histo." + tooltipKey + ".tooltip");
  }

  private List<Integer> getMonthIds(Integer currentMonthId) {
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