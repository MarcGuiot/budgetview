package org.designup.picsou.gui.analysis;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.card.NavigationPopup;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartColors;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.charts.stack.utils.StackChartAdapter;
import org.designup.picsou.gui.model.*;
import org.designup.picsou.gui.analysis.budget.StackToggleController;
import org.designup.picsou.gui.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.utils.SeriesOrGroup;
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

import static org.designup.picsou.gui.model.SeriesStat.*;
import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesChartsPanel implements GlobSelectionListener {

  private GlobRepository repository;
  private Directory directory;

  private Integer referenceMonthId;
  private SortedSet<Integer> selectedMonthIds;
  private Set<Key> selectedWrapperKeys = new HashSet<Key>();

  private HistoChartBuilder histoChartBuilder;

  private StackChart balanceChart;
  private StackChart rootSeriesChart;
  private StackChart groupSeriesChart;
  private StackChart subSeriesChart;

  private JLabel balanceChartLabel;
  private JLabel seriesChartLabel;
  private JLabel groupChartLabel;
  private JLabel subSeriesChartLabel;

  private StackChartColors balanceStackColors;
  private StackChartColors incomeStackColors;
  private StackChartColors expensesStackColors;
  private SelectionService selectionService;

  private static final Set<GlobType> USED_TYPES =
    new HashSet<GlobType>(Arrays.asList(BudgetStat.TYPE, Series.TYPE, SubSeries.TYPE,
                                        SavingsBudgetStat.TYPE, PeriodSeriesStat.TYPE, SeriesStat.TYPE, SubSeriesStat.TYPE,
                                        AccountStat.TYPE));
  private StackToggleController stackToggle;

  public SeriesChartsPanel(HistoChartRange range,
                           final GlobRepository repository,
                           Directory directory,
                           final SelectionService parentSelectionService) {
    this.repository = repository;
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, SeriesWrapper.TYPE);

    setMainSummaryWrapperKey();

    HistoChartConfig config = new HistoChartConfig(true, true, false, true, true, false, false, true, true, false);
    config.setUseWheelScroll(true);
    histoChartBuilder = new HistoChartBuilder(config,
                                              new HistoChartColors(directory), range, repository, directory, parentSelectionService);
    histoChartBuilder.addListener(new HistoChartListenerAdapter() {
      public void scroll(int count) {
        updateCharts(false);
      }
    });
    balanceChart = new StackChart();
    rootSeriesChart = new StackChart();
    groupSeriesChart = new StackChart();
    subSeriesChart = new StackChart();

    balanceStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                           "stack.expenses.bar", "stack.expenses.border", directory);
    incomeStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                          "stack.income.bar", "stack.income.border", directory);
    expensesStackColors = createStackColors("stack.expenses.bar", "stack.expenses.bar",
                                            "stack.expenses.bar", "stack.expenses.bar", directory);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        for (GlobType type : USED_TYPES) {
          if (changeSet.containsChanges(type)) {
            GlobList newSelection = new GlobList();
            for (Key wrapperKey : selectedWrapperKeys) {
              if (repository.contains(wrapperKey)) {
                newSelection.add(repository.get(wrapperKey));
              }
            }
            stackToggle.updateFromSelection(newSelection);
            updateCharts(true);
            return;
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (!Collections.disjoint(changedTypes, USED_TYPES)) {
          selectionService.clear(SeriesWrapper.TYPE);
        }
      }
    });

    new StackSelectionListener(balanceChart, parentSelectionService);
    new StackSelectionListener(rootSeriesChart, parentSelectionService);
    new StackSelectionListener(groupSeriesChart, parentSelectionService);
    new StackSelectionListener(subSeriesChart, parentSelectionService);

    stackToggle = new StackToggleController(balanceChart, rootSeriesChart, groupSeriesChart, subSeriesChart, repository);
  }

  public void reset() {
    stackToggle.showBudget(setMainSummaryWrapperKey());
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
    builder.add("seriesChart", rootSeriesChart);
    builder.add("groupChart", groupSeriesChart);
    builder.add("subSeriesChart", subSeriesChart);

    builder.add("histoChartLabel", histoChartBuilder.getLabel());
    builder.add("histoChartLegend", histoChartBuilder.getLegend());
    balanceChartLabel = builder.add("balanceChartLabel", new JLabel()).getComponent();
    seriesChartLabel = builder.add("seriesChartLabel", new JLabel()).getComponent();
    groupChartLabel = builder.add("groupChartLabel", new JLabel()).getComponent();
    subSeriesChartLabel = builder.add("subSeriesChartLabel", new JLabel()).getComponent();

    builder.add("gotoUpButton", stackToggle.getGotoUpButton());
    builder.add("gotoDownButton", stackToggle.getGotoDownButton());
  }

  public void monthSelected(Integer monthId, SortedSet<Integer> monthIds) {
    this.referenceMonthId = monthId;
    this.selectedMonthIds = monthIds;
    updateCharts(true);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(SeriesWrapper.TYPE)) {
      GlobList wrappers = selection.getAll(SeriesWrapper.TYPE);

      selectedWrapperKeys.clear();
      if (wrappers.isEmpty()) {
        wrappers.add(setMainSummaryWrapperKey());
      }
      else {
        selectedWrapperKeys.addAll(wrappers.getKeyList());
      }
      stackToggle.updateFromSelection(wrappers);
    }

    updateCharts(true);
  }

  private Glob setMainSummaryWrapperKey() {
    selectedWrapperKeys.clear();
    Key key = Key.create(SeriesWrapper.TYPE, SeriesWrapper.BALANCE_SUMMARY_ID);
    selectedWrapperKeys.add(key);
    return repository.find(key);
  }

  private void updateCharts(final boolean resetPosition) {
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

    Set<SeriesOrGroup> selectedSeriesOrGroups = getSeriesOrGroups(selectedWrappers);
    Set<Integer> seriesIds = getIds(selectedWrappers, SeriesWrapperType.SERIES);
    Set<Integer> subSeriesIds = getIds(selectedWrappers, SeriesWrapperType.SUB_SERIES);
    for (Integer subSeriesId : subSeriesIds) {
      Glob subSeries = repository.get(Key.create(SubSeries.TYPE, subSeriesId));
      seriesIds.add(subSeries.get(SubSeries.SERIES));
      selectedSeriesOrGroups.add(new SeriesOrGroup(subSeries.get(SubSeries.SERIES), SeriesType.SERIES));
    }
    for (SeriesOrGroup seriesOrGroup : selectedSeriesOrGroups) {
      budgetAreas.add(seriesOrGroup.getBudgetArea(repository));
    }

    if (selectedSeriesOrGroups.isEmpty()) {
      histoChartBuilder.showBudgetAreaHisto(budgetAreas, referenceMonthId, resetPosition);
    }
    else if (!subSeriesIds.isEmpty()) {
      histoChartBuilder.showSubSeriesHisto(subSeriesIds, referenceMonthId, resetPosition);
    }
    else { // Series or SeriesGroup
      histoChartBuilder.showSeriesHisto(selectedSeriesOrGroups, referenceMonthId, resetPosition);
    }

    if (isSingleSeries(selectedSeriesOrGroups)) {
      updateSubSeriesStacks(selectedSeriesOrGroups.iterator().next(), subSeriesIds);
    }
    else {
      clearSubSeriesStacks();
    }

    Set<Integer> groupIds = new HashSet<Integer>();
    for (Integer seriesId : seriesIds) {
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      if (series.get(Series.GROUP) != null) {
        Glob group = repository.findLinkTarget(series, Series.GROUP);
        groupIds.add(group.get(SeriesGroup.ID));
        selectedSeriesOrGroups.add(new SeriesOrGroup(group));
      }
    }

    Integer groupId = getGroupToShow(selectedSeriesOrGroups);
    if (groupId != null) {
      updateGroupStacks(groupId, selectedSeriesOrGroups);
    }
    else {
      clearGroupSeriesStacks();
    }

    updateMainBalanceStack(budgetAreas);
    updateBudgetAreaSeriesStack(budgetAreas, selectedSeriesOrGroups);
  }

  private Integer getGroupToShow(Set<SeriesOrGroup> seriesOrGroups) {
    Set<SeriesOrGroup> groups = new HashSet<SeriesOrGroup>();
    for (SeriesOrGroup seriesOrGroup : seriesOrGroups) {
      if (seriesOrGroup.isGroup()) {
        groups.add(seriesOrGroup);
      }
      else if (seriesOrGroup.isInGroup(repository)) {
        groups.add(seriesOrGroup.getContainingGroup(repository));
      }
    }
    if (groups.size() == 1) {
      return groups.iterator().next().getId();
    }
    return null;
  }

  private boolean isSingleSeries(Set<SeriesOrGroup> seriesOrGroups) {
    return seriesOrGroups.size() == 1 && seriesOrGroups.iterator().next().isSeries();
  }

  private Set<SeriesOrGroup> getSeriesOrGroups(GlobList wrappers) {
    Set<SeriesOrGroup> result = new HashSet<SeriesOrGroup>();
    for (Glob wrapper : wrappers.filter(or(fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                                           fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES_GROUP.getId())),
                                        repository)) {
      result.add(new SeriesOrGroup(wrapper.get(SeriesWrapper.ITEM_ID), SeriesWrapper.getSeriesType(wrapper)));
    }
    return result;
  }

  private Set<Integer> getIds(GlobList wrappers, SeriesWrapperType type) {
    return wrappers
      .filter(fieldEquals(SeriesWrapper.ITEM_TYPE, type.getId()), repository)
      .getValueSet(SeriesWrapper.ITEM_ID);
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
                                           isRoot(),
                                           not(fieldEquals(SeriesStat.TARGET, Series.UNCATEGORIZED_SERIES_ID))));
    for (SeriesOrGroup seriesOrGroup : SeriesOrGroup.getAllFromSeriesStat(stats)) {
      Double amount = getTotalAmountForSelectedPeriod(seriesOrGroup);
      if (amount != null && amount < 0) {
        if (seriesOrGroup.shouldCreateWrapper(repository)) {
          dataset.add(seriesOrGroup.getName(repository), -amount, seriesOrGroup.getKey());
        }
      }
    }

    rootSeriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("mainAccount");
  }

  private void updateGroupStacks(Integer groupId, Set<SeriesOrGroup> selectedSeriesOrGroups) {
    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();

    GlobList stats = repository.getAll(SeriesStat.TYPE, and(isSeriesInGroup(groupId), fieldIn(SeriesStat.MONTH, selectedMonthIds)));
    for (SeriesOrGroup seriesOrGroup : SeriesOrGroup.getAllFromSeriesStat(stats)) {
      if (seriesOrGroup.shouldCreateWrapper(repository)) {
        double amount = getTotalAmountForSelectedPeriod(seriesOrGroup);
        StackChartDataset targetDataset = amount >= 0 ? incomeDataset : expensesDataset;
        targetDataset.add(seriesOrGroup.getName(repository),
                          Math.abs(amount),
                          seriesOrGroup.getKey(),
                          selectedSeriesOrGroups.contains(seriesOrGroup));
      }
    }

    installDatasets(groupSeriesChart, incomeDataset, expensesDataset);

    Glob group = repository.get(Key.create(SeriesGroup.TYPE, groupId));
    updateGroupLabel(group.get(SeriesGroup.NAME));
  }

  private void clearGroupSeriesStacks() {
    groupSeriesChart.clear();
  }

  private void updateBudgetAreaSeriesStack(Set<BudgetArea> budgetAreas, Set<SeriesOrGroup> selectedSeriesOrGroups) {
    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();

    GlobList stats = repository.getAll(SeriesStat.TYPE, and(isRoot(), fieldIn(SeriesStat.MONTH, selectedMonthIds)));
    for (SeriesOrGroup seriesOrGroup : SeriesOrGroup.getAllFromSeriesStat(stats)) {
      if (budgetAreas.contains(seriesOrGroup.getBudgetArea(repository)) &&
          seriesOrGroup.shouldCreateWrapper(repository)) {
        double amount = getTotalAmountForSelectedPeriod(seriesOrGroup);
        StackChartDataset targetDataset = amount >= 0 ? incomeDataset : expensesDataset;
        targetDataset.add(seriesOrGroup.getName(repository),
                          Math.abs(amount),
                          seriesOrGroup.getKey(),
                          selectedSeriesOrGroups.contains(seriesOrGroup));
      }
    }

    installDatasets(rootSeriesChart, incomeDataset, expensesDataset);
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
                  uncategorized, BudgetArea.UNCATEGORIZED.getKey(), false);
    }

    double categorized = 0.0;
    for (Glob seriesStat : repository.getAll(SeriesStat.TYPE,
                                             and(isSeries(),
                                                 fieldIn(SeriesStat.MONTH, selectedMonthIds),
                                                 fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                                                 not(fieldEquals(SeriesStat.TARGET, Series.UNCATEGORIZED_SERIES_ID))))) {
      if (!Series.isSavingToExternal(SeriesStat.findSeries(seriesStat, repository))) {
        categorized += Math.abs(seriesStat.get(SeriesStat.ACTUAL_AMOUNT, 0.00));
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

    rootSeriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("uncategorized");
  }

  private void updateSavingsStacks() {

    double savingsIn = 0;
    double savingsOut = 0;
    StackChartDataset seriesInDataset = new StackChartDataset();
    StackChartDataset seriesOutDataset = new StackChartDataset();

    GlobList stats = repository.getAll(SeriesStat.TYPE,
                                       fieldEquals(SeriesStat.MONTH, referenceMonthId));

    for (SeriesOrGroup seriesOrGroup : SeriesOrGroup.getAllFromSeriesStat(stats)) {
      if (!seriesOrGroup.isSeries()) {
        continue;
      }
      Glob series = repository.find(seriesOrGroup.getKey());
      if (series == null ||
          !series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId()) ||
          Series.isSavingToExternal(series)) {
        continue;
      }

      Double amount = getTotalAmountForSelectedPeriod(seriesOrGroup);
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

  private double getTotalAmountForSelectedPeriod(SeriesOrGroup seriesOrGroup) {
    if (selectedMonthIds.size() == 1) {
      Integer monthId = selectedMonthIds.first();
      Glob stat = repository.find(seriesOrGroup.createSeriesStatKey(monthId));
      if ((stat != null) && stat.isTrue(SeriesStat.ACTIVE)) {
        return stat.get(SeriesStat.SUMMARY_AMOUNT, 0.00);
      }
      else {
        return 0.00;
      }
    }

    Double amount = null;
    for (Glob stat : seriesOrGroup.getStatsForAllMonths(repository)) {
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
    rootSeriesChart.update(seriesInDataset, seriesOutDataset, balanceStackColors);
    updateSeriesLabel("savingsSeries");
  }

  private void updateSubSeriesStacks(SeriesOrGroup seriesOrGroup, Set<Integer> selectedSubSeriesIds) {

    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();

    double totalIncomeForSubSeries = 0;
    double totalExpensesForSubSeries = 0;
    GlobList subSeriesList = GlobList.EMPTY;
    if (seriesOrGroup.type.equals(SeriesType.SERIES)) {
      subSeriesList = repository.findByIndex(SubSeries.SERIES_INDEX, seriesOrGroup.id);
    }
    if (subSeriesList.isEmpty()) {
      clearSubSeriesStacks();
      return;
    }
    for (Glob subSeries : subSeriesList) {
      Integer subSeriesId = subSeries.get(SubSeries.ID);
      double amount = 0;
      for (Integer monthId : selectedMonthIds) {
        Glob stat = repository.find(SubSeriesStat.createKey(subSeriesId, monthId));
        amount += stat != null ? stat.get(SubSeriesStat.ACTUAL_AMOUNT) : 0;
      }
      if (amount != 0) {
        StackChartDataset targetDataset = amount > 0 ? incomeDataset : expensesDataset;
        targetDataset.add(subSeries.get(SubSeries.NAME),
                          Math.abs(amount),
                          subSeries.getKey(),
                          selectedSubSeriesIds.contains(subSeriesId));
      }
      if (amount > 0) {
        totalIncomeForSubSeries += amount;
      }
      else {
        totalExpensesForSubSeries += amount;
      }
    }

    double totalForSeries = 0;
    for (Integer monthId : selectedMonthIds) {
      Glob stat = repository.find(seriesOrGroup.createSeriesStatKey(monthId));
      Double amount = (stat != null ? stat.get(SeriesStat.ACTUAL_AMOUNT) : new Double(0.00));
      if (amount != null) {
        totalForSeries += amount;
      }
    }

    if (totalForSeries > 0) {
      double remainder = totalForSeries - totalIncomeForSubSeries;
      if (Amounts.isNotZero(remainder)) {
        incomeDataset.add(seriesOrGroup.getName(repository),
                          Math.abs(remainder),
                          null,
                          false);
      }
    }
    else if (totalForSeries < 0) {
      double remainder = totalForSeries - totalExpensesForSubSeries;
      if (Amounts.isNotZero(remainder)) {
        expensesDataset.add(seriesOrGroup.getName(repository),
                            Math.abs(remainder),
                            null,
                            false);
      }
    }

    installDatasets(subSeriesChart, incomeDataset, expensesDataset);

    updateSubSeriesLabel(seriesOrGroup.getName(repository));
  }

  private void clearSubSeriesStacks() {
    subSeriesChart.clear();
  }

  private void updateBalanceLabel(String messageKey, String... args) {
    updateLabel(balanceChartLabel, "chart.balance." + messageKey, args);
  }

  private void updateSeriesLabel(String messageKey, String... args) {
    updateLabel(seriesChartLabel, "chart.series." + messageKey, args);
  }

  private void updateGroupLabel(String groupName) {
    updateLabel(groupChartLabel, "chart.group", Strings.cut(groupName, 30));
  }

  private void updateSubSeriesLabel(String seriesName) {
    updateLabel(subSeriesChartLabel, "chart.subSeries", Strings.cut(seriesName, 30));
  }

  private void updateLabel(JLabel label, String messageKey, String... args) {
    label.setText(Lang.get("seriesAnalysis." + messageKey, args));
  }

  private class StackSelectionListener extends StackChartAdapter {

    private StackChart chart;
    private NavigationPopup popup;

    private StackSelectionListener(StackChart chart, SelectionService parentSelectionService) {
      this.chart = chart;
      this.chart.addListener(this);
      this.popup = new NavigationPopup(chart, repository, directory, parentSelectionService);
    }

    public void processClick(Key selectedKey, boolean forceExpandSelection) {
      if (selectedKey == null) {
        return;
      }

      Glob glob = repository.find(selectedKey);
      if (Transaction.TYPE.equals(selectedKey.getGlobType())) {
        selectTransactions(glob);
        return;
      }

      updateSelection(glob, forceExpandSelection);
    }

    public void processRightClick(Key selectedKey, boolean forceExpandSelection) {
      if (selectedKey == null) {
        return;
      }

      Glob glob = repository.find(selectedKey);
      if (Transaction.TYPE.equals(selectedKey.getGlobType())) {
        popup.show(selectedMonthIds, Collections.singleton(selectedKey));
        return;
      }

      Set<Glob> wrappers = updateSelection(glob, forceExpandSelection);

      GlobList sameTypeWrappers = new GlobList();
      for (Glob wrappedGlob : SeriesWrapper.getWrappedGlobs(wrappers, repository)) {
        if (glob.getType().equals(wrappedGlob.getType())) {
          sameTypeWrappers.add(wrappedGlob);
        }
      }
      popup.show(selectedMonthIds, sameTypeWrappers.getKeySet());
    }

    private Set<Glob> updateSelection(Glob selectedGlob, boolean forceExpandSelection) {

      Glob selectedWrapper = SeriesWrapper.getWrapper(selectedGlob, repository);

      Set<Glob> wrappers = new HashSet<Glob>();
      wrappers.add(selectedWrapper);
      GlobList selection = selectionService.getSelection(SeriesWrapper.TYPE);
      if (forceExpandSelection || selection.contains(selectedWrapper)) {
        wrappers.addAll(selection);
      }

      selectionService.select(wrappers, SeriesWrapper.TYPE);

      return wrappers;
    }

    private void selectTransactions(Glob transaction) {
      directory.get(NavigationService.class).gotoCategorization(new GlobList(transaction), true);
    }
  }

  private void installDatasets(StackChart chart, StackChartDataset incomeDataset, StackChartDataset expensesDataset) {
    if (!incomeDataset.isEmpty() && !expensesDataset.isEmpty()) {
      chart.update(incomeDataset, expensesDataset, balanceStackColors);
    }
    else if (!incomeDataset.isEmpty()) {
      chart.update(incomeDataset, incomeStackColors);
    }
    else if (!expensesDataset.isEmpty()) {
      chart.update(expensesDataset, expensesStackColors);
    }
    else {
      chart.clear();
    }
  }
}
