package org.designup.picsou.triggers;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class LastTransactionToTimeServiceTrigger implements ChangeSetListener {
  private TimeService timeService;

  public LastTransactionToTimeServiceTrigger(Directory directory) {
    timeService = directory.get(TimeService.class);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      GlobList transactions = repository.getAll(Transaction.TYPE);
      int lastMonthId = 0;
      for (Glob transaction : transactions) {
        Integer monthId = transaction.get(Transaction.BANK_MONTH);
        if (!transaction.get(Transaction.PLANNED) && monthId > lastMonthId) {
          lastMonthId = monthId;
        }
      }
      if (lastMonthId != 0) {
        timeService.setLastAvailableTransactionMonthId(lastMonthId);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
