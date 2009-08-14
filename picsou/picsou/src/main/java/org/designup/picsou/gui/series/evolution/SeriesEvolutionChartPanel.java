package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.painters.*;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.model.BalanceStat;
import org.designup.picsou.gui.model.SavingsBalanceStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SeriesEvolutionChartPanel implements GlobSelectionListener {

  private GlobRepository repository;

  private Integer currentMonthId;
  private Glob currentWrapper;

  private HistoChart histoChart;
  private StackChart balanceChart;
  private StackChart seriesChart;

  private JLabel histoChartLabel;
  private JLabel balanceChartLabel;
  private JLabel seriesChartLabel;

  private HistoDiffColors balanceColors;
  private HistoDiffColors incomeColors;
  private HistoDiffColors expensesColors;
  private HistoLineColors uncategorizedColors;
  private HistoLineColors accountColors;

  private StackChartColors balanceStackColors;
  private StackChartColors incomeStackColors;
  private StackChartColors expensesStackColors;
  private SelectionService selectionService;

  public SeriesEvolutionChartPanel(final GlobRepository repository,
                                   Directory directory,
                                   final SelectionService parentSelectionService) {
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, SeriesWrapper.TYPE);

    this.currentWrapper = getMainSummaryWrapper();

    histoChart = new HistoChart(directory);
    balanceChart = new StackChart();
    seriesChart = new StackChart();

    histoChart.setListener(new HistoChartListener() {
      public void columnClicked(int monthId) {
        Glob month = repository.get(Key.create(Month.TYPE, monthId));
        parentSelectionService.select(month);
      }
    });

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
      "histo.chart.bg",
      "histo.income.line",
      "histo.income.overrun",
      "histo.balance.income.fill",
      directory
    );

    expensesColors = new HistoDiffColors(
      "histo.balance.expenses.line.planned",
      "histo.chart.bg",
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

    balanceStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                           "stack.expenses.bar", "stack.expenses.border", directory);
    incomeStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                          "stack.income.bar", "stack.income.border", directory);
    expensesStackColors = createStackColors("stack.expenses.bar", "stack.expenses.bar",
                                            "stack.expenses.bar", "stack.expenses.bar", directory);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BalanceStat.TYPE)
            || changeSet.containsChanges(SavingsBalanceStat.TYPE)
            || changeSet.containsChanges(SeriesStat.TYPE)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update();
      }
    });
  }

  private StackChartColors createStackColors(String leftBar, String leftBorder,
                                             String rightBar, String rightBorder,
                                             Directory directory) {
    return new StackChartColors(
      leftBar,
      rightBar,
      "stack.barText",
      "stack.label",
      "stack.border",
      "stack.selection.border",
      "stack.rollover.text",
      directory
    );
  }

  public void registerCharts(GlobsPanelBuilder builder) {
    builder.add("histoChart", histoChart);
    builder.add("balanceChart", balanceChart);
    builder.add("seriesChart", seriesChart);

    histoChartLabel = builder.add("histoChartLabel", new JLabel());
    balanceChartLabel = builder.add("balanceChartLabel", new JLabel());
    seriesChartLabel = builder.add("seriesChartLabel", new JLabel());
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
    if ((currentWrapper != null) && !currentWrapper.exists()) {
      currentWrapper = null;
    }
    if ((currentMonthId == null) || (currentWrapper == null)) {
      histoChart.clear();
      return;
    }

    switch (SeriesWrapperType.get(currentWrapper)) {
      case BUDGET_AREA: {
        BudgetArea budgetArea = BudgetArea.get(currentWrapper.get(SeriesWrapper.ITEM_ID));
        if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
          updateUncategorizedHisto();
          updateMainBalanceStack(budgetArea);
          clearSeriesStack();
        }
        else {
          updateBudgetAreaHisto(budgetArea);
          updateMainBalanceStack(budgetArea);
          updateBudgetAreaSeriesStack(budgetArea, null);
        }
      }
      break;

      case SERIES: {
        Integer seriesId = currentWrapper.get(SeriesWrapper.ITEM_ID);
        BudgetArea budgetArea = Series.getBudgetArea(seriesId, repository);
        updateSeriesHisto(seriesId);
        updateMainBalanceStack(budgetArea);
        updateBudgetAreaSeriesStack(budgetArea, seriesId);
      }
      break;

      case SUMMARY: {
        Integer id = currentWrapper.get(SeriesWrapper.ID);
        if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
          updateMainBalanceHisto();
          updateMainBalanceStack(null);
          updateMainAccountSeriesStack();
        }
        else if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
          updateMainAccountsHisto();
          updateMainBalanceStack(null);
          updateMainAccountSeriesStack();
        }
        else if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
          updateSavingsAccountsHisto();
          updateSavingsBalanceStack();
          clearSeriesStack();
        }
      }
      break;

      default:
        throw new InvalidParameter("Unexpected case: " + currentWrapper);
    }
  }

  private void updateMainBalanceHisto() {
    HistoDatasetBuilder dataset = new HistoDatasetBuilder();

    for (int monthId : getMonthIds()) {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, monthId));
      if (balanceStat != null) {
        Double income = balanceStat.get(BalanceStat.INCOME_SUMMARY);
        Double expense = balanceStat.get(BalanceStat.EXPENSE_SUMMARY);
        dataset.add(monthId, income, expense != null ? -expense : null);
      }
      else {
        dataset.addEmpty(monthId);
      }
    }

    dataset.apply(balanceColors,
                  "mainBalance",
                  Colors.toString(balanceColors.getReferenceLineColor()),
                  Colors.toString(balanceColors.getActualLineColor()));
  }

  private void updateMainBalanceStack(BudgetArea selectedBudgetArea) {

    Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, currentMonthId));
    if (balanceStat == null) {
      balanceChart.clear();
      return;
    }

    StackChartDataset incomeDataset = createStackDataset(balanceStat, selectedBudgetArea, BudgetArea.INCOME_AREAS);
    StackChartDataset expensesDataset = createStackDataset(balanceStat, selectedBudgetArea, BudgetArea.EXPENSES_AREAS);

    balanceChart.update(incomeDataset, expensesDataset, balanceStackColors);
    updateBalanceLabel("mainBalance");
  }

  private void updateMainAccountSeriesStack() {
    StackChartDataset dataset = new StackChartDataset();
    dataset.setInverted(true);

    for (Glob stat : repository.getAll(SeriesStat.TYPE,
                                       and(fieldEquals(SeriesStat.MONTH, currentMonthId),
                                           not(fieldEquals(SeriesStat.SERIES, Series.UNCATEGORIZED_SERIES_ID))))) {
      Integer seriesId = stat.getKey().get(SeriesStat.SERIES);
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
      if (!budgetArea.isIncome()) {
        dataset.add(series.get(Series.NAME), stat.get(SeriesStat.SUMMARY_AMOUNT), createSelectionAction(seriesId));
      }
    }

    seriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("mainAccount");
  }

  private void updateBudgetAreaHisto(BudgetArea budgetArea) {
    HistoDatasetBuilder dataset = new HistoDatasetBuilder();

    DoubleField plannedField = BalanceStat.getPlanned(budgetArea);
    DoubleField actualField = BalanceStat.getObserved(budgetArea);
    dataset.setInverted(!budgetArea.isIncome());
    dataset.setActualHiddenInTheFuture();

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

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    dataset.apply(colors,
                  "budgetArea",
                  budgetArea.getLabel(),
                  Colors.toString(colors.getReferenceLineColor()),
                  Colors.toString(colors.getActualLineColor()));
  }

  private void updateBudgetAreaSeriesStack(BudgetArea budgetArea, Integer selectedSeriesId) {
    StackChartDataset dataset = new StackChartDataset();
    dataset.setInverted(!budgetArea.isIncome());

    for (Glob stat : repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, currentMonthId))) {
      Integer seriesId = stat.getKey().get(SeriesStat.SERIES);
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      if (budgetArea.getId().equals(series.get(Series.BUDGET_AREA))) {
        dataset.add(series.get(Series.NAME),
                    stat.get(SeriesStat.SUMMARY_AMOUNT),
                    createSelectionAction(seriesId),
                    seriesId.equals(selectedSeriesId));
      }
    }

    StackChartColors colors = budgetArea.isIncome() ? incomeStackColors : expensesStackColors;
    seriesChart.update(dataset, colors);
    updateSeriesLabel("budgetArea", budgetArea.getLabel().toLowerCase());
  }

  private void updateUncategorizedHisto() {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds()) {
      Glob balanceStat = repository.find(Key.create(BalanceStat.TYPE, monthId));
      double value = balanceStat != null ? balanceStat.get(BalanceStat.UNCATEGORIZED) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, uncategorizedColors));
    updateHistoLabel("uncategorized");
  }

  private void updateSeriesHisto(Integer seriesId) {
    HistoDatasetBuilder dataset = new HistoDatasetBuilder();

    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    BudgetArea budgetArea = BudgetArea.get(series.get(Series.BUDGET_AREA));
    dataset.setInverted(!budgetArea.isIncome());
    dataset.setActualHiddenInTheFuture();

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

    HistoDiffColors colors = budgetArea.isIncome() ? incomeColors : expensesColors;
    dataset.apply(colors,
                  "series",
                  series.get(Series.NAME),
                  Colors.toString(colors.getReferenceLineColor()),
                  Colors.toString(colors.getActualLineColor()));
  }

  private void updateMainAccountsHisto() {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds()) {
      Glob stat = repository.find(Key.create(BalanceStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
    updateHistoLabel("mainAccounts");
  }

  private void updateSavingsAccountsHisto() {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds()) {
      Glob stat = SavingsBalanceStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBalanceStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
    updateHistoLabel("savingsAccounts");
  }

  private void updateSavingsBalanceStack() {

    Glob balanceStat = SavingsBalanceStat.findSummary(currentMonthId, repository);

    StackChartDataset incomeDataset = new StackChartDataset();

    StackChartDataset expensesDataset = new StackChartDataset();

    balanceChart.update(incomeDataset, expensesDataset, balanceStackColors);
    updateBalanceLabel("savingsBalance");
  }

  private void clearBalanceStack() {
    balanceChart.clear();
    balanceChartLabel.setText(null);
  }

  private void clearSeriesStack() {
    seriesChart.clear();
    balanceChartLabel.setText(null);
  }

  private StackChartDataset createStackDataset(Glob balanceStat,
                                               BudgetArea selectedBudgetArea,
                                               BudgetArea... budgetAreas) {
    StackChartDataset dataset = new StackChartDataset();
    for (BudgetArea budgetArea : budgetAreas) {
      dataset.setInverted(!budgetArea.isIncome());
      dataset.add(budgetArea.getLabel(),
                  balanceStat.get(BalanceStat.getSummary(budgetArea)),
                  createSelectionAction(budgetArea),
                  budgetArea.equals(selectedBudgetArea));
    }
    return dataset;
  }

  private String getLabel(int monthId) {
    return Month.getOneLetterMonthLabel(monthId);
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

    public void add(int monthId, Double reference, Double actual) {
      dataset.add(monthId,
                  reference != null ? reference * multiplier : 0,
                  actual != null ? actual * multiplier : 0,
                  getLabel(monthId), getSection(monthId),
                  monthId == currentMonthId,
                  monthId > lastMonthWithTransactions);
    }

    public void addEmpty(int monthId) {
      add(monthId, 0.0, 0.0);
    }

    public void apply(HistoDiffColors colors, String messageKey, String... args) {
      histoChart.update(new HistoDiffPainter(dataset, colors, showActualInTheFuture));
      updateHistoLabel(messageKey, args);
    }
  }

  private void updateHistoLabel(String messageKey, String... args) {
    updateLabel(histoChartLabel, "chart.histo." + messageKey, args);
  }

  private void updateBalanceLabel(String messageKey, String... args) {
    updateLabel(balanceChartLabel, "chart.balance." + messageKey, args);
  }

  private void updateSeriesLabel(String messageKey, String... args) {
    updateLabel(seriesChartLabel, "chart.series." + messageKey, args);
  }

  private void updateLabel(JLabel label, String messageKey, String... args) {
    label.setText(Lang.get("seriesEvolution." + messageKey, args));
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

  private Action createSelectionAction(final BudgetArea budgetArea) {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob wrapper = SeriesWrapper.getWrapperForBudgetArea(budgetArea, repository);
        selectionService.select(wrapper);
      }
    };
  }

  private Action createSelectionAction(final Integer seriesId) {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob wrapper = SeriesWrapper.getWrapperForSeries(seriesId, repository);
        selectionService.select(wrapper);
      }
    };
  }

  private String getSection(int monthId) {
    return Integer.toString(Month.toYear(monthId));
  }
}
