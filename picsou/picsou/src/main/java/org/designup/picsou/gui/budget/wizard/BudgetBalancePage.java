package org.designup.picsou.gui.budget.wizard;

import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.wizard.AbstractWizardPage;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class BudgetBalancePage extends AbstractWizardPage {

  private StackChart balanceChart;
  private StackChartColors balanceChartColors;
  private JEditorPane balanceDescription;

  private Directory directory;
  private GlobRepository repository;
  private JPanel panel;
  private Directory parentDirectory;

  public BudgetBalancePage(GlobRepository repository, Directory parentDirectory) {
    this.repository = repository;
    this.parentDirectory = parentDirectory;
    this.directory = createDirectory(parentDirectory);
    this.balanceChart = new StackChart();

    this.balanceChartColors = new StackChartColors(
      "stack.income.bar",
      "stack.expenses.bar",
      "stack.barText",
      "stack.label",
      "block.inner.bg",
      "stack.floor",
      "stack.selection.border",
      "stack.rollover.text",
      directory
    );
  }

  public String getId() {
    return "balance";
  }

  public String getTitle() {
    return Lang.get("budgetWizard.balance.title");
  }

  public JComponent getPanel() {
    return panel;
  }

  public void init() {
    createDialog();
    registerBalanceChartUpdater();
  }

  public void updateBeforeDisplay() {
    selectStats(getSelectedMonths());
  }

  private GlobList getSelectedMonths() {
    return parentDirectory.get(SelectionService.class).getSelection(Month.TYPE).sort(Month.ID);
  }

  private void selectStats(GlobList selectedMonths) {
    GlobList stats = new GlobList();
    for (Glob month : selectedMonths) {
      stats.add(repository.find(Key.create(BudgetStat.TYPE, month.get(Month.ID))));
    }
    directory.get(SelectionService.class).select(stats, BudgetStat.TYPE);
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void createDialog() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/budgetWizard/budgetBalancePage.splits", repository, directory);

    builder.add("balanceChart", balanceChart);
    builder.addLabel("balanceLabel", BudgetStat.TYPE, new BalanceStringifier()).getComponent();

    balanceDescription = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("balanceDescription", balanceDescription);

    panel = builder.load();
  }

  private void registerBalanceChartUpdater() {
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        updateBalanceChart(selection.getAll(BudgetStat.TYPE));
      }
    }, BudgetStat.TYPE);
  }

  private void updateBalanceChart(GlobList budgetStats) {
    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();
    for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
      Double amount = budgetStats.getSum(BudgetStat.getSummary(budgetArea));
      StackChartDataset dataset = amount > 0 ? incomeDataset : expensesDataset;
      dataset.add(budgetArea.getLabel(), Math.abs(amount), null, false);
    }

    balanceChart.update(incomeDataset, expensesDataset, balanceChartColors);
  }

  private Glob getLastBudgetStat(GlobList list) {
    list.sort(BudgetStat.MONTH);
    return list.getLast();
  }

  private class BalanceStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return "";
      }
      double total = list.getSum(BudgetStat.MONTH_BALANCE);
      updateBalanceDescription(total);
      return Formatting.toStringWithPlus(total);
    }
  }

  private void updateBalanceDescription(double total) {
    String amountString = Formatting.toString(Math.abs(total));
    if (total > 0) {
      balanceDescription.setText(Lang.get("budgetWizard.balance.positive", amountString));
    }
    else if (total < 0) {
      balanceDescription.setText(Lang.get("budgetWizard.balance.negative", amountString));
    }
    else {
      balanceDescription.setText(Lang.get("budgetWizard.balance.zero"));
    }
    GuiUtils.revalidate(balanceDescription);
  }
}