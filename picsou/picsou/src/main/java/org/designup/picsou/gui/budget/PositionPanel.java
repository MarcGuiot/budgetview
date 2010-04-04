package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.help.HyperlinkHandler;
import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.monthsummary.AccountPositionThresholdDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.SortedSet;

public class PositionPanel {
  private PicsouDialog dialog;
  private GlobRepository repository;
  private GlobsPanelBuilder builder;
  private JPanel panel;

  private JLabel bankPositionAmount = new JLabel();
  private JLabel bankPositionLabel = new JLabel();
  private JLabel estimatedPositionLabel = new JLabel();
  private JLabel estimatedPosition = new JLabel();
  private JLabel waitedIncomeAmount = new JLabel();
  private JLabel waitedExpenseAmount = new JLabel();
  private JLabel waitedSavingsAmount = new JLabel();
  private JEditorPane positionPanelLimit = GuiUtils.createReadOnlyHtmlComponent();

  private JEditorPane positionPast = GuiUtils.createReadOnlyHtmlComponent();
  private JLabel positionPastAmount = new JLabel();
  private JEditorPane thresholdPast = GuiUtils.createReadOnlyHtmlComponent();
  private CardHandler cardHandler;
  private SortedSet<Integer> monthIds;

  public PositionPanel(final PicsouDialog dialog, final GlobRepository repository, final Directory directory) {
    this.dialog = dialog;
    this.repository = repository;
    builder = new GlobsPanelBuilder(getClass(), "/layout/budget/positionPanel.splits", repository, directory);
    cardHandler = builder.addCardHandler("cardHandler");
    builder.add("bankPositionAmount", bankPositionAmount);
    builder.add("bankPositionLabel", bankPositionLabel);

    builder.add("waitedIncomeAmount", waitedIncomeAmount);
    builder.add("waitedExpenseAmount", waitedExpenseAmount);
    builder.add("waitedSavingsAmount", waitedSavingsAmount);

    builder.add("estimatedPositionLabel", estimatedPositionLabel);
    builder.add("estimatedPosition", estimatedPosition);
    builder.add("positionPanelLimit", positionPanelLimit);

    builder.add("positionPast", positionPast);
    builder.add("positionPastAmount", positionPastAmount);
    builder.add("thresholdPast", thresholdPast);

    HyperlinkHandler thresholdHandler = new HyperlinkHandler(directory) {
      protected void processCustomLink(String href) {
        if (href.equals("alerte")) {
          AccountPositionThresholdDialog positionThresholdDialog =
            new AccountPositionThresholdDialog(dialog, repository, directory);
          positionThresholdDialog.show();
          update();
        }
      }
    };
    positionPanelLimit.addHyperlinkListener(thresholdHandler);
    thresholdPast.addHyperlinkListener(thresholdHandler);
    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void setMonth(SortedSet<Integer> monthIds) {
    this.monthIds = monthIds;
    update();
  }

  private void update() {
    Integer currentMonthId = repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH);

    boolean hasCurrentMonth = false;
    boolean isPastOnly = true;
    for (Integer monthId : monthIds) {
      if (currentMonthId.equals(monthId)) {
        hasCurrentMonth = true;
      }
      if (monthId >= currentMonthId) {
        isPastOnly = false;
      }
    }

    if (isPastOnly) {
      updatePastCard(monthIds.last());
      cardHandler.show("past");
    }
    else {
      updatePresentOrFutureCard(monthIds, currentMonthId, hasCurrentMonth);
      cardHandler.show("present");
    }
  }

  private void updatePastCard(Integer lastMonthId) {
    Glob lastMonthStat = repository.find(Key.create(BudgetStat.TYPE, lastMonthId));
    positionPast.setText(Lang.getWithDefault("position.panel.past.label." + lastMonthId,
                                             "position.panel.past.label",
                                             Month.getFullMonthLabel(lastMonthId),
                                             Month.toYearString(lastMonthId)));
    Double amount = lastMonthStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION);
    positionPastAmount.setText(Formatting.toStringWithPlus(amount));
    Double threshold = repository.get(AccountPositionThreshold.KEY).get(AccountPositionThreshold.THRESHOLD);
    Double thresholdWarm = repository.get(AccountPositionThreshold.KEY).get(AccountPositionThreshold.THRESHOLD_FOR_WARN);
    if (amount < threshold - thresholdWarm) {
      thresholdPast.setText(Lang.get("position.panel.past.threshold.inf",
                                     Formatting.toString(threshold)));
    }
    else if (amount > threshold + thresholdWarm) {
      thresholdPast.setText(Lang.get("position.panel.past.threshold.sup",
                                     Formatting.toString(threshold)));
    }
    else {
      thresholdPast.setText(Lang.get("position.panel.past.threshold.equal", Formatting.toString(threshold)));
    }
  }

  private void updatePresentOrFutureCard(SortedSet<Integer> monthIds, Integer currentMonthId, boolean hasCurrentMonth) {
    Integer firstMonthId = monthIds.first();
    Integer lastMonthId = monthIds.last();
    Glob firstMonthStat = repository.find(Key.create(BudgetStat.TYPE, firstMonthId));
    Glob currentMonthStat = repository.find(Key.create(BudgetStat.TYPE, currentMonthId));
    Glob lastMonthStat = repository.find(Key.create(BudgetStat.TYPE, lastMonthId));

    double expence_future = 0;
    double income_future = 0;
    double savings_future = 0;
    for (Integer monthId : monthIds) {
      Glob stat = repository.find(Key.create(BudgetStat.TYPE, monthId));
      if (monthId >= currentMonthId) {
        for (DoubleField field : BudgetStat.EXPENSE_REMAINING_FIELDS) {
          expence_future += Amounts.zeroIfNull(stat.get(field));
        }
        income_future += Amounts.zeroIfNull(stat.get(BudgetStat.INCOME_NEGATIVE_REMAINING)) +
                         Amounts.zeroIfNull(stat.get(BudgetStat.INCOME_POSITIVE_REMAINING));
        savings_future += Amounts.zeroIfNull(stat.get(BudgetStat.SAVINGS_NEGATIVE_REMAINING)) +
                          Amounts.zeroIfNull(stat.get(BudgetStat.SAVINGS_POSITIVE_REMAINING));
      }
    }

    Double bankPosition;
    if (hasCurrentMonth) {
      bankPosition = Amounts.zeroIfNull(currentMonthStat.get(BudgetStat.LAST_KNOWN_ACCOUNT_POSITION));
    }
    else {
      bankPosition = Amounts.zeroIfNull(firstMonthStat.get(BudgetStat.BEGIN_OF_MONTH_ACCOUNT_POSITION));
    }

    if (hasCurrentMonth) {
      bankPositionLabel.setText(Lang.get("position.panel.bankPositionLabel"));
    }
    else {
      bankPositionLabel.setText(Lang.get("position.panel.bankPositionLabel.future"));
    }
    bankPositionAmount.setText(Formatting.toString(bankPosition));


    waitedIncomeAmount.setText(Formatting.toStringWithPlus(income_future));
    waitedExpenseAmount.setText(Formatting.toStringWithPlus(expence_future));
    waitedSavingsAmount.setText(Formatting.toStringWithPlus(savings_future));

    estimatedPositionLabel.setText(Lang.getWithDefault("position.panel.estimatedLabel." + lastMonthId,
                                                       "position.panel.estimatedLabel",
                                                       Month.getFullMonthLabel(lastMonthId).toLowerCase()));

    estimatedPosition.setText(
      Formatting.toString(lastMonthStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION)));

    updateLimiteMessage(lastMonthStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION));
  }

  private void updateLimiteMessage(final Double currentPosition) {
    Double threshold = repository.get(AccountPositionThreshold.KEY).get(AccountPositionThreshold.THRESHOLD);
    Double thresholdWarn = repository.get(AccountPositionThreshold.KEY).get(AccountPositionThreshold.THRESHOLD_FOR_WARN);
    if (currentPosition < (threshold - thresholdWarn)) {
      positionPanelLimit.setText(Lang.get("position.panel.alerte.inf", Formatting.toString(threshold)));
    }
    else {
      boolean hasSavingsAccount =
        !repository.getAll(Account.TYPE,
                           GlobMatchers.and(
                             GlobMatchers.fieldEquals(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()),
                             GlobMatchers.not(GlobMatchers.fieldIn(Account.ID, Account.SUMMARY_ACCOUNT_IDS)))).isEmpty();
      if (currentPosition > threshold + thresholdWarn) {
        if (hasSavingsAccount) {
          positionPanelLimit.setText(Lang.get("position.panel.alerte.sup.withSavings", Formatting.toString(threshold)));
        }
        else {
          positionPanelLimit.setText(Lang.get("position.panel.alerte.sup.withoutSavings", Formatting.toString(threshold)));
        }
      }
      else {
        if (hasSavingsAccount) {
          positionPanelLimit.setText(Lang.get("position.panel.alerte.zero.withSavings", Formatting.toString(threshold)));
        }
        else {
          positionPanelLimit.setText(Lang.get("position.panel.alerte.zero.withoutSavings", Formatting.toString(threshold)));
        }
      }
    }
  }
}