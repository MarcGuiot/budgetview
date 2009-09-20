package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.painters.*;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobPrinter;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TablePrinter;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SeriesEvolutionChartPanel implements GlobSelectionListener {

  private GlobRepository repository;
  private Directory directory;

  private Integer currentMonthId;
  private Key currentWrapperKey;

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
    this.directory = directory;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, SeriesWrapper.TYPE);

    this.currentWrapperKey = getMainSummaryWrapper();

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

    balanceStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                           "stack.expenses.bar", "stack.expenses.border", directory);
    incomeStackColors = createStackColors("stack.income.bar", "stack.income.border",
                                          "stack.income.bar", "stack.income.border", directory);
    expensesStackColors = createStackColors("stack.expenses.bar", "stack.expenses.bar",
                                            "stack.expenses.bar", "stack.expenses.bar", directory);

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(BudgetStat.TYPE)
            || changeSet.containsChanges(SavingsBudgetStat.TYPE)
            || changeSet.containsChanges(SeriesStat.TYPE)) {
          update();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update();
      }
    });
  }

  public void reset() {
    this.currentWrapperKey = getMainSummaryWrapper();
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
    builder.add("histoChart", histoChart);
    builder.add("balanceChart", balanceChart);
    builder.add("seriesChart", seriesChart);

    histoChartLabel = builder.add("histoChartLabel", new JLabel()).getComponent();
    balanceChartLabel = builder.add("balanceChartLabel", new JLabel()).getComponent();
    seriesChartLabel = builder.add("seriesChartLabel", new JLabel()).getComponent();
  }

  public void monthSelected(Integer monthId) {
    this.currentMonthId = monthId;
    update();
  }

  public void selectionUpdated(GlobSelection selection) {
    if (selection.isRelevantForType(SeriesWrapper.TYPE)) {
      GlobList wrappers = selection.getAll(SeriesWrapper.TYPE);
      if (wrappers.size() == 1) {
        currentWrapperKey = wrappers.getFirst().getKey();
      }
      else if (wrappers.size() == 0) {
        currentWrapperKey = getMainSummaryWrapper();
      }
      else {
        currentWrapperKey = null;
      }
    }

    update();
  }

  private Key getMainSummaryWrapper() {
    return Key.create(SeriesWrapper.TYPE, SeriesWrapper.BALANCE_SUMMARY_ID);
  }

  private void update() {
    Glob currentWrapper = null;
    if (currentWrapperKey != null) {
      currentWrapper = repository.find(currentWrapperKey);
    }
    if ((currentMonthId == null) || (currentWrapper == null) || repository.find(CurrentMonth.KEY) == null) {
      histoChart.clear();
      return;
    }
    switch (SeriesWrapperType.get(currentWrapper)) {
      case BUDGET_AREA: {
        BudgetArea budgetArea = BudgetArea.get(currentWrapper.get(SeriesWrapper.ITEM_ID));
        if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
          updateUncategorizedHisto();
          updateUncategorizedBalanceStack();
          updateUncategorizedSeriesStack();
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
          updateMainAccountExpensesSeriesStack();
        }
        else if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
          updateMainAccountsHisto();
          updateMainBalanceStack(null);
          updateMainAccountExpensesSeriesStack();
        }
        else if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
          updateSavingsAccountsHisto();
          updateSavingsStacks();
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
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double income = budgetStat.get(BudgetStat.INCOME_SUMMARY);
        Double expense = budgetStat.get(BudgetStat.EXPENSE_SUMMARY);
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

    Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, currentMonthId));
    if (budgetStat == null) {
      balanceChart.clear();
      return;
    }

    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();
    for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
      Double amount = budgetStat.get(BudgetStat.getSummary(budgetArea));
      StackChartDataset dataset = amount > 0 ? incomeDataset : expensesDataset;
      dataset.add(budgetArea.getLabel(),
                  Math.abs(amount),
                  createSelectionAction(budgetArea),
                  budgetArea.equals(selectedBudgetArea));
    }

    balanceChart.update(incomeDataset, expensesDataset, balanceStackColors);
    updateBalanceLabel("mainBalance");
  }

  private void updateMainAccountExpensesSeriesStack() {
    StackChartDataset dataset = new StackChartDataset();

    for (Glob stat : repository.getAll(SeriesStat.TYPE,
                                       and(fieldEquals(SeriesStat.MONTH, currentMonthId),
                                           not(fieldEquals(SeriesStat.SERIES, Series.UNCATEGORIZED_SERIES_ID))))) {
      Integer seriesId = stat.getKey().get(SeriesStat.SERIES);
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      Double amount = stat.get(SeriesStat.SUMMARY_AMOUNT);
      if (amount < 0) {
        dataset.add(series.get(Series.NAME), -amount, createSelectionAction(seriesId));
      }
    }

    seriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("mainAccount");
  }

  private void updateBudgetAreaHisto(BudgetArea budgetArea) {
    HistoDatasetBuilder dataset = new HistoDatasetBuilder();

    DoubleField plannedField = BudgetStat.getPlanned(budgetArea);
    DoubleField actualField = BudgetStat.getObserved(budgetArea);
    dataset.setInverted(!budgetArea.isIncome());
    dataset.setActualHiddenInTheFuture();

    for (int monthId : getMonthIds()) {
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (budgetStat != null) {
        Double planned = budgetStat.get(plannedField);
        Double actual = budgetStat.get(actualField);
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

    for (Glob stat : repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, currentMonthId))) {
      Integer seriesId = stat.getKey().get(SeriesStat.SERIES);
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      if (budgetArea.getId().equals(series.get(Series.BUDGET_AREA))) {
        Double amount = stat.get(SeriesStat.SUMMARY_AMOUNT);
        dataset.add(series.get(Series.NAME),
                    Math.abs(amount != null ? amount : 0.0),
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
      Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      double value = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId), isCurrentMonth(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, uncategorizedColors));
    updateHistoLabel("uncategorized");
  }

  private void updateUncategorizedBalanceStack() {

    Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, currentMonthId));
    double uncategorized = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0;

    StackChartDataset dataset = new StackChartDataset();
    if (uncategorized > 0.01) {
      dataset.add(Lang.get("seriesEvolution.chart.balance.uncategorized.tocategorize"),
                  uncategorized, null, false);
    }

    double categorized = 0.0;
    for (Glob seriesStat : repository.getAll(SeriesStat.TYPE,
                                             and(fieldEquals(SeriesStat.MONTH, currentMonthId),
                                                 not(fieldEquals(SeriesStat.SERIES, Series.UNCATEGORIZED_SERIES_ID))))) {
      categorized += Math.abs(seriesStat.get(SeriesStat.AMOUNT));
    }
    dataset.add(Lang.get("seriesEvolution.chart.balance.uncategorized.categorized"),
                categorized, null, false);

    balanceChart.update(dataset, expensesStackColors);
    updateBalanceLabel("uncategorized");
  }

  private void updateUncategorizedSeriesStack() {

    GlobList uncategorizedTransactions =
      repository.getAll(Transaction.TYPE,
                        and(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                            fieldEquals(Transaction.MONTH, currentMonthId)));

    StackChartDataset dataset = new StackChartDataset();
    for (Glob transaction : uncategorizedTransactions) {
      dataset.add(Strings.cut(transaction.get(Transaction.LABEL), 20),
                  Math.abs(transaction.get(Transaction.AMOUNT)),
                  new CategorizeTransactionAction(transaction.getKey()),
                  false);
    }

    seriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("uncategorized");
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
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      Double value = stat != null ? stat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId), isCurrentMonth(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
    updateHistoLabel("mainAccounts");
  }

  private boolean isCurrentMonth(int monthId) {
    return monthId == currentMonthId;
  }

  private void updateSavingsAccountsHisto() {
    HistoLineDataset dataset = new HistoLineDataset();

    for (int monthId : getMonthIds()) {
      Glob stat = SavingsBudgetStat.findSummary(monthId, repository);
      Double value = stat != null ? stat.get(SavingsBudgetStat.END_OF_MONTH_POSITION) : 0.0;
      dataset.add(monthId, value, getLabel(monthId), getSection(monthId), isCurrentMonth(monthId));
    }

    histoChart.update(new HistoLinePainter(dataset, accountColors));
    updateHistoLabel("savingsAccounts");
  }

  private void updateSavingsStacks() {

    double savingsIn = 0;
    double savingsOut = 0;
    StackChartDataset seriesInDataset = new StackChartDataset();
    StackChartDataset seriesOutDataset = new StackChartDataset();

    for (Glob seriesStat : repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, currentMonthId))) {
      Double amount = seriesStat.get(SeriesStat.SUMMARY_AMOUNT);
      if (amount == null) {
        continue;
      }

      Glob series = repository.findLinkTarget(seriesStat, SeriesStat.SERIES);
      if ((series == null) || series.isTrue(Series.IS_MIRROR)) {
        continue;
      }

      Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
      Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);

      boolean isFromSavingsAccount = Account.isUserCreatedSavingsAccount(fromAccount);
      boolean isToSavingsAccount = Account.isUserCreatedSavingsAccount(toAccount);

      String in = "";
      String out = "";
      String branch = "";

      boolean isFromImported = GlobUtils.safeIsTrue(fromAccount, Account.IS_IMPORTED_ACCOUNT);
      boolean isToImported = GlobUtils.safeIsTrue(toAccount, Account.IS_IMPORTED_ACCOUNT);

      if (isFromImported && isToImported) {

        if (isFromSavingsAccount && !isToSavingsAccount) {
          if (amount >= 0) {
            savingsOut += amount;
            seriesOutDataset.add(series.get(Series.NAME), amount, createSelectionAction(series.get(Series.ID)));
          }
          else {
            double absAmount = -amount;
            savingsOut += absAmount;
            seriesOutDataset.add(series.get(Series.NAME), absAmount, createSelectionAction(series.get(Series.ID)));
          }
        }
        else if (!isFromSavingsAccount && isToSavingsAccount) {
          if (amount >= 0) {
            savingsIn += amount;
            seriesInDataset.add(series.get(Series.NAME), amount, createSelectionAction(series.get(Series.ID)));
          }
          else {
            double absAmount = -amount;
            savingsOut += absAmount;
            seriesOutDataset.add(series.get(Series.NAME), absAmount, createSelectionAction(series.get(Series.ID)));
          }
        }
      }
      else if (isFromImported && !isToImported) {
        if (isFromSavingsAccount && !isToSavingsAccount) {
          double absAmount = Math.abs(amount);
          savingsOut += absAmount;
          seriesOutDataset.add(series.get(Series.NAME), absAmount, createSelectionAction(series.get(Series.ID)));
        }
        else if (!isFromSavingsAccount && isToSavingsAccount) {
          double absAmount = Math.abs(amount);
          savingsIn += absAmount;
          seriesInDataset.add(series.get(Series.NAME), absAmount, createSelectionAction(series.get(Series.ID)));
        }
      }
      else if (!isFromImported && isToImported) {
        if (isFromSavingsAccount && !isToSavingsAccount) {
          double absAmount = Math.abs(amount);
          savingsOut += absAmount;
          seriesOutDataset.add(series.get(Series.NAME), absAmount, createSelectionAction(series.get(Series.ID)));
        }
        else if (!isFromSavingsAccount && isToSavingsAccount) {
          double absAmount = Math.abs(amount);
          savingsIn += absAmount;
          seriesInDataset.add(series.get(Series.NAME), absAmount, createSelectionAction(series.get(Series.ID)));
        }
      }
    }

    updateSavingsBalanceStack(savingsIn, savingsOut);
    updateSavingsSeriesStack(seriesInDataset, seriesOutDataset);
  }

  private void updateSavingsBalanceStack(double savingsIn, double savingsOut) {
    Glob budgetStat = SavingsBudgetStat.findSummary(currentMonthId, repository);

    StackChartDataset incomeDataset = new StackChartDataset();
    incomeDataset.add(Lang.get("seriesEvolution.chart.balance.savingsIn"), savingsIn, null);

    StackChartDataset expensesDataset = new StackChartDataset();
    expensesDataset.add(Lang.get("seriesEvolution.chart.balance.savingsOut"), savingsOut, null);

    balanceChart.update(incomeDataset, expensesDataset, balanceStackColors);
    updateBalanceLabel("savingsBalance");
  }

  private void updateSavingsSeriesStack(StackChartDataset seriesInDataset, StackChartDataset seriesOutDataset) {
    seriesChart.update(seriesInDataset, seriesOutDataset, balanceStackColors);
    updateSeriesLabel("savingsSeries");
  }

  private void clearBalanceStack() {
    balanceChart.clear();
    balanceChartLabel.setText(null);
  }

  private void clearSeriesStack() {
    seriesChart.clear();
    balanceChartLabel.setText(null);
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
                  isCurrentMonth(monthId),
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

  private class CategorizeTransactionAction extends AbstractAction {
    private Key transactionKey;

    public CategorizeTransactionAction(Key transactionKey) {
      this.transactionKey = transactionKey;
    }

    public void actionPerformed(ActionEvent e) {
      Glob transaction = repository.find(transactionKey);
      System.out.println("SeriesEvolutionChartPanel$CategorizeTransactionAction.actionPerformed " + transaction);
      if (transaction != null) {
        directory.get(NavigationService.class).gotoCategorization(new GlobList(transaction), true);
      }
    }
  }
}
