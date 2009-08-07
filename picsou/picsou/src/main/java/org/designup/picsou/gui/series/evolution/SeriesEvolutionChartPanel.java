package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffPainter;
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
  private TimeService timeService;

  public SeriesEvolutionChartPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    directory.get(SelectionService.class).addListener(this, SeriesWrapper.TYPE);

    this.currentWrapper = getMainSummaryWrapper();

    histoChart = new HistoChart(directory);

    balanceColors = new HistoDiffColors(
      "histo.balance.income.line",
      "histo.balance.income.overrun",
      "histo.balance.expenses.line",
      "histo.balance.expenses.overrun",
      "histo.balance.fill",
      "histo.balance.fill.selected",
      directory
    );

    incomeColors = new HistoDiffColors(
      "histo.balance.income.line.planned",
      "histo.chart.bg",
      "histo.balance.income.line",
      "histo.balance.income.overrun",
      "histo.balance.income.fill",
      "histo.balance.income.fill.selected",
      directory
    );

    expensesColors = new HistoDiffColors(
      "histo.balance.expenses.line.planned",
      "histo.chart.bg",
      "histo.balance.expenses.line",
      "histo.balance.expenses.overrun",
      "histo.balance.expenses.fill",
      "histo.balance.expenses.fill.selected",
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
        updateBudgetArea(BudgetArea.get(currentWrapper.get(SeriesWrapper.ITEM_ID)));
        break;

      case SERIES:
        updateSeries(currentWrapper.get(SeriesWrapper.ITEM_ID));
        break;

      case SUMMARY:
        Integer id = currentWrapper.get(SeriesWrapper.ID);
        if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)
            || id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
          updateMainBalance();
        }
        else if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
          updateSavingsBalance();
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

  private String getMonthLabel(int monthId) {
    return Month.getOneLetterMonthLabel(monthId);
  }

  private void updateSavingsBalance() {
    DatasetBuilder dataset = new DatasetBuilder();

    for (int monthId : getMonthIds()) {
      String label = getMonthLabel(monthId);
      Glob balanceStat = repository.find(Key.create(SavingsBalanceStat.MONTH, monthId,
                                                    SavingsBalanceStat.ACCOUNT, Account.SAVINGS_SUMMARY_ACCOUNT_ID));
      if (balanceStat != null) {
        Double reference = balanceStat.get(SavingsBalanceStat.SAVINGS_PLANNED);
        Double actual = balanceStat.get(SavingsBalanceStat.SAVINGS);
        dataset.add(monthId, reference, actual);
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
