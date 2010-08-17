package org.designup.picsou.gui.budget.wizard;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
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

public class BudgetPositionPage {

  private JLabel amountSummaryLabel;
  private JTextArea positionDescription;
  private CardHandler positionCard;

  private Directory directory;
  private GlobRepository repository;
  private JPanel panel;
  private Directory parentDirectory;

  public BudgetPositionPage(GlobRepository repository, Directory parentDirectory) {
    this.repository = repository;
    this.parentDirectory = parentDirectory;

    this.directory = createDirectory(parentDirectory);
  }

  public String getId() {
    return "position";
  }

  public JComponent getPanel() {
    return panel;
  }

  public void init() {
    createDialog();
  }

  public void updateBeforeDisplay() {
    GlobList selectedMonths = getSelectedMonths();
    Integer maxMonthId = selectedMonths.getLast().get(Month.ID);
    if (maxMonthId >= CurrentMonth.getLastTransactionMonth(repository)) {
      showEstimatedPositionDetails();
    }
    else {
      showActualPositionDetails();
    }
    selectStats(selectedMonths);
  }

  private GlobList getSelectedMonths() {
    return parentDirectory.get(SelectionService.class).getSelection(Month.TYPE).sort(Month.ID);
  }

  public void applyChanges() {
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
    positionDescription.setText(Lang.get("budgetPositionPage.description.estimated"));
  }

  private void showActualPositionDetails() {
    positionCard.show("actual");
    positionDescription.setText(Lang.get("budgetPositionPage.description.actual"));
  }

  private static Directory createDirectory(Directory parentDirectory) {
    Directory directory = new DefaultDirectory(parentDirectory);
    directory.add(new SelectionService());
    return directory;
  }

  public void createDialog() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/budget/budgetWizard/budgetPositionPage.splits", repository, directory);

    builder.add("text", Gui.createHelpTextComponent("budgetWizard/endOfMonthPosition.html"));
    builder.add("handler", new HyperlinkHandler(directory));

    positionCard = builder.addCardHandler("cards");

    positionDescription = builder.add("positionDescription", new JTextArea()).getComponent();

    builder.addLabel("estimatedPosition", BudgetStat.TYPE, new EspectedPositionStringifier());
    builder.addLabel("estimatedPositionDate", BudgetStat.TYPE, new PositionDateStringifier());
    builder.addLabel("initialPosition", BudgetStat.TYPE, new InitialPositionStringifier());
    addLabel(builder, "remainingIncome", false, BudgetStat.INCOME_POSITIVE_REMAINING, BudgetStat.INCOME_NEGATIVE_REMAINING);
    addLabel(builder, "remainingFixed", true, BudgetStat.RECURRING_POSITIVE_REMAINING, BudgetStat.RECURRING_NEGATIVE_REMAINING);
    addLabel(builder, "remainingVariable", true, BudgetStat.VARIABLE_POSITIVE_REMAINING, BudgetStat.VARIABLE_NEGATIVE_REMAINING);
    addLabel(builder, "remainingInSavings", false, BudgetStat.SAVINGS_IN_POSITIVE_REMAINING);
    addLabel(builder, "remainingOutSavings", true, BudgetStat.SAVINGS_OUT_NEGATIVE_REMAINING);
    addLabel(builder, "remainingExtras", true, BudgetStat.EXTRAS_POSITIVE_REMAINING, BudgetStat.EXTRAS_NEGATIVE_REMAINING);

    panel = builder.load();
  }

  private void addLabel(GlobsPanelBuilder builder, String name, boolean invert, DoubleField... fields) {
    builder.addLabel(name, BudgetStat.TYPE, GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT, invert, fields));
  }

  private static Glob getLastBudgetStat(GlobList list) {
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

  private static class EspectedPositionStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob budgetStat = getLastBudgetStat(list);
      if (budgetStat == null) {
        return "";
      }
      return Formatting.toString(budgetStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION));
    }
  }

  private static class PositionDateStringifier implements GlobListStringifier {
    public String toString(GlobList list, GlobRepository repository) {
      Glob budgetStat = getLastBudgetStat(list);
      if (budgetStat == null) {
        return "";
      }
      final String date = Formatting.toString(Month.getLastDay(budgetStat.get(BudgetStat.MONTH)));
      return Lang.get("budgetPositionPage.date", date);
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