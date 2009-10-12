package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class BudgetSummaryDetailsDialog {
  private JLabel amountSummaryLabel;
  private JTextArea positionDescription;
  private StackChart balanceChart;
  private StackChartColors balanceChartColors;
  private Directory directory;
  private GlobRepository repository;
  private PicsouDialog dialog;
  private CardHandler positionCard;
  private JLabel title;

  public BudgetSummaryDetailsDialog(GlobRepository repository, Directory parentDirectory) {
    this.repository = repository;
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

    createDialog();
    registerBalanceChartUpdater();
  }

  public void show(GlobList selectedMonths) {

    Integer maxMonthId = selectedMonths.getLast().get(Month.ID);
    if (maxMonthId >= CurrentMonth.getLastTransactionMonth(repository)) {
      showEstimatedPositionDetails();
    }
    else {
      showActualPositionDetails();
    }

    selectStats(selectedMonths);

    updateTitle(selectedMonths);

    dialog.showCentered();
  }

  private void updateTitle(GlobList months) {
    title.setText(Lang.get("budgetSummaryDetails.title", 
                           MonthListStringifier.toString(months.getValueSet(Month.ID)).toLowerCase()));
  }

  private void selectStats(GlobList selectedMonths) {
    GlobList stats = new GlobList();
    for (Glob month : selectedMonths) {
      stats.add(repository.find(Key.create(BudgetStat.TYPE, month.get(Month.ID))));
    }
    directory.get(SelectionService.class).select(stats, BudgetStat.TYPE);
  }

  private void showEstimatedPositionDetails() {
    positionCard.show("estimated");
    positionDescription.setText(Lang.get("budgetSummaryDetails.position.description.estimated"));
  }

  private void showActualPositionDetails() {
    positionCard.show("actual");
    positionDescription.setText(Lang.get("budgetSummaryDetails.position.description.actual"));
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void createDialog() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/budgetSummaryDetailsDialog.splits", repository, directory);

    title = builder.add("title", new JLabel()).getComponent();

    builder.add("balanceChart", balanceChart);
    builder.addLabel("balanceLabel", BudgetStat.TYPE, new BalanceStringifier()).getComponent();

    positionCard = builder.addCardHandler("cards");

    positionDescription = builder.add("positionDescription", new JTextArea()).getComponent();

    builder.addLabel("estimatedPosition", BudgetStat.TYPE, new EspectedPositionStringifier()).getComponent();
    builder.addLabel("estimatedPositionDate", BudgetStat.TYPE, new PositionDateStringifier()).getComponent();
    builder.addLabel("initialPosition", BudgetStat.TYPE, new InitialPositionStringifier()).getComponent();
    addLabel(builder, "remainingIncome", BudgetStat.INCOME_REMAINING, false);
    addLabel(builder, "remainingFixed", BudgetStat.RECURRING_REMAINING, true);
    addLabel(builder, "remainingEnvelope", BudgetStat.ENVELOPES_REMAINING, true);
    addLabel(builder, "remainingInSavings", BudgetStat.SAVINGS_IN_REMAINING, false);
    addLabel(builder, "remainingOutSavings", BudgetStat.SAVINGS_OUT_REMAINING, true);
    addLabel(builder, "remainingSpecial", BudgetStat.SPECIAL_REMAINING, true);

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(directory.get(JFrame.class), true, directory);
    dialog.setPanelAndButton(panel, new CloseAction(dialog));
    dialog.pack();
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

  private void addLabel(GlobsPanelBuilder builder, String name, DoubleField field, boolean invert) {
    builder.addLabel(name, BudgetStat.TYPE, GlobListStringifiers.sum(field, Formatting.DECIMAL_FORMAT, invert));
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
      return Formatting.toStringWithPlus(total);
    }
  }

  private class EspectedPositionStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob budgetStat = getLastBudgetStat(list);
      if (budgetStat == null) {
        return "";
      }
      return Formatting.toString(budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION));
    }
  }

  private class PositionDateStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob budgetStat = getLastBudgetStat(list);
      if (budgetStat == null) {
        return "";
      }
      final String date = Formatting.toString(Month.getLastDay(budgetStat.get(BudgetStat.MONTH)));
      return Lang.get("budgetSummaryDetails.position.date", date);
    }
  }

  private class InitialPositionStringifier implements GlobListStringifier {
    public String toString(GlobList budgetStats, GlobRepository repository) {
      budgetStats.sort(BudgetStat.MONTH);
      if (budgetStats.isEmpty()) {
        return "";
      }

      Integer currentMonthId = CurrentMonth.getLastTransactionMonth(repository);
      Glob budgetStat = null;
      for (Glob stat : budgetStats) {
        budgetStat = stat;
        if (budgetStat.get(BudgetStat.MONTH) >= currentMonthId) {
          break;
        }
      }
      if (budgetStat == null) {
        return "";
      }

      Double amount = budgetStat.get(BudgetStat.LAST_KNOWN_ACCOUNT_POSITION);
      if (amount == null) {
        amount = budgetStat.get(BudgetStat.BEGIN_OF_MONTH_ACCOUNT_POSITION);
      }
      return Formatting.toString(amount);
    }
  }
}