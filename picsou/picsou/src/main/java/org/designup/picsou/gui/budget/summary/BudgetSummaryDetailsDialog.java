package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.components.CloseAction;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.MonthListStringifier;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.help.HelpAction;
import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.util.Amounts;
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
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class BudgetSummaryDetailsDialog {

  private JLabel title;
  private JLabel amountSummaryLabel;
  private JTextArea positionDescription;
  private StackChart balanceChart;
  private StackChartColors balanceChartColors;
  private CardHandler positionCard;
  private PicsouDialog dialog;

  private Directory directory;
  private LocalGlobRepository localRepository;

  public BudgetSummaryDetailsDialog(GlobRepository repository, Directory parentDirectory) {
    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(BudgetStat.TYPE)
        .copy(AccountPositionThreshold.TYPE)
        .copy(Month.TYPE)
        .copy(CurrentMonth.TYPE)
        .get();

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

    localRepository.rollback();

    Integer maxMonthId = selectedMonths.getLast().get(Month.ID);
    if (maxMonthId >= CurrentMonth.getLastTransactionMonth(localRepository)) {
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
      stats.add(localRepository.find(Key.create(BudgetStat.TYPE, month.get(Month.ID))));
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
      new GlobsPanelBuilder(getClass(), "/layout/budgetSummaryDetailsDialog.splits", localRepository, directory);

    dialog = PicsouDialog.create(directory.get(JFrame.class), true, directory);

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
    addLabel(builder, "remainingExtras", BudgetStat.EXTRAS_REMAINING, true);

    PositionThresholdIndicator thresholdIndicator =
      builder.add("thresholdIndicator",
                  new PositionThresholdIndicator(localRepository, directory,
                                                 "budgetSummaryDialog.threshold.top",
                                                 "budgetSummaryDialog.threshold.bottom",
                                                 "budgetSummaryDialog.threshold.border")).getComponent();
    builder.addEditor("thresholdField", AccountPositionThreshold.THRESHOLD)
      .forceSelection(AccountPositionThreshold.KEY)
      .setNotifyOnKeyPressed(true)
      .setValueForNull(0.00);
    builder.addLabel("thresholdMessage", BudgetStat.TYPE, new ThresholdStringifier())
      .setUpdateMatcher(ChangeSetMatchers.changesForKey(AccountPositionThreshold.KEY));
    builder.add("thresholdHelp", new HelpAction(Lang.get("help"), "positionThreshold", directory, dialog));

    JPanel panel = builder.load();

    dialog.addPanelWithButtons(panel, new ValidateAction(), new CloseAction(dialog));
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

  private class ThresholdStringifier implements GlobListStringifier {
    public String toString(GlobList stats, GlobRepository repository) {
      Glob budgetStat = getLastBudgetStat(stats);
      if (budgetStat == null) {
        return "";
      }
      Double position = budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
      Double threshold = AccountPositionThreshold.getValue(repository);
      double diff = Amounts.diff(position, threshold);
      if (diff < 0) {
        return Lang.get("budgetSummaryDetails.threshold.negative");
      }
      else if (diff > 0) {
        return Lang.get("budgetSummaryDetails.threshold.positive");
      }
      return Lang.get("budgetSummaryDetails.threshold.equal");
    }
  }

  private class ValidateAction extends AbstractAction {

    private ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      localRepository.commitChanges(false);
      dialog.setVisible(false);
    }
  }
}