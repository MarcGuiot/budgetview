package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.model.BalanceStat;
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
      stats.add(repository.find(Key.create(BalanceStat.TYPE, month.get(Month.ID))));
    }
    directory.get(SelectionService.class).select(stats, BalanceStat.TYPE);
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

    title = builder.add("title", new JLabel());

    builder.add("balanceChart", balanceChart);
    builder.addLabel("balanceLabel", BalanceStat.TYPE, new BalanceStringifier()).getComponent();

    positionCard = builder.addCardHandler("cards");

    positionDescription = builder.add("positionDescription", new JTextArea());

    builder.addLabel("estimatedPosition", BalanceStat.TYPE, new EspectedPositionStringifier()).getComponent();
    builder.addLabel("estimatedPositionDate", BalanceStat.TYPE, new PositionDateStringifier()).getComponent();
    builder.addLabel("initialPosition", BalanceStat.TYPE, new InitialPositionStringifier()).getComponent();
    addLabel(builder, "remainingIncome", BalanceStat.INCOME_REMAINING, false);
    addLabel(builder, "remainingFixed", BalanceStat.RECURRING_REMAINING, true);
    addLabel(builder, "remainingEnvelope", BalanceStat.ENVELOPES_REMAINING, true);
    addLabel(builder, "remainingInSavings", BalanceStat.SAVINGS_IN_REMAINING, true);
    addLabel(builder, "remainingOutSavings", BalanceStat.SAVINGS_OUT_REMAINING, true);
    addLabel(builder, "remainingSpecial", BalanceStat.SPECIAL_REMAINING, true);

    JPanel panel = builder.load();

    dialog = PicsouDialog.create(directory.get(JFrame.class), true, directory);
    dialog.setPanelAndButton(panel, new CloseAction(dialog));
    dialog.pack();
  }

  private void registerBalanceChartUpdater() {
    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        updateBalanceChart(selection.getAll(BalanceStat.TYPE));
      }
    }, BalanceStat.TYPE);
  }

  private void updateBalanceChart(GlobList balanceStats) {
    StackChartDataset incomeDataset = new StackChartDataset();
    StackChartDataset expensesDataset = new StackChartDataset();
    for (BudgetArea budgetArea : BudgetArea.INCOME_AND_EXPENSES_AREAS) {
      Double amount = balanceStats.getSum(BalanceStat.getSummary(budgetArea));
      StackChartDataset dataset = amount > 0 ? incomeDataset : expensesDataset;
      dataset.add(budgetArea.getLabel(), Math.abs(amount), null, false);
    }

    balanceChart.update(incomeDataset, expensesDataset, balanceChartColors);
  }

  private void addLabel(GlobsPanelBuilder builder, String name, DoubleField field, boolean invert) {
    builder.addLabel(name, BalanceStat.TYPE, GlobListStringifiers.sum(field, Formatting.DECIMAL_FORMAT, invert));
  }

  private Glob getLastBalanceStat(GlobList list) {
    list.sort(BalanceStat.MONTH);
    return list.getLast();
  }

  private class BalanceStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty()) {
        return "";
      }
      double total = list.getSum(BalanceStat.MONTH_BALANCE);
      return Formatting.toStringWithPlus(total);
    }
  }

  private class EspectedPositionStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob balanceStat = getLastBalanceStat(list);
      if (balanceStat == null) {
        return "";
      }
      return Formatting.toString(balanceStat.get(BalanceStat.END_OF_MONTH_ACCOUNT_POSITION));
    }
  }

  private class PositionDateStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob balanceStat = getLastBalanceStat(list);
      if (balanceStat == null) {
        return "";
      }
      final String date = Formatting.toString(Month.getLastDay(balanceStat.get(BalanceStat.MONTH)));
      return Lang.get("budgetSummaryDetails.position.date", date);
    }
  }

  private class InitialPositionStringifier implements GlobListStringifier {
    public String toString(GlobList balanceStats, GlobRepository repository) {
      balanceStats.sort(BalanceStat.MONTH);
      if (balanceStats.isEmpty()) {
        return "";
      }

      Integer currentMonthId = CurrentMonth.getLastTransactionMonth(repository);
      Glob balanceStat = null;
      for (Glob stat : balanceStats) {
        balanceStat = stat;
        if (balanceStat.get(BalanceStat.MONTH) >= currentMonthId) {
          break;
        }
      }
      if (balanceStat == null) {
        return "";
      }

      Double amount = balanceStat.get(BalanceStat.LAST_KNOWN_ACCOUNT_POSITION);
      if (amount == null) {
        amount = balanceStat.get(BalanceStat.BEGIN_OF_MONTH_ACCOUNT_POSITION);
      }
      return Formatting.toString(amount);
    }
  }
}