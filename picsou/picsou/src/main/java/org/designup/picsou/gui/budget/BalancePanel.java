package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.SortedSet;

public class BalancePanel {
  private GlobRepository repository;
  private GlobsPanelBuilder builder;
  private JPanel panel;

  private JLabel previousMonthLabel = new JLabel();
  private JLabel previousMonthPosition = new JLabel();
  private JLabel shiftOperationsLabel = new JLabel();
  private JLabel shiftOperationsAmount = new JLabel();
  private JLabel newStartPositionLabel = new JLabel();
  private JLabel newStartPositionAmount = new JLabel();
  private JLabel bankPositionAmount = new JLabel();
  private JLabel estimatedPositionLabel = new JLabel();
  private JLabel estimatedPosition = new JLabel();
  private JLabel budgetBalanceLabel = new JLabel();
  private JLabel realBalanceLabel = new JLabel();
  private JLabel currentOperationsLabel = new JLabel();
  private JLabel waitedOperationsAmount = new JLabel();
  private JLabel waitedOperationsLabel = new JLabel();
  private JLabel waitedIncomeAmountLabel = new JLabel();
  private JLabel waitedIncomeAmount = new JLabel();
  private JLabel waitedExpenseAmountLabel = new JLabel();
  private JLabel waitedExpenseAmount = new JLabel();
  private JLabel waitedSavingsAmountLabel = new JLabel();
  private JLabel waitedSavingsAmount = new JLabel();
  private JLabel currentOperationsAmount = new JLabel();
  private JLabel currentIncomeAmountLabel = new JLabel();
  private JLabel currentIncomeAmount = new JLabel();
  private JLabel currentExpenseAmountLabel = new JLabel();
  private JLabel currentExpenseAmount = new JLabel();
  private JLabel currentSavingsAmountLabel = new JLabel();
  private JLabel currentSavingsAmount = new JLabel();
  private JLabel waitedOperations[] = {waitedExpenseAmount, waitedExpenseAmountLabel,
                                       waitedIncomeAmount, waitedIncomeAmountLabel,
                                       waitedOperationsAmount, waitedOperationsLabel,
                                       waitedSavingsAmount, waitedSavingsAmountLabel};
  private JLabel currentOperations[] = {currentExpenseAmount, currentExpenseAmountLabel,
                                       currentIncomeAmount, currentIncomeAmountLabel,
                                       currentOperationsAmount, currentOperationsLabel,
                                       currentSavingsAmount, currentSavingsAmountLabel};


  public BalancePanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    builder = new GlobsPanelBuilder(getClass(), "/layout/balancePanel.splits", repository, directory);
    builder.add("budgetBalanceLabel", budgetBalanceLabel);
    builder.add("realBalanceLabel", realBalanceLabel);
    builder.add("previousMonthLabel", previousMonthLabel);
    builder.add("previousMonthPosition", previousMonthPosition);
    builder.add("shiftOperationsLabel", shiftOperationsLabel);
    builder.add("shiftOperationsAmount", shiftOperationsAmount);
    builder.add("newStartPositionLabel", newStartPositionLabel);
    builder.add("newStartPositionAmount", newStartPositionAmount);

    builder.add("currentOperationsAmount", currentOperationsAmount);
    builder.add("currentOperationsLabel", currentOperationsLabel);
    builder.add("currentIncomeAmountLabel", currentIncomeAmountLabel);
    builder.add("currentIncomeAmount", currentIncomeAmount);
    builder.add("currentExpenseAmountLabel", currentExpenseAmountLabel);
    builder.add("currentExpenseAmount", currentExpenseAmount);
    builder.add("currentSavingsAmountLabel", currentSavingsAmountLabel);
    builder.add("currentSavingsAmount", currentSavingsAmount);

    builder.add("bankPositionAmount", bankPositionAmount);

    builder.add("waitedOperationsLabel", waitedOperationsLabel);
    builder.add("waitedOperationsAmount", waitedOperationsAmount);
    builder.add("waitedIncomeAmountLabel", waitedIncomeAmountLabel);
    builder.add("waitedIncomeAmount", waitedIncomeAmount);
    builder.add("waitedExpenseAmountLabel", waitedExpenseAmountLabel);
    builder.add("waitedExpenseAmount", waitedExpenseAmount);
    builder.add("waitedSavingsAmountLabel", waitedSavingsAmountLabel);
    builder.add("waitedSavingsAmount", waitedSavingsAmount);

    builder.add("estimatedPositionLabel", estimatedPositionLabel);
    builder.add("estimatedPosition", estimatedPosition);
    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void setMonth(SortedSet<Integer> monthIds) {
    setMonth(monthIds.first(), monthIds.last());
  }

  private void setMonth(int firstMonthId, int lastMonthId) {
    Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);

    boolean hasCurrentMonth = true;
    boolean isPast = false;
    boolean isFuture = false;
    if (firstMonthId > currentMonthId) {
      hasCurrentMonth = false;
      isFuture = true;
    }
    if (lastMonthId < currentMonthId) {
      hasCurrentMonth = false;
      isPast = true;
    }

    Glob firstMonthStat = repository.find(Key.create(BudgetStat.TYPE, firstMonthId));
    Glob currentMonthStat = repository.find(Key.create(BudgetStat.TYPE, currentMonthId));
    Glob lastMonthStat = repository.find(Key.create(BudgetStat.TYPE, lastMonthId));

    double balance = 0;
    double expence_past = 0;
    double income_past = 0;
    double savings_past = 0;
    double expence_future = 0;
    double income_future = 0;
    double savings_future = 0;
    for (Integer monthId = firstMonthId; monthId <= lastMonthId; monthId = Month.next(monthId)) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      balance += Amounts.zeroIfNull(stat.get(BudgetStat.MONTH_BALANCE));
      if (monthId <= currentMonthId) {
        for (DoubleField field : BudgetStat.EXPENSE_FIELDS) {
          expence_past += Amounts.zeroIfNull(stat.get(field));
        }
        income_past += Amounts.zeroIfNull(stat.get(BudgetStat.INCOME));
        savings_past += Amounts.zeroIfNull(stat.get(BudgetStat.SAVINGS));
      }
      if (monthId >=currentMonthId){
        for (DoubleField field : BudgetStat.EXPENSE_REMAINING_FIELDS) {
          expence_future += Amounts.zeroIfNull(stat.get(field));
        }
        income_future += Amounts.zeroIfNull(stat.get(BudgetStat.INCOME_NEGATIVE_REMAINING)) +
                         Amounts.zeroIfNull(stat.get(BudgetStat.INCOME_POSITIVE_REMAINING));
        savings_future += Amounts.zeroIfNull(stat.get(BudgetStat.SAVINGS_NEGATIVE_REMAINING)) +
                          Amounts.zeroIfNull(stat.get(BudgetStat.SAVINGS_POSITIVE_REMAINING));
      }
    }

    Double previousMonthBankPosition = Amounts.zeroIfNull(firstMonthStat.get(BudgetStat.BEGIN_OF_MONTH_ACCOUNT_POSITION));
    Double endOfMonthPosition = Amounts.zeroIfNull(lastMonthStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION));
    Double bankPosition = Amounts.zeroIfNull(currentMonthStat.get(BudgetStat.LAST_KNOWN_ACCOUNT_POSITION));
    double newStartPosition = endOfMonthPosition - balance;
    double shiftAmount = newStartPosition - previousMonthBankPosition;


    previousMonthLabel.setText(Lang.getWithDefault("balance.panel.previousMonthLabel." + firstMonthId,
                                                   "balance.panel.previousMonthLabel",
                                                   Month.getFullMonthLabel(Month.previous(firstMonthId)).toLowerCase()));

    if (hasCurrentMonth || isPast) {
      newStartPositionLabel.setVisible(true);
      newStartPositionLabel.setText(Lang.get("balance.panel.newStartPosition"));
      shiftOperationsLabel.setVisible(true);
      shiftOperationsLabel.setText(Lang.getWithDefault("balance.panel.shiftOperations." + firstMonthId,
                                                       "balance.panel.shiftOperations",
                                                       Month.getFullMonthLabel(firstMonthId).toLowerCase(),
                                                       Month.getFullMonthLabel(Month.previous(firstMonthId)).toLowerCase()));
      newStartPositionAmount.setVisible(true);
      newStartPositionAmount.setText(Formatting.toString(newStartPosition));
      shiftOperationsAmount.setVisible(true);
      shiftOperationsAmount.setText(Formatting.toString(shiftAmount));

      for (JLabel operation : currentOperations) {
        operation.setVisible(true);
      }
      currentOperationsAmount.setText(Formatting.toString(bankPosition - newStartPosition));
      currentExpenseAmount.setText(Formatting.toString(expence_past));
      currentSavingsAmount.setText(Formatting.toString(savings_past));
      currentIncomeAmount.setText(Formatting.toString(income_past));
      realBalanceLabel.setVisible(true);
      realBalanceLabel.setText(Lang.get("balance.panel.realBalanceLabel",
                                        Formatting.toString(endOfMonthPosition - previousMonthBankPosition),
                                        Formatting.toString(endOfMonthPosition) + " - " + addParenthesis(previousMonthBankPosition)));
    }
    else {
      for (JLabel operation : currentOperations) {
        operation.setVisible(false);
      }
      currentOperationsLabel.setVisible(false);
      newStartPositionAmount.setVisible(false);
      shiftOperationsAmount.setVisible(false);
      newStartPositionLabel.setVisible(false);
      shiftOperationsLabel.setVisible(false);
      currentOperationsAmount.setVisible(false);
      realBalanceLabel.setVisible(false);
    }

    if (hasCurrentMonth || isFuture) {
      for (JLabel operation : waitedOperations) {
        operation.setVisible(true);
      }
      waitedOperationsAmount.setText(Formatting.toString(endOfMonthPosition - bankPosition));
      waitedExpenseAmount.setText(Formatting.toString(expence_future));
      waitedSavingsAmount.setText(Formatting.toString(savings_future));
      waitedIncomeAmount.setText(Formatting.toString(income_future));

      estimatedPositionLabel.setText(Lang.getWithDefault("balance.panel.estimatedPosition." + lastMonthId,
                                                         "balance.panel.estimatedPosition",
                                                         Month.getFullMonthLabel(lastMonthId).toLowerCase()));
    }
    else {
      for (JLabel operation : waitedOperations) {
        operation.setVisible(false);
      }
      estimatedPositionLabel.setText(Lang.getWithDefault("balance.panel.positionAtEnd" + lastMonthId,
                                                         "balance.panel.positionAtEnd",
                                                         Month.getFullMonthLabel(lastMonthId).toLowerCase()));
    }

    if (hasCurrentMonth) {
      bankPositionAmount.setVisible(true);
      bankPositionAmount.setText(Formatting.toString(bankPosition));
    }
    else {
      bankPositionAmount.setVisible(false);
    }

    previousMonthPosition.setText(Formatting.toString(previousMonthBankPosition));
    estimatedPosition.setText(Formatting.toString(endOfMonthPosition));

    budgetBalanceLabel.setText(Lang.get("balance.panel.budgetBalanceLabel",
                                        Formatting.toString(balance),
                                        Formatting.toString(endOfMonthPosition) + " - " +
                                        addParenthesis(newStartPosition)));

  }

  private String addParenthesis(double amount) {
    return (amount < 0 ?
            "(" + Formatting.toString(amount) + ")" :
            Formatting.toString(amount));
  }
}
