package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.painters.*;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
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
    HistoDatasetBuilder dataset = new HistoDatasetBuilder();

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double income = budgetStat.get(BudgetStat.INCOME_SUMMARY);
        Double expense = budgetStat.get(BudgetStat.EXPENSE_SUMMARY);
        dataset.add(monthId, currentMonthId, income, expense != null ? -expense : null);
      }
      else {
        dataset.addEmpty(monthId, currentMonthId);
      }
    }

    dataset.apply(balanceColors,
                  "mainBalance",
                  Colors.toString(balanceColors.getReferenceLineColor()),
                  Colors.toString(balanceColors.getActualLineColor()));
  }

  public void showBudgetAreaHisto(BudgetArea budgetArea, int currentMonthId) {
    HistoDatasetBuilder dataset = new HistoDatasetBuilder();

    DoubleField plannedField = BudgetStat.getPlanned(budgetArea);
    DoubleField actualField = BudgetStat.getObserved(budgetArea);
    dataset.setInverted(!budgetArea.isIncome());
    dataset.setActualHiddenInTheFuture();

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double planned = budgetStat.get(plannedField);
        Double actual = budgetStat.get(actualField);
        dataset.add(monthId, currentMonthId, planned, actual);
      }
      else {
        dataset.addEmpty(monthId, currentMonthId);
      }
    }

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    dataset.apply(colors,
                  "budgetArea",
                  budgetArea.getLabel(),
                  Colors.toString(colors.getReferenceLineColor()),
                  Colors.toString(colors.getActualLineColor()));
  }

  public void showUncategorizedHisto(int currentMonthId) {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId), monthId == currentMonthId);
    }

    histoChart.update(new HistoLinePainter(dataset, uncategorizedColors));
    updateHistoLabel("uncategorized");
  }

  public void showSeriesHisto(Integer seriesId, int currentMonthId) {
    HistoDatasetBuilder dataset = new HistoDatasetBuilder();

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    dataset.setInverted(!budgetArea.isIncome());
    dataset.setActualHiddenInTheFuture();

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (stat != null) {
        Double planned = stat.get(SeriesStat.PLANNED_AMOUNT);
        Double actual = stat.get(SeriesStat.AMOUNT);
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

  public void showMainAccountsHisto(int currentMonthId) {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId), monthId == currentMonthId);
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
    updateHistoLabel("mainAccounts");
  }

  public void showSavingsAccountsHisto(int currentMonthId) {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds(currentMonthId)) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId), monthId == currentMonthId);
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
    updateHistoLabel("savingsAccounts");
  }

  public void showAllAccountsHisto(int currentMonthId) {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds(currentMonthId)) {

      double value = 0.0;

      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (stat != null) {
        stat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
      }

      Glob savingsStat = SavingsBudgetStat.findSummary(monthId, repository);
      if (savingsStat != null) {
        value += savingsStat.get(SavingsBudgetStat.END_OF_MONTH_POSITION);
      }

      dataset.add(monthId, value, getLabel(monthId), getSection(monthId), monthId == currentMonthId);
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
    updateHistoLabel("allAccounts");
  }

  private class HistoDatasetBuilder {

    private HistoDiffDataset dataset = new HistoDiffDataset();
    private int multiplier = 1;
    private boolean showActualInTheFuture = true;
    private int lastMonthWithTransactions;

    private HistoDatasetBuilder() {
      lastMonthWithTransactions = CurrentMonth.getLastTransactionMonth(repository);
    }

    public void setActualHiddenInTheFuture() {
      showActualInTheFuture = false;
    }

    public void setInverted(boolean inverted) {
      this.multiplier = inverted ? -1 : 1;
    }

    public void add(int monthId, int currentMonthId, Double reference, Double actual) {
      dataset.add(monthId,
                  reference != null ? reference * multiplier : 0,
                  actual != null ? actual * multiplier : 0,
                  getLabel(monthId), getSection(monthId),
                  monthId == currentMonthId,
                  monthId > lastMonthWithTransactions);
    }

    public void addEmpty(int monthId, int currentMonthId) {
      add(monthId, currentMonthId, 0.0, 0.0);
    }

    public void apply(HistoDiffColors colors, String messageKey, String... args) {
      histoChart.update(new HistoDiffPainter(dataset, colors, showActualInTheFuture));
      updateHistoLabel(messageKey, args);
    }
  }

  private String getLabel(int monthId) {
    return Month.getOneLetterMonthLabel(monthId);
  }

  private void updateHistoLabel(String messageKey, String... args) {
    updateLabel(histoChartLabel, "chart.histo." + messageKey, args);
  }

  private void updateLabel(JLabel label, String messageKey, String... args) {
    label.setText(Lang.get("seriesEvolution." + messageKey, args));
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

  private String getSection(int monthId) {
    return Integer.toString(Month.toYear(monthId));
  }
}
