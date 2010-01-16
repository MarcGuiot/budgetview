package org.designup.picsou.gui.transactions.shift;

import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.Set;

public class ShiftTransactionAction extends AbstractAction implements GlobSelectionListener, ChangeSetListener {
  protected static final int DAY_LIMIT_FOR_PREVIOUS = 10;
  protected static final int DAY_LIMIT_FOR_NEXT = 20;

  protected final GlobRepository repository;
  protected final Directory directory;
  private Glob transaction;
  private ShiftDirection direction;
  private Glob series;
  private boolean validMonthForSeries;
  private int targetMonth;

  public ShiftTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("shift.transaction.button"));
    this.repository = repository;
    this.directory = directory;
    this.repository.addChangeListener(this);
    this.directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    setEnabled(false);
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList transactions = selection.getAll(Transaction.TYPE);
    if (transactions.size() != 1) {
      setEnabled(false);
      return;
    }

    transaction = transactions.getFirst();
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

    putValue(NAME, Lang.get("shift.transaction.button"));
    putValue(SHORT_DESCRIPTION, Lang.get("shift.transaction.tooltip"));

    int day = transaction.get(Transaction.BUDGET_DAY);
    int month = transaction.get(Transaction.BUDGET_MONTH);

    if (day < DAY_LIMIT_FOR_PREVIOUS) {
      targetMonth = Month.previous(month);
      series = repository.findLinkTarget(transaction, Transaction.SERIES);
      validMonthForSeries = Series.isValidMonth(targetMonth, series);
      if (containsTransactions(targetMonth)) {
        direction = ShiftDirection.PREVIOUS;
        setEnabled(true);
        return;
      }
    }

    if (day > DAY_LIMIT_FOR_NEXT) {
      targetMonth = Month.next(month);
      series = repository.findLinkTarget(transaction, Transaction.SERIES);
      validMonthForSeries = Series.isValidMonth(targetMonth, series);
      if (containsTransactions(targetMonth)) {
        direction = ShiftDirection.NEXT;
        setEnabled(true);
        return;
      }
    }

    setEnabled(false);
  }

  public void actionPerformed(ActionEvent e) {
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
                             "shift.transaction.seriesError.message",
                             directory.get(JFrame.class),
                             directory,
                             Month.getFullLabel(targetMonth)) {
        protected void postValidate() {
          getSeriesEditionDialog().show(series, Collections.singleton(transaction.get(Transaction.BUDGET_MONTH)));
        }
      };
    dialog.show();
  }

  private SeriesEditionDialog getSeriesEditionDialog() {
    return directory.get(SeriesEditionDialog.class);
  }

  private void openShiftDialog() {
    ConfirmationDialog dialog =
      new ConfirmationDialog("shift.transaction.title",
                             getMessageKey(direction),
                             directory.get(JFrame.class),
                             directory) {
        protected void postValidate() {
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

  private boolean containsTransactions(int monthToCheck) {
    return repository.contains(Transaction.TYPE,
                               and(fieldEquals(Transaction.BUDGET_MONTH, monthToCheck),
                                   isFalse(Transaction.PLANNED)));
  }

  private String getMessageKey(ShiftDirection direction) {
    return "shift.transaction.message." + direction.name().toLowerCase();
  }
}
