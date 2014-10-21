package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.*;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SavingsAccountUpdateSeriesTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Account.IS_IMPORTED_ACCOUNT) && values.get(Account.IS_IMPORTED_ACCOUNT)) {
          deleteCreatedBySeries(key, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void deleteCreatedBySeries(Key key, GlobRepository repository) {
    GlobList savingSeries = repository.getAll(Series.TYPE,
                                              fieldEquals(Series.BUDGET_AREA, BudgetArea.TRANSFER.getId()));
    repository.startChangeSet();
    try {
      for (Glob series : savingSeries) {
        if ((series.get(Series.FROM_ACCOUNT).equals(key.get(Account.ID))) ||
            (series.get(Series.TO_ACCOUNT).equals(key.get(Account.ID)))) {
          repository.delete(
            repository.getAll(Transaction.TYPE,
                              and(fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                                  or(fieldEquals(Transaction.CREATED_BY_SERIES, Boolean.TRUE),
                                     fieldEquals(Transaction.MIRROR, Boolean.TRUE)))));
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
