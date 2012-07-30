package org.designup.picsou.gui.categorization.reconciliation;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.Utils;

public class ReferenceTransactionFilter implements GlobMatcher {

  private ClosedMonthRange range;
  private Integer accountId;

  ReferenceTransactionFilter(Glob current) {
    int month = current.get(Transaction.POSITION_MONTH);
    range = new ClosedMonthRange(Month.previous(month), Month.next(month));
    accountId = current.get(Transaction.ACCOUNT); 
  }

  public boolean matches(Glob transaction, GlobRepository repository) {
    return !transaction.isTrue(Transaction.PLANNED) && 
           !transaction.isTrue(Transaction.MIRROR) && 
           !Transaction.isToReconcile(transaction) &&
           range.contains(transaction.get(Transaction.POSITION_MONTH)) &&
           Utils.equal(accountId, transaction.get(Transaction.ACCOUNT));
  }
}
