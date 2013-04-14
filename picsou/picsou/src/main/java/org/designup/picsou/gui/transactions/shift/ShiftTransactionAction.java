package org.designup.picsou.gui.transactions.shift;

import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.SingleSelectionAction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Collections;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class ShiftTransactionAction extends SingleSelectionAction implements ChangeSetListener {
  protected static final int DAY_LIMIT_FOR_PREVIOUS = 10;
  protected static final int DAY_LIMIT_FOR_NEXT = 20;

  private Glob transaction;
  private ShiftDirection direction;
  private Glob series;
  private boolean validMonthForSeries;
  private int targetMonth;

  public ShiftTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("shift.transaction.button"), Transaction.TYPE, repository, directory);
    this.repository.addChangeListener(this);
    setEnabled(false);
  }

  protected void processSelection(Glob selectedTransaction) {
    this.transaction = selectedTransaction;
    updateState();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (transaction == null) {
      return;
    }
    if (!repository.contains(transaction.getKey())) {
      transaction = null;
      updateState();
      return;
    }
    if (changeSet.containsChanges(transaction.getKey())) {
      updateState();
      return;
    }
    Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
    if ((series != null) && changeSet.containsChanges(series.getKey())) {
      updateState();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if ((transaction == null) || !changedTypes.contains(Transaction.TYPE)) {
      return;
    }
    transaction = repository.find(transaction.getKey());
    updateState();
  }

  protected void updateState() {
    validMonthForSeries = true;

    if (transaction == null) {
      setEnabled(false);
      return;
    }

    Glob account = repository.findLinkTarget(transaction, Transaction.ACCOUNT);
    if (!account.get(Account.CARD_TYPE).equals(AccountCardType.NOT_A_CARD.getId())) {
      setEnabled(false);
      return;
    }

    if (transaction.get(Transaction.DAY_BEFORE_SHIFT) != null) {
      putValue(NAME, Lang.get("unshift.transaction.button"));
      putValue(SHORT_DESCRIPTION, Lang.get("unshift.transaction.tooltip"));
      setEnabled(true);
      return;
    }

    // tentative de shifter les operation defferees
//    if (!account.get(Account.CARD_TYPE).equals(AccountCardType.NOT_A_CARD.getId())) {
//      Integer month = transaction.get(Transaction.BUDGET_MONTH);
//      Glob deferredCard = repository
//        .findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, account.get(Account.ID))
//        .findByIndex(DeferredCardDate.MONTH, month).getGlobs().getFirst();
//
//      if (deferredCard != null){
//        Integer day = deferredCard.get(DeferredCardDate.DAY);
//
//      }
//
//
//      return;
//    }

    putValue(NAME, Lang.get("shift.transaction.button"));
    putValue(SHORT_DESCRIPTION, Lang.get("shift.transaction.tooltip"));

    int day = transaction.get(Transaction.BUDGET_DAY);
    int month = transaction.get(Transaction.BUDGET_MONTH);

    if (day < DAY_LIMIT_FOR_PREVIOUS) {
      targetMonth = Month.previous(month);
      series = repository.findLinkTarget(transaction, Transaction.SERIES);
      validMonthForSeries = Series.isValidMonth(targetMonth, series);
      direction = ShiftDirection.PREVIOUS;
      setEnabled(true);
      return;
    }

    if (day > DAY_LIMIT_FOR_NEXT) {
      targetMonth = Month.next(month);
      series = repository.findLinkTarget(transaction, Transaction.SERIES);
      validMonthForSeries = Series.isValidMonth(targetMonth, series);
      direction = ShiftDirection.NEXT;
      setEnabled(true);
      return;
    }

    setEnabled(false);
  }

  protected void process(Glob transaction, GlobRepository repository, Directory directory) {

    if (transaction.get(Transaction.DAY_BEFORE_SHIFT) != null) {
      unshift();
      return;
    }

    if (!validMonthForSeries) {
      openSeriesErrorDialog();
      return;
    }

    openShiftDialog();
  }

  private void openSeriesErrorDialog() {
    ConfirmationDialog dialog =
      new ConfirmationDialog("shift.transaction.seriesError.title",
                             Lang.get("shift.transaction.seriesError.message", Month.getFullLabel(targetMonth)),
                             directory.get(JFrame.class),
                             directory) {
        protected void processOk() {
          getSeriesEditor().showSeries(series, Collections.singleton(transaction.get(Transaction.BUDGET_MONTH)));
        }
      };
    dialog.show();
  }

  private SeriesEditor getSeriesEditor() {
    return directory.get(SeriesEditor.class);
  }

  private void openShiftDialog() {
    ConfirmationDialog dialog =
      new ConfirmationDialog("shift.transaction.title",
                             Lang.get(getMessageKey(direction)),
                             directory.get(JFrame.class),
                             directory) {
        protected void processOk() {
          doShift(transaction);
        }
      };
    dialog.show();
  }

  private void doShift(Glob transaction) {
    int day = transaction.get(Transaction.BUDGET_DAY);
    int month = transaction.get(Transaction.BUDGET_MONTH);

    int newDay = day;
    int newMonth = month;
    switch (direction) {
      case PREVIOUS:
        newMonth = Month.previous(month);
        newDay = Month.getLastDayNumber(newMonth);
        break;
      case NEXT:
        newMonth = Month.next(month);
        newDay = 1;
        break;
    }

    Key monthKey = Key.create(Month.TYPE, newMonth);
    Glob glob = repository.find(monthKey);
    if (glob == null) {
      repository.create(monthKey);
    }

    repository.update(transaction.getKey(),
                      value(Transaction.BUDGET_MONTH, newMonth),
                      value(Transaction.BUDGET_DAY, newDay),
                      value(Transaction.DAY_BEFORE_SHIFT, day));
  }

  protected void unshift() {
    int dayBeforeShift = transaction.get(Transaction.DAY_BEFORE_SHIFT);
    int month = transaction.get(Transaction.BUDGET_MONTH);
    int monthBeforeShift = month;
    if (dayBeforeShift > DAY_LIMIT_FOR_NEXT) {
      monthBeforeShift = Month.previous(month);
    }
    else if (dayBeforeShift < DAY_LIMIT_FOR_PREVIOUS) {
      monthBeforeShift = Month.next(month);
    }

    repository.update(transaction.getKey(),
                      value(Transaction.BUDGET_DAY, dayBeforeShift),
                      value(Transaction.BUDGET_MONTH, monthBeforeShift),
                      value(Transaction.DAY_BEFORE_SHIFT, null));
  }

  private String getMessageKey(ShiftDirection direction) {
    return "shift.transaction.message." + direction.name().toLowerCase();
  }
}
