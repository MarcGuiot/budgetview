package org.designup.picsou.gui.series.evolution;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilderConfig;
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
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesEvolutionChartPanel implements GlobSelectionListener {

  private GlobRepository repository;
  private Directory directory;

  private Integer selectedMonthId;
  private Key currentWrapperKey;

  private HistoChartBuilder histoChartBuilder;

  private StackChart balanceChart;
  private StackChart seriesChart;

  private JLabel balanceChartLabel;
  private JLabel seriesChartLabel;

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

    histoChartBuilder = new HistoChartBuilder(new HistoChartBuilderConfig(true, true, false, true, 12, 6, false),
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
            || changeSet.containsChanges(SavingsBudgetStat.TYPE)
            || changeSet.containsChanges(SeriesStat.TYPE)) {
          update(true);
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        update(true);
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
    builder.add("histoChart", histoChartBuilder.getChart());
    builder.add("balanceChart", balanceChart);
    builder.add("seriesChart", seriesChart);

    builder.add("histoChartLabel", histoChartBuilder.getLabel()).getComponent();
    balanceChartLabel = builder.add("balanceChartLabel", new JLabel()).getComponent();
    seriesChartLabel = builder.add("seriesChartLabel", new JLabel()).getComponent();
  }

  public void monthSelected(Integer monthId) {
    this.selectedMonthId = monthId;
    update(true);
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

    update(true);
  }

  private Key getMainSummaryWrapper() {
    return Key.create(SeriesWrapper.TYPE, SeriesWrapper.BALANCE_SUMMARY_ID);
  }

  private void update(final boolean resetPosition) {
    Glob currentWrapper = null;
    if (currentWrapperKey != null) {
      currentWrapper = repository.find(currentWrapperKey);
    }
    if ((selectedMonthId == null) || (currentWrapper == null) || repository.find(CurrentMonth.KEY) == null) {
      histoChartBuilder.clear();
      return;
    }
    switch (SeriesWrapperType.get(currentWrapper)) {
      case BUDGET_AREA: {
        BudgetArea budgetArea = BudgetArea.get(currentWrapper.get(SeriesWrapper.ITEM_ID));
        if (budgetArea.equals(BudgetArea.UNCATEGORIZED)) {
          histoChartBuilder.showUncategorizedHisto(selectedMonthId, resetPosition);
          updateUncategorizedBalanceStack();
          updateUncategorizedSeriesStack();
        }
        else {
          histoChartBuilder.showBudgetAreaHisto(budgetArea, selectedMonthId, resetPosition);
          updateMainBalanceStack(budgetArea);
          updateBudgetAreaSeriesStack(budgetArea, null);
        }
      }
      break;

      case SERIES: {
        Integer seriesId = currentWrapper.get(SeriesWrapper.ITEM_ID);
        BudgetArea budgetArea = Series.getBudgetArea(seriesId, repository);
        histoChartBuilder.showSeriesHisto(seriesId, selectedMonthId, resetPosition);
        updateMainBalanceStack(budgetArea);
        updateBudgetAreaSeriesStack(budgetArea, seriesId);
      }
      break;

      case SUMMARY: {
        Integer id = currentWrapper.get(SeriesWrapper.ID);
        if (id.equals(SeriesWrapper.BALANCE_SUMMARY_ID)) {
          histoChartBuilder.showMainBalanceHisto(selectedMonthId, resetPosition);
          updateMainBalanceStack(null);
          updateMainAccountExpensesSeriesStack();
        }
        else if (id.equals(SeriesWrapper.MAIN_POSITION_SUMMARY_ID)) {
          histoChartBuilder.showMainAccountsHisto(selectedMonthId, resetPosition);
          updateMainBalanceStack(null);
          updateMainAccountExpensesSeriesStack();
        }
        else if (id.equals(SeriesWrapper.SAVINGS_POSITION_SUMMARY_ID)) {
          histoChartBuilder.showSavingsAccountsHisto(selectedMonthId, resetPosition);
          updateSavingsStacks();
        }
      }
      break;

      default:
        throw new InvalidParameter("Unexpected case: " + currentWrapper);
    }
  }

  private void updateMainBalanceStack(BudgetArea selectedBudgetArea) {

    Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, selectedMonthId));
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
                                       and(fieldEquals(SeriesStat.MONTH, selectedMonthId),
                                           not(fieldEquals(SeriesStat.SERIES, Series.UNCATEGORIZED_SERIES_ID))))) {
      Integer seriesId = stat.getKey().get(SeriesStat.SERIES);
      Glob series = repository.get(Key.create(Series.TYPE, seriesId));
      Double amount = stat.get(SeriesStat.SUMMARY_AMOUNT);
      if (amount != null && amount < 0) {
        dataset.add(series.get(Series.NAME), -amount, createSelectionAction(seriesId));
      }
    }

    seriesChart.update(dataset, expensesStackColors);
    updateSeriesLabel("mainAccount");
  }

  private void updateBudgetAreaSeriesStack(BudgetArea budgetArea, Integer selectedSeriesId) {
    StackChartDataset dataset = new StackChartDataset();

    for (Glob stat : repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, selectedMonthId))) {
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

  private void updateUncategorizedBalanceStack() {

    Glob budgetStat = repository.find(Key.create(BudgetStat.TYPE, selectedMonthId));
    double uncategorized = budgetStat != null ? budgetStat.get(BudgetStat.UNCATEGORIZED_ABS) : 0;

    StackChartDataset dataset = new StackChartDataset();
    if (uncategorized > 0.01) {
      dataset.add(Lang.get("seriesEvolution.chart.balance.uncategorized.tocategorize"),
                  uncategorized, null, false);
    }

    double categorized = 0.0;
    for (Glob seriesStat : repository.getAll(SeriesStat.TYPE,
                                             and(fieldEquals(SeriesStat.MONTH, selectedMonthId),
                                                 not(fieldEquals(SeriesStat.SERIES, Series.UNCATEGORIZED_SERIES_ID))))) {
      categorized += Math.abs(Utils.zeroIfNull(seriesStat.get(SeriesStat.AMOUNT)));
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
                            fieldEquals(Transaction.BUDGET_MONTH, selectedMonthId)));

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

  private void updateSavingsStacks() {

    double savingsIn = 0;
    double savingsOut = 0;
    StackChartDataset seriesInDataset = new StackChartDataset();
    StackChartDataset seriesOutDataset = new StackChartDataset();

    for (Glob seriesStat : repository.getAll(SeriesStat.TYPE, fieldEquals(SeriesStat.MONTH, selectedMonthId))) {
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
    Glob budgetStat = SavingsBudgetStat.findSummary(selectedMonthId, repository);

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

  private void updateBalanceLabel(String messageKey, String... args) {
    updateLabel(balanceChartLabel, "chart.balance." + messageKey, args);
  }

  private void updateSeriesLabel(String messageKey, String... args) {
    updateLabel(seriesChartLabel, "chart.series." + messageKey, args);
  }

  private void updateLabel(JLabel label, String messageKey, String... args) {
    label.setText(Lang.get("seriesEvolution." + messageKey, args));
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

  private class CategorizeTransactionAction extends AbstractAction {
    private Key transactionKey;

    public CategorizeTransactionAction(Key transactionKey) {
      this.transactionKey = transactionKey;
    }

    public void actionPerformed(ActionEvent e) {
      Glob transaction = repository.find(transactionKey);
      if (transaction != null) {
        directory.get(NavigationService.class).gotoCategorization(new GlobList(transaction), true);
      }
    }
  }
}
