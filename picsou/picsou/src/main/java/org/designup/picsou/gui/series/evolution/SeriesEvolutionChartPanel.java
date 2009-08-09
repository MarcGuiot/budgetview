package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.painters.*;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.ArrayList;
import java.util.List;

public class SeriesEvolutionChartPanel implements GlobSelectionListener {

  private GlobRepository repository;

  private HistoChart histoChart;
  private Integer currentMonthId;
  private Glob currentWrapper;

  private HistoDiffColors balanceColors;
  private HistoDiffColors incomeColors;
  private HistoDiffColors expensesColors;
  private HistoLineColors uncategorizedColors;
  private HistoLineColors accountColors;

  private TimeService timeService;

  public SeriesEvolutionChartPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, SeriesWrapper.TYPE);

    this.currentWrapper = getMainSummaryWrapper();

    histoChart = new HistoChart(directory);

    balanceColors = new HistoDiffColors(
      "histo.income.line",
      "histo.income.overrun",
      "histo.expenses.line",
      "histo.expenses.overrun",
      "histo.balance.fill",
      "histo.balance.fill.selected",
      directory
    );

    incomeColors = new HistoDiffColors(
      "histo.balance.income.line.planned",
      "histo.chart.bg",
      "histo.income.line",
      "histo.income.overrun",
      "histo.balance.income.fill",
      "histo.balance.income.fill.selected",
      directory
    );

    expensesColors = new HistoDiffColors(
      "histo.balance.expenses.line.planned",
      "histo.chart.bg",
      "histo.expenses.line",
      "histo.expenses.overrun",
      "histo.expenses.fill",
      "histo.expenses.fill.selected",
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

    timeService = directory.get(TimeService.class);
  }

  public HistoChart getHistoChart() {
    return histoChart;
  }

  public void monthSelected(Integer monthId) {
    this.currentMonthId = monthId;
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(SeriesWrapper.TYPE)) {
      GlobList wrappers = selection.getAll(SeriesWrapper.TYPE);
      if (wrappers.size() == 1) {
        currentWrapper = wrappers.getFirst();
      }
      else if (wrappers.size() == 0) {
        currentWrapper = getMainSummaryWrapper();
      }
      else {
        currentWrapper = null;
      }
    }

    update();
  }

  private Glob getMainSummaryWrapper() {
    return repository.find(Key.create(SeriesWrapper.TYPE, SeriesWrapper.BALANCE_SUMMARY_ID));
  }

  private void update() {
    if ((currentMonthId == null) || (currentWrapper == null)) {
      histoChart.clear();
      return;
    }

    switch (SeriesWrapperType.get(currentWrapper)) {
      case BUDGET_AREA:
        BudgetArea budgetArea = BudgetArea.get(currentWrapper.get(SeriesWrapper.ITEM_ID));
        if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
          updateUncategorized();
        }
        else {
          updateBudgetArea(budgetArea);
        }
        break;

      case SERIES:
        updateSeries(currentWrapper.get(SeriesWrapper.ITEM_ID));
        break;

      case SUMMARY:
        Integer id = currentWrapper.get(SeriesWrapper.ID);
        if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
          updateMainBalance();
        }
        else if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
          updateMainAccounts();
        }
        else if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
          updateSavingsAccounts();
        }
        break;

      default:
        throw new InvalidParameter("Unexpected case: " + currentWrapper);
    }
  }

  private void updateMainBalance() {
    DatasetBuilder dataset = new DatasetBuilder();

    for (int monthId : getMonthIds()) {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, monthId));
      if (balanceStat != null) {
        Double income = balanceStat.get(BalanceStat.INCOME);
        Double expense = balanceStat.get(BalanceStat.EXPENSE);
        dataset.add(monthId, income, expense != null ? -expense : null);
      }
      else {
        dataset.addEmpty(monthId);
      }
    }

    dataset.apply(balanceColors);
  }

  private void updateBudgetArea(BudgetArea budgetArea) {
    DatasetBuilder dataset = new DatasetBuilder();

    DoubleField plannedField = BalanceStat.getPlanned(budgetArea);
    DoubleField actualField = BalanceStat.getObserved(budgetArea);
    dataset.setInverted(!budgetArea.isIncome());

    for (int monthId : getMonthIds()) {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, monthId));
      if (balanceStat != null) {
        Double planned = balanceStat.get(plannedField);
        Double actual = balanceStat.get(actualField);
        dataset.add(monthId, planned, actual);
      }
      else {
        dataset.addEmpty(monthId);
      }
    }

    dataset.apply(budgetArea.isIncome() ? incomeColors : expensesColors);
  }

  private void updateUncategorized() {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds()) {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, monthId));
      double value = balanceStat != null ? balanceStat.get(BalanceStat.UNCATEGORIZED) : 0.0;
      dataset.add(value, getMonthLabel(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, uncategorizedColors));
  }

  private void updateSeries(Integer seriesId) {
    DatasetBuilder dataset = new DatasetBuilder();

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    dataset.setInverted(!budgetArea.isIncome());

    for (int monthId : getMonthIds()) {
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if (stat != null) {
        Double planned = stat.get(SeriesStat.PLANNED_AMOUNT);
        Double actual = stat.get(SeriesStat.AMOUNT);
        dataset.add(monthId, planned, actual);
      }
      else {
        dataset.addEmpty(monthId);
      }
    }

    dataset.apply(budgetArea.isIncome() ? incomeColors : expensesColors);
  }

  private void updateMainAccounts() {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds()) {
      String label = getMonthLabel(monthId);
      Glob stat = repository.find(Key.create(BalanceStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION) : 0.0;
      dataset.add(value, getMonthLabel(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
  }

  private void updateSavingsAccounts() {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds()) {
      String label = getMonthLabel(monthId);
      Glob stat = repository.find(Key.create(SavingsBalanceStat.MONTH, monthId,
                                                    SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID));
      Double value = stat != null ? stat.get(SavingsBalanceStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(value, getMonthLabel(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
  }

  private String getMonthLabel(int monthId) {
    return Month.getOneLetterMonthLabel(monthId);
  }

  private class DatasetBuilder {

    private HistoDiffDataset dataset = new HistoDiffDataset();
    private int multiplier = 1;

    public void setInverted(boolean inverted) {
      this.multiplier = inverted ? -1 : 1;
    }

    public void add(int monthId, Double reference, Double actual) {
      String label = getMonthLabel(monthId);
      dataset.add(reference != null ? reference * multiplier : 0,
                  actual != null ? actual * multiplier : 0,
                  label,
                  monthId == currentMonthId,
                  monthId > timeService.getCurrentMonthId());
    }

    public void addEmpty(int monthId) {
      add(monthId, 0.0, 0.0);
    }

    public void apply(HistoDiffColors colors) {
      histoChart.update(new HistoDiffPainter(dataset, colors));
    }
  }

  public List<Integer> getMonthIds() {
    List<Integer> result = new ArrayList<Integer>();
    for (Integer monthId : Month.range(Month.normalize(currentMonthId - 100), Month.normalize(currentMonthId + 6))) {
      if (repository.contains(Key.create(Month.TYPE, monthId))) {
        result.add(monthId);
      }
    }
    return result;
  }
}
