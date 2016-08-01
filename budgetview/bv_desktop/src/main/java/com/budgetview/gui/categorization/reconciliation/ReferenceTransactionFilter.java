package com.budgetview.gui.categorization.reconciliation;

import com.budgetview.model.Transaction;
import com.budgetview.model.Month;
import com.budgetview.model.TransactionType;
import com.budgetview.model.util.ClosedMonthRange;
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
           Utils.equal(accountId, transaction.get(Transaction.ACCOUNT)) &&
           (transaction.get(Transaction.TRANSACTION_TYPE) != TransactionType.OPEN_ACCOUNT_EVENT.getId()) &&
           (transaction.get(Transaction.TRANSACTION_TYPE) != TransactionType.CLOSE_ACCOUNT_EVENT.getId());
  }
}
