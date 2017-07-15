package com.budgetview.desktop.utils.datacheck.utils;

import com.budgetview.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatcher;

public class TransactionMonthRange implements GlobFunctor {
  int firstMonthForTransaction = Integer.MAX_VALUE;
  int lastMonthForTransaction = Integer.MIN_VALUE;

  public static TransactionMonthRange get(GlobRepository repository, GlobMatcher matcher) {
    TransactionMonthRange range = new TransactionMonthRange();
    repository.safeApply(Transaction.TYPE, matcher, range);
    return range;
  }

  public void run(Glob glob, GlobRepository repository) throws Exception {
    addMonth(glob.get(Transaction.MONTH));
    addMonth(glob.get(Transaction.BANK_MONTH));
  }

  void addMonth(Integer month) {
    if (month != null) {
      if (month > lastMonthForTransaction) {
        lastMonthForTransaction = month;
      }
      if (month < firstMonthForTransaction) {
        firstMonthForTransaction = month;
      }
    }
  }

  public int first() {
    return firstMonthForTransaction;
  }

  public int last() {
    return lastMonthForTransaction;
  }
}
