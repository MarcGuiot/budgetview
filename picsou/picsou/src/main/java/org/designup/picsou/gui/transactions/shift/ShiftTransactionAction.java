package org.designup.picsou.gui.transactions.shift;

import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class ShiftTransactionAction extends AbstractAction implements GlobSelectionListener, ChangeSetListener {
  protected static final int DAY_LIMIT_FOR_PREVIOUS = 10;
  protected static final int DAY_LIMIT_FOR_NEXT = 20;

  protected final GlobRepository repository;
  protected final Directory directory;
  private Glob transaction;
  private ShiftDirection direction;

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
    if (repository.find(transaction.getKey()) == null) {
      transaction = null;
      updateState();
      return;
    }
    if (changeSet.containsChanges(transaction.getKey())) {
      updateState();
      return;
    }
    Integer seriesId = transaction.get(Transaction.SERIES);
    if (seriesId != null){
      if (changeSet.containsChanges(Key.create(Series.TYPE, seriesId))){
        updateState();
        return;
      }
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
    if (transaction == null) {
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

    int day = transaction.get(Transaction.DAY);
    int month = transaction.get(Transaction.MONTH);

    if (day < DAY_LIMIT_FOR_PREVIOUS) {
      int monthToCheck = Month.previous(month);
      Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
      boolean isValidMonth = Series.checkIsValidMonth(monthToCheck, series);
      if (isValidMonth && repository.contains(Transaction.TYPE, fieldEquals(Transaction.MONTH, monthToCheck))) {
        direction = ShiftDirection.PREVIOUS;
        setEnabled(true);
        return;
      }
    }

    if (day > DAY_LIMIT_FOR_NEXT) {
      int monthToCheck = Month.next(month);
      Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
      boolean isValidMonth = Series.checkIsValidMonth(monthToCheck, series);
      if (isValidMonth && repository.contains(Transaction.TYPE,
                                              and(fieldEquals(Transaction.MONTH, monthToCheck),
                                                  fieldEquals(Transaction.PLANNED, false)))) {
        direction = ShiftDirection.NEXT;
        setEnabled(true);
        return;
      }
    }

    setEnabled(false);
  }

  public void actionPerformed(ActionEvent e) {
    if (transaction.get(Transaction.DAY_BEFORE_SHIFT) == null) {
      ConfirmationDialog dialog = new ConfirmationDialog("shift.transaction.title",
                                                         getMessageKey(direction),
                                                         directory.get(JFrame.class),
                                                         directory) {
        protected void postValidate() {
          doShift(transaction);
        }
      };
      dialog.show();
    }
    else {
      unshift();
    }
  }

  private void doShift(Glob transaction) {
    int day = transaction.get(Transaction.DAY);
    int month = transaction.get(Transaction.MONTH);

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
                      value(Transaction.MONTH, newMonth),
                      value(Transaction.DAY, newDay),
                      value(Transaction.DAY_BEFORE_SHIFT, day));
  }

  protected void unshift() {
    int dayBeforeShift = transaction.get(Transaction.DAY_BEFORE_SHIFT);
    int month = transaction.get(Transaction.MONTH);
    int monthBeforeShift = month;
    if (dayBeforeShift > DAY_LIMIT_FOR_NEXT) {
      monthBeforeShift = month - 1;
    }
    else if (dayBeforeShift < DAY_LIMIT_FOR_PREVIOUS) {
      monthBeforeShift = month + 1;
    }

    repository.update(transaction.getKey(),
                      value(Transaction.DAY, dayBeforeShift),
                      value(Transaction.MONTH, monthBeforeShift),
                      value(Transaction.DAY_BEFORE_SHIFT, null));
  }

  private String getMessageKey(ShiftDirection direction) {
    return "shift.transaction.message." + direction.name().toLowerCase();
  }
}
