package org.designup.picsou.gui.series.analysis;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.charts.stack.utils.StackChartAdapter;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.*;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesChartsPanel implements GlobSelectionListener {

  private GlobRepository repository;
  private Directory directory;

  private Integer referenceMonthId;
  private SortedSet<Integer> selectedMonthIds;
  private Set<Key> selectedWrapperKeys = new HashSet<Key>();

  private HistoChartBuilder histoChartBuilder;

  private StackChart balanceChart;
  private StackChart seriesChart;

  private JLabel balanceChartLabel;
  private JLabel seriesChartLabel;

  private StackChartColors balanceStackColors;
  private StackChartColors incomeStackColors;
  private StackChartColors expensesStackColors;
  private SelectionService selectionService;

  public SeriesChartsPanel(final GlobRepository repository,
                           Directory directory,
                           final SelectionService parentSelectionService) {
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, SeriesWrapper.TYPE);

    setMainSummaryWrapperKey();

    histoChartBuilder = new HistoChartBuilder(new HistoChartConfig(true, true, false, true, true, false, true, false, false),
                                              new ScrollableHistoChartRange(12, 6, false, repository),
                                              repository, directory, parentSelectionService);
    histoChartBuilder.addListener(new HistoChartListenerAdapter() {
      public void scroll(int count) {
        update(false);
      }
    });
    balanceChart = new StackChart();
    seriesChart = new StackChart();

    balanceStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                           "stack.expenses.bar", "stack.expenses.border", directory);
    incomeStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                          "stack.income.bar", "stack.income.border", directory);
    expensesStackColors = createStackColors("stack.expenses.bar", "stack.expenses.bar",
                                            "stack.expenses.bar", "stack.expenses.bar", directory);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BudgetStat.TYPE)
            || changeSet.containsChanges(Series.TYPE)
            || changeSet.containsChanges(SavingsBudgetStat.TYPE)
            || changeSet.containsChanges(PeriodSeriesStat.TYPE)
            || changeSet.containsChanges(SeriesStat.TYPE)) {
          update(true);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(true);
      }
    });

    StackSelectionListener listener = new StackSelectionListener();
    balanceChart.addListener(listener);
    seriesChart.addListener(listener);
  }

  public void reset() {
    setMainSummaryWrapperKey();
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
      "stack.floor",
      "stack.selection.border",
      "stack.rollover.text",
      directory
    );
  }

  public void registerCharts(GlobsPanelBuilder builder) {
    builder.add("histoChart", histoChartBuilder.getChart());
    builder.add("balanceChart", balanceChart);
    builder.add("seriesChart", seriesChart);

    builder.add("histoChartLabel", histoChartBuilder.getLabel());
    builder.add("histoChartLegend", histoChartBuilder.getLegend());
    balanceChartLabel = builder.add("balanceChartLabel", new JLabel()).getComponent();
    seriesChartLabel = builder.add("seriesChartLabel", new JLabel()).getComponent();
  }

  public void monthSelected(Integer monthId, SortedSet<Integer> monthIds) {
    this.referenceMonthId = monthId;
    this.selectedMonthIds = monthIds;
    update(true);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(SeriesWrapper.TYPE)) {
      GlobList selectedWrappers = selection.getAll(SeriesWrapper.TYPE);
      if (selectedWrappers.isEmpty()) {
        setMainSummaryWrapperKey();
      }
      else {
        selectedWrapperKeys.clear();
        selectedWrapperKeys.addAll(selectedWrappers.getKeyList());
      }
    }

    update(true);
  }

  private void setMainSummaryWrapperKey() {
    selectedWrapperKeys.clear();
    selectedWrapperKeys.add(Key.create(SeriesWrapper.TYPE, SeriesWrapper.BALANCE_SUMMARY_ID));
  }

  private void update(final boolean resetPosition) {
    GlobList selectedWrappers = getSelectedWrappers();
    Set<Integer> types = selectedWrappers.getValueSet(SeriesWrapper.ITEM_TYPE);
    if ((referenceMonthId == null) || repository.find(CurrentMonth.KEY) == null) {
      histoChartBuilder.clear();
      return;
    }

    Set<BudgetArea> budgetAreas = getBudgetAreas(selectedWrappers);
    if (budgetAreas.size() == 1 && budgetAreas.contains(BudgetArea.UNCATEGORIZED)) {
      histoChartBuilder.showUncategorizedHisto(referenceMonthId, resetPosition);
      updateUncategorizedBalanceStack();
      updateUncategorizedSeriesStack();
      return;
    }

    if (types.contains(SeriesWrapperType.SUMMARY.getId())) {
      Integer id = selectedWrappers
        .filter(fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUMMARY.getId()), repository)
        .getFirst().get(SeriesWrapper.ID);
      if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
        histoChartBuilder.showMainBalanceHisto(referenceMonthId, resetPosition);
        updateMainBalanceStack(Collections.<BudgetArea>emptySet());
        updateMainAccountExpensesSeriesStack();
      }
      else if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
        histoChartBuilder.showMainAccountsHisto(referenceMonthId, resetPosition);
        updateMainBalanceStack(Collections.<BudgetArea>emptySet());
        updateMainAccountExpensesSeriesStack();
      }
      else if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
        histoChartBuilder.showSavingsAccountsHisto(referenceMonthId, resetPosition);
        updateSavingsStacks();
      }
      return;
    }

    Set<Integer> seriesIds =
      selectedWrappers
        .filter(fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()), repository)
        .getValueSet(SeriesWrapper.ITEM_ID);
    for (Integer seriesId : seriesIds) {
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      budgetAreas.add(BudgetArea.get(series.get(Series.BUDGET_AREA)));
    }
    if (seriesIds.isEmpty()) {
      histoChartBuilder.showBudgetAreaHisto(budgetAreas, referenceMonthId, resetPosition);
    }
    else {
      histoChartBuilder.showSeriesHisto(seriesIds, referenceMonthId, resetPosition);
    }

    updateMainBalanceStack(budgetAreas);
    updateBudgetAreaSeriesStack(budgetAreas, seriesIds);
  }

  private Set<BudgetArea> getBudgetAreas(GlobList selectedWrappers) {
    return new HashSet<BudgetArea>(
      BudgetArea.getAll(
        selectedWrappers
          .filter(fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()), repository)
          .getValues(SeriesWrapper.ITEM_ID)));
  }

  private GlobList getSelectedWrappers() {
    GlobList selectedWrappers = new GlobList();
    selectedWrappers.addAll(selectedWrapperKeys, repository);
    return selectedWrappers;
  }

  private void updateMainBalanceStack(Set<BudgetArea> selectedBudgetAreas) {
    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();
    for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
      double amount = 0.00;
      for (Integer monthId : selectedMonthIds) {
        Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
        if (budgetStat != null) {
          amount += budgetStat.get(BudgetStat.getSummary(budgetArea));
        }
      }
      StackChartDataset dataset = amount > 0 ? incomeDataset : expensesDataset;
      dataset.add(budgetArea.getLabel(),
                  Math.abs(amount),
                  budgetArea.getKey(),
                  selectedBudgetAreas.contains(budgetArea));
    }

    balanceChart.update(incomeDataset, expensesDataset, balanceStackColors);
    updateBalanceLabel("mainBalance");
  }

  private void updateMainAccountExpensesSeriesStack() {
    StackChartDataset dataset = new StackChartDataset();

    GlobList stats = repository.getAll(SeriesStat.TYPE,
                                       and(fieldIn(SeriesStat.MONTH, selectedMonthIds),
                                           not(fieldEquals(SeriesStat.SERIES, Series.UNCATEGORIZED_SERIES_ID))));
    for (Integer seriesId : stats.getValueSet(SeriesStat.SERIES)) {
      Double amount = getTotalAmountForSelectedPeriod(seriesId);
      if (amount != null && amount < 0) {
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        String name = series.get(Series.NAME);
        dataset.add(name, -amount, series.getKey());
      }
    }

    seriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("mainAccount");
  }

  private void updateBudgetAreaSeriesStack(Set<BudgetArea> budgetAreas, Set<Integer> selectedSeriesIds) {
    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();

    GlobList stats = repository.getAll(SeriesStat.TYPE, fieldIn(SeriesStat.MONTH, selectedMonthIds));
    for (Integer seriesId : stats.getValueSet(SeriesStat.SERIES)) {
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      if (budgetAreas.contains(BudgetArea.get(series.get(Series.BUDGET_AREA)))
          && !Series.isSavingToExternal(series)) {
        double amount = getTotalAmountForSelectedPeriod(seriesId);
        StackChartDataset targetDataset = amount >= 0 ? incomeDataset : expensesDataset;
        targetDataset.add(series.get(Series.NAME),
                          Math.abs(amount),
                          series.getKey(),
                          selectedSeriesIds.contains(seriesId));
      }
    }

    if (!incomeDataset.isEmpty() && !expensesDataset.isEmpty()) {
      seriesChart.update(incomeDataset, expensesDataset, balanceStackColors);
    }
    else if (!incomeDataset.isEmpty()) {
      seriesChart.update(incomeDataset, incomeStackColors);
    }
    else if (!expensesDataset.isEmpty()) {
      seriesChart.update(expensesDataset, expensesStackColors);
    }
    else {
      seriesChart.clear();
    }
    if (budgetAreas.size() == 1) {
      updateSeriesLabel("budgetArea", budgetAreas.iterator().next().getLabel().toLowerCase());
    }
    else {
      updateSeriesLabel("budgetAreas", Integer.toString(budgetAreas.size()));
    }
  }

  private void updateUncategorizedBalanceStack() {

    double uncategorized = 0;
    for (Integer monthId : selectedMonthIds) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      uncategorized += budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0;
    }

    StackChartDataset dataset = new StackChartDataset();
    if (uncategorized > 0.01) {
      dataset.add(Lang.get("seriesAnalysis.chart.balance.uncategorized.tocategorize"),
                  uncategorized, null, false);
    }

    double categorized = 0.0;
    for (Glob seriesStat : repository.getAll(SeriesStat.TYPE,
                                             and(fieldIn(SeriesStat.MONTH, selectedMonthIds),
                                                 not(fieldEquals(SeriesStat.SERIES, Series.UNCATEGORIZED_SERIES_ID))))) {
      if (!Series.isSavingToExternal(repository.findLinkTarget(seriesStat, SeriesStat.SERIES))) {
        categorized += Math.abs(seriesStat.get(SeriesStat.AMOUNT, 0.00));
      }
    }
    dataset.add(Lang.get("seriesAnalysis.chart.balance.uncategorized.categorized"),
                categorized, null, false);

    balanceChart.update(dataset, expensesStackColors);
    updateBalanceLabel("uncategorized");
  }

  private void updateUncategorizedSeriesStack() {

    GlobList uncategorizedTransactions =
      repository.getAll(Transaction.TYPE,
                        and(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                            fieldIn(Transaction.BUDGET_MONTH, selectedMonthIds)));

    StackChartDataset dataset = new StackChartDataset();
    for (Glob transaction : uncategorizedTransactions) {
      dataset.add(Strings.cut(transaction.get(Transaction.LABEL), 20),
                  Math.abs(transaction.get(Transaction.AMOUNT)),
                  transaction.getKey(),
                  false);
    }

    seriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("uncategorized");
  }

  private void updateSavingsStacks() {

    double savingsIn = 0;
    double savingsOut = 0;
    StackChartDataset seriesInDataset = new StackChartDataset();
    StackChartDataset seriesOutDataset = new StackChartDataset();

    GlobList stats = repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, referenceMonthId));

    for (Integer seriesId : stats.getValueSet(SeriesStat.SERIES)) {

      Glob series = repository.find(Key.create(Series.TYPE, seriesId));
      if (series == null ||
          !series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId()) ||
          Series.isSavingToExternal(series)) {
        continue;
      }

      Double amount = getTotalAmountForSelectedPeriod(seriesId);
      if (amount == null) {
        continue;
      }

      Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
      Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
      boolean isFromSavingsAccount = Account.isSavings(fromAccount);
      boolean isToSavingsAccount = Account.isSavings(toAccount);
      if (isFromSavingsAccount) {
        if (series.get(Series.FROM_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
          savingsOut += -amount;
          seriesOutDataset.add(series.get(Series.NAME),
                               -amount,
                               series.getKey());
        }
      }
      if (isToSavingsAccount) {
        if (series.get(Series.TO_ACCOUNT).equals(series.get(Series.TARGET_ACCOUNT))) {
          savingsIn += amount;
          seriesInDataset.add(series.get(Series.NAME),
                              amount,
                              series.getKey());
        }
      }
    }

    updateSavingsBalanceStack(savingsIn, savingsOut);
    updateSavingsSeriesStack(seriesInDataset, seriesOutDataset);
  }

  private double getTotalAmountForSelectedPeriod(Integer seriesId) {
    if (selectedMonthIds.size() == 1) {
      Integer monthId = selectedMonthIds.first();
      Glob stat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
      if ((stat != null) && stat.isTrue(SeriesStat.ACTIVE)) {
        return stat.get(SeriesStat.SUMMARY_AMOUNT, 0.00);
      }
      else {
        return 0.00;
      }
    }

    Double amount = null;
    for (Glob stat : repository.findByIndex(SeriesStat.SERIES_INDEX, seriesId)) {
      if (selectedMonthIds.contains(stat.get(SeriesStat.MONTH)) && stat.isTrue(SeriesStat.ACTIVE)) {
        Double statAmount = stat.get(SeriesStat.SUMMARY_AMOUNT);
        if (statAmount != null) {
          if (amount == null) {
            amount = 0.00;
          }
          amount += statAmount;
        }
      }
    }
    return amount != null ? amount : 0.00;
  }

  private void updateSavingsBalanceStack(double savingsIn, double savingsOut) {
    StackChartDataset incomeDataset = new StackChartDataset();
    incomeDataset.add(Lang.get("seriesAnalysis.chart.balance.savingsIn"), savingsIn, null);

    StackChartDataset expensesDataset = new StackChartDataset();
    expensesDataset.add(Lang.get("seriesAnalysis.chart.balance.savingsOut"), savingsOut, null);

    balanceChart.update(incomeDataset, expensesDataset, balanceStackColors);
    updateBalanceLabel("savingsBalance");
  }

  private void updateSavingsSeriesStack(StackChartDataset seriesInDataset, StackChartDataset seriesOutDataset) {
    seriesChart.update(seriesInDataset, seriesOutDataset, balanceStackColors);
    updateSeriesLabel("savingsSeries");
  }

  private void updateBalanceLabel(String messageKey, String... args) {
    updateLabel(balanceChartLabel, "chart.balance." + messageKey, args);
  }

  private void updateSeriesLabel(String messageKey, String... args) {
    updateLabel(seriesChartLabel, "chart.series." + messageKey, args);
  }

  private void updateLabel(JLabel label, String messageKey, String... args) {
    label.setText(Lang.get("seriesAnalysis." + messageKey, args));
  }

  private class StackSelectionListener extends StackChartAdapter {
    public void processClick(Key selectedKey, boolean expandSelection) {
      if (selectedKey == null) {
        return;
      }

      Glob glob = repository.find(selectedKey);
      if (Transaction.TYPE.equals(selectedKey.getGlobType())) {
        selectTransactions(glob);
        return;
      }

      Set<Glob> wrappers = new HashSet<Glob>();
      if (expandSelection) {
        wrappers.addAll(selectionService.getSelection(SeriesWrapper.TYPE));
      }
      wrappers.addAll(SeriesWrapper.findAll(new GlobList(glob), repository));
      selectionService.select(wrappers, SeriesWrapper.TYPE);
    }

    private void selectTransactions(Glob transaction) {
      directory.get(NavigationService.class).gotoCategorization(new GlobList(transaction), true);
    }
  }
}
