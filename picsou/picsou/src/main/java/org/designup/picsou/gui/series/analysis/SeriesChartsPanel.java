package org.designup.picsou.gui.series.analysis;

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
import org.designup.picsou.gui.series.analysis.components.StackToggleController;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
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
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

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
  private StackChart seriesChart;
  private StackChart subSeriesChart;

  private JLabel balanceChartLabel;
  private JLabel seriesChartLabel;
  private JLabel subSeriesChartLabel;

  private StackChartColors balanceStackColors;
  private StackChartColors incomeStackColors;
  private StackChartColors expensesStackColors;
  private SelectionService selectionService;

  private static final GlobType[] USED_TYPES =
    new GlobType[]{BudgetStat.TYPE, Series.TYPE, SubSeries.TYPE,
                   SavingsBudgetStat.TYPE, PeriodSeriesStat.TYPE, SeriesStat.TYPE, SubSeriesStat.TYPE};
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

    histoChartBuilder = new HistoChartBuilder(new HistoChartConfig(true, true, false, true, true, false, false, true, true, false),
                                              new HistoChartColors(directory), range, repository, directory, parentSelectionService);
    histoChartBuilder.addListener(new HistoChartListenerAdapter() {
      public void scroll(int count) {
        updateCharts(false);
      }
    });
    balanceChart = new StackChart();
    seriesChart = new StackChart();
    subSeriesChart = new StackChart();

    balanceStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                           "stack.expenses.bar", "stack.expenses.border", directory);
    incomeStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                          "stack.income.bar", "stack.income.border", directory);
    expensesStackColors = createStackColors("stack.expenses.bar", "stack.expenses.bar",
                                            "stack.expenses.bar", "stack.expenses.bar", directory);

    repository.addChangeListener(new TypeChangeSetListener(USED_TYPES) {
      protected void update(GlobRepository repository) {
        updateCharts(true);
      }
    });

    new StackSelectionListener(balanceChart, parentSelectionService);
    new StackSelectionListener(seriesChart, parentSelectionService);
    new StackSelectionListener(subSeriesChart, parentSelectionService);

    stackToggle = new StackToggleController(balanceChart, subSeriesChart);
  }

  public void reset() {
    setMainSummaryWrapperKey();
    stackToggle.showBudgetStack();
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
    builder.add("subSeriesChart", subSeriesChart);

    builder.add("histoChartLabel", histoChartBuilder.getLabel());
    builder.add("histoChartLegend", histoChartBuilder.getLegend());
    balanceChartLabel = builder.add("balanceChartLabel", new JLabel()).getComponent();
    seriesChartLabel = builder.add("seriesChartLabel", new JLabel()).getComponent();
    subSeriesChartLabel = builder.add("subSeriesChartLabel", new JLabel()).getComponent();

    builder.add("gotoBudgetButton", stackToggle.getGotoBudgetButton());
    builder.add("gotoSubSeriesButton", stackToggle.getGotoSubSeriesButton());
  }

  public void monthSelected(Integer monthId, SortedSet<Integer> monthIds) {
    this.referenceMonthId = monthId;
    this.selectedMonthIds = monthIds;
    updateCharts(true);
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(SeriesWrapper.TYPE)) {
      GlobList wrappers = selection.getAll(SeriesWrapper.TYPE);
      if (wrappers.isEmpty()) {
        setMainSummaryWrapperKey();
      }
      else {
        selectedWrapperKeys.clear();
        selectedWrapperKeys.addAll(wrappers.getKeyList());
        if (containsSubSeries(wrappers)) {
          stackToggle.showSubSeriesStack();
        }
        else {
          stackToggle.setSubSeriesPresent(containsSeriesWithSubSeries(wrappers));
        }
      }
    }

    updateCharts(true);
  }

  private boolean containsSubSeries(GlobList selectedWrappers) {
    for (Glob wrapper : selectedWrappers) {
      if (SeriesWrapper.isSubSeries(wrapper)) {
        return true;
      }
    }
    return false;
  }

  private boolean containsSeriesWithSubSeries(GlobList selectedWrappers) {
    for (Glob wrapper : selectedWrappers) {
      if (SeriesWrapper.isSeries(wrapper)) {
        Glob series = SeriesWrapper.getSeries(wrapper, repository);
        if (repository.contains(SubSeries.TYPE, GlobMatchers.linkedTo(series, SubSeries.SERIES))) {
          return true;
        }
      }
    }
    return false;
  }

  private void setMainSummaryWrapperKey() {
    selectedWrapperKeys.clear();
    selectedWrapperKeys.add(Key.create(SeriesWrapper.TYPE, SeriesWrapper.BALANCE_SUMMARY_ID));
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

    Set<SeriesOrGroup> seriesOrGroups = getSeriesOrGroups(selectedWrappers);
    Set<Integer> subSeriesIds = getIds(selectedWrappers, SeriesWrapperType.SUB_SERIES);
    for (Integer subSeriesId : subSeriesIds) {
      Glob subSeries = repository.get(Key.create(SubSeries.TYPE, subSeriesId));
      seriesOrGroups.add(new SeriesOrGroup(subSeries.get(SubSeries.SERIES), SeriesType.SERIES));
    }
    for (SeriesOrGroup seriesOrGroup : seriesOrGroups) {
      budgetAreas.add(seriesOrGroup.getBudgetArea(repository));
    }

    if (seriesOrGroups.isEmpty()) {
      histoChartBuilder.showBudgetAreaHisto(budgetAreas, referenceMonthId, resetPosition);
    }
    else if (!subSeriesIds.isEmpty()) {
      histoChartBuilder.showSubSeriesHisto(subSeriesIds, referenceMonthId, resetPosition);
    }
    else {
      histoChartBuilder.showSeriesHisto(seriesOrGroups, referenceMonthId, resetPosition);
    }

    if ((seriesOrGroups.size() == 1)) {
      updateSubSeriesStacks(seriesOrGroups.iterator().next(), subSeriesIds);
    }
    else {
      clearSubSeriesStacks();
    }

    updateMainBalanceStack(budgetAreas);
    updateBudgetAreaSeriesStack(budgetAreas, seriesOrGroups);
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

    seriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("mainAccount");
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

    installDatasets(seriesChart, incomeDataset, expensesDataset);
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

    seriesChart.update(dataset, expensesStackColors);
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
    seriesChart.update(seriesInDataset, seriesOutDataset, balanceStackColors);
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
      stackToggle.setSubSeriesPresent(false);
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

//  Glob series = repository.get(Key.create(Series.TYPE, seriesId));
//  if (SeriesWrapper.shouldCreateWrapperForSeries(series, repository)) {
//    if (budgetAreas.contains(BudgetArea.get(series.get(Series.BUDGET_AREA)))
//        && SeriesWrapper.shouldCreateWrapperForSeries(series, repository)) {

}
