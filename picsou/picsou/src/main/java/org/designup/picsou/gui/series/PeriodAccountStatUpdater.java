package org.designup.picsou.gui.series;

import org.designup.picsou.gui.accounts.position.DailyAccountPositionComputer;
import org.designup.picsou.gui.model.PeriodAccountStat;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import java.util.Set;
import java.util.SortedSet;

import static org.designup.picsou.model.Account.activeUserCreatedMainAccounts;
import static org.globsframework.model.FieldValue.value;

public class PeriodAccountStatUpdater implements ChangeSetListener, GlobSelectionListener {

  private GlobRepository repository;
  private final SelectionService selectionService;

  public static void init(GlobRepository repository, Directory directory) {
    repository.addTrigger(new PeriodAccountStatUpdater(repository, directory));
  }

  public PeriodAccountStatUpdater(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.selectionService.addListener(this, Month.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    recomputeAll();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(Month.TYPE)
        || changeSet.containsChanges(Transaction.TYPE)
        || changeSet.containsChanges(Account.TYPE)) {
      recomputeAll();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE) || changedTypes.contains(Month.TYPE) || changedTypes.contains(Account.TYPE)) {
      recomputeAll();
    }
  }

  private void recomputeAll() {

    repository.startChangeSet();
    try {
      repository.deleteAll(PeriodAccountStat.TYPE);
      final SortedSet<Integer> monthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
      if (!monthIds.isEmpty()) {
        for (Glob account : repository.getAll(Account.TYPE, activeUserCreatedMainAccounts(monthIds))) {
          Integer accountId = account.get(Account.ID);
          Glob stat = repository.findOrCreate(Key.create(PeriodAccountStat.TYPE, accountId));
          repository.update(stat.getKey(),
                            value(PeriodAccountStat.OK, isAccountOk(accountId, monthIds)),
                            value(PeriodAccountStat.SEQUENCE, account.get(Account.SEQUENCE)));
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private boolean isAccountOk(Integer accountId, SortedSet<Integer> monthIds) {
    if (monthIds.isEmpty()) {
      return false;
    }
    boolean transactionsFound = false;
    for (Integer monthId : monthIds) {
      for (Glob transaction : repository.findByIndex(Transaction.POSITION_MONTH_INDEX, monthId)) {
        if (accountId.equals(transaction.get(Transaction.ACCOUNT))) {
          transactionsFound = true;
          if (transaction.get(Transaction.ACCOUNT_POSITION) < 0) {
            return false;
          }
        }
      }
    }
    if (!transactionsFound) {
      GlobMatcher accountMatcher = Matchers.transactionsForAccount(accountId);
      Double lastKnownPosition = DailyAccountPositionComputer.getLastValue(accountMatcher, monthIds.first(), Transaction.ACCOUNT_POSITION, repository);
      return lastKnownPosition == null || lastKnownPosition >= 0;
    }
    return true;
  }
}
