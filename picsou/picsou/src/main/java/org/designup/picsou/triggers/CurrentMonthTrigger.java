package org.designup.picsou.triggers;

import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class CurrentMonthTrigger implements ChangeSetListener {

  public CurrentMonthTrigger(Directory directory) {
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    int lastMonthId = 0;
    if (changeSet.containsChanges(Transaction.TYPE)) {
      GlobList transactions = repository.getAll(Transaction.TYPE);
      for (Glob transaction : transactions) {
        Integer monthId = transaction.get(Transaction.BANK_MONTH);
        if (!transaction.get(Transaction.PLANNED) && monthId > lastMonthId) {
          lastMonthId = monthId;
        }
      }
      if (lastMonthId != 0) {
        repository.update(CurrentMonth.KEY, CurrentMonth.MONTH_ID, lastMonthId);
      }
    }
    if (changeSet.containsChanges(CurrentMonth.KEY) || lastMonthId != 0) {
      Integer currentMonth = repository.get(CurrentMonth.KEY).get(CurrentMonth.MONTH_ID);
      GlobList transactions = repository.getAll(Transaction.TYPE, GlobMatchers.and(
        GlobMatchers.fieldEquals(Transaction.PLANNED, true),
        GlobMatchers.fieldStrickyLesser(Transaction.MONTH, currentMonth)));
      repository.delete(transactions);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
