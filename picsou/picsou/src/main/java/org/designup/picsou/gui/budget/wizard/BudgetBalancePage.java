package org.designup.picsou.gui.budget.wizard;

import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.wizard.AbstractWizardPage;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.util.Amounts;
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
  private JLabel balanceAmount = new JLabel();

  private JLabel beginOfMonthLabel = new JLabel();
  private JLabel beginOfMonthAmount = new JLabel();
  private JLabel shiftAmount = new JLabel();
  private JLabel shiftLabel = new JLabel();
  private JLabel balanceLabelExplain = new JLabel();
  private JLabel balanceAmountExplain = new JLabel();
  private JLabel endOfMonthAmount = new JLabel();
  private JLabel endOfMonthLabel = new JLabel();
  private JEditorPane balanceExplain = new JEditorPane();

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

  public JComponent getPanel() {
    return panel;
  }

  public void init() {
    createPanel();
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

  public void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/budget/budgetWizard/budgetBalancePage.splits", repository, directory);

    builder.add("balanceChart", balanceChart);

    balanceDescription = GuiUtils.createReadOnlyHtmlComponent();
    builder.add("balanceDescription", balanceDescription);

    builder.add("balanceAmount", balanceAmount);

    builder.add("balanceExplain", balanceExplain);

    builder.add("beginOfMonthAmount", beginOfMonthAmount);
    builder.add("beginOfMonthLabel", beginOfMonthLabel);
    builder.add("shiftLabel", shiftLabel);
    builder.add("shiftAmount", shiftAmount);
    builder.add("balanceLabelExplain", balanceLabelExplain);
    builder.add("balanceAmountExplain", balanceAmountExplain);
    builder.add("endOfMonthAmount", endOfMonthAmount);
    builder.add("endOfMonthLabel", endOfMonthLabel);

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

    double balance = budgetStats.getSum(BudgetStat.MONTH_BALANCE);
    budgetStats.sort(BudgetStat.MONTH);
    Glob firstBudgetStat = budgetStats.getFirst();
    Double beginOfMonthPosition = firstBudgetStat.get(BudgetStat.BEGIN_OF_MONTH_ACCOUNT_POSITION);
    Glob lastBudgetStat = budgetStats.getLast();
    Double endOfMonthPosition = lastBudgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
    double shift =  endOfMonthPosition - (beginOfMonthPosition + balance);

    boolean hasShift = !Amounts.isNearZero(shift);
    if (hasShift){
      balanceExplain.setText(Lang.get("budgetBalancePage.explain.shift"));
      shiftAmount.setText(Formatting.toStringWithPlus(shift));
    }else {
      balanceExplain.setText(Lang.get("budgetBalancePage.explain.noShift"));
    }
    shiftLabel.setVisible(hasShift);
    shiftAmount.setVisible(hasShift);

    beginOfMonthLabel.setText(Lang.get("budgetBalancePage.beginOfMonth",
                                       getMonthAndYear(Month.previous(firstBudgetStat.get(BudgetStat.MONTH)))));
    beginOfMonthAmount.setText(Formatting.toString(beginOfMonthPosition));
    balanceLabelExplain.setText(Lang.get("budgetBalancePage.balanceLabelExplain",
                                  budgetStats.size() == 1 ?
                                  getMonthAndYear(firstBudgetStat.get(BudgetStat.MONTH)) :
                                  ""));
    balanceAmount.setText(Formatting.toStringWithPlus(balance));
    balanceAmountExplain.setText(Formatting.toStringWithPlus(balance));
    endOfMonthLabel.setText(Lang.get("budgetBalancePage.endOfMonth",
                                     getMonthAndYear(lastBudgetStat.get(BudgetStat.MONTH))));
    endOfMonthAmount.setText(Formatting.toString(endOfMonthPosition));

  }

  private String getMonthAndYear(final Integer monthId) {
    return Month.getFullMonthLabelWith4DigitYear(monthId).toLowerCase();
  }


  private Glob getLastBudgetStat(GlobList list) {
    list.sort(BudgetStat.MONTH);
    return list.getLast();
  }
}