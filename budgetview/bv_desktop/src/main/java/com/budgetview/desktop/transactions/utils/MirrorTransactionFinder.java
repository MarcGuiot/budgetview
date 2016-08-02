package com.budgetview.desktop.transactions.utils;

import com.budgetview.model.Month;
import com.budgetview.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.Comparator;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class MirrorTransactionFinder {
  public static GlobList getClosestMirrors(GlobList sources, Integer targetAccountId, GlobList all, GlobRepository repository) {
    GlobList targetTransactions = all.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, targetAccountId), repository);
    GlobList result = new GlobList();
    TransactionDateComparator comparator = new TransactionDateComparator();
    for (Glob source : sources) {
      GlobList candidates =
        targetTransactions.filter(fieldEquals(Transaction.AMOUNT, source.get(Transaction.AMOUNT) * -1), repository);
      if (candidates.size() == 0) {
        continue;
      }
      comparator.setCurrent(source);
      candidates.sortSelf(comparator);
      Glob bestCandidate = candidates.getFirst();
      if (getDateDistance(source, bestCandidate) < 5) {
        result.add(bestCandidate);
      }
    }
    return result;
  }

  public static class TransactionDateComparator implements Comparator<Glob> {

    private Integer month;
    private Integer day;

    public void setCurrent(Glob referenceTransaction) {
      this.month = referenceTransaction.get(Transaction.POSITION_MONTH);
      this.day = referenceTransaction.get(Transaction.POSITION_DAY);
    }

    public int compare(Glob transaction1, Glob transaction2) {
      if ((transaction1 == null) && (transaction2 == null)) {
        return 0;
      }
      if (transaction1 == null) {
        return -1;
      }
      if (transaction2 == null) {
        return 1;
      }

      int dateDistance1 = getDateDistance(transaction1, month, day);
      int dateDistance2 = getDateDistance(transaction2, month, day);
      if (dateDistance1 < dateDistance2) {
        return -1;
      }
      else if (dateDistance1 > dateDistance2) {
        return 1;
      }

      return Utils.compare(transaction1.get(Transaction.ID), transaction2.get(Transaction.ID));
    }
  }

  public static int getDateDistance(Glob transaction1, Glob transaction2) {
    return getDateDistance(transaction1, transaction2.get(Transaction.POSITION_MONTH), transaction2.get(Transaction.POSITION_DAY));
  }

  public static int getDateDistance(Glob transaction1, Integer month, Integer day) {
    return Math.abs(Month.distance(month, day,
                                   transaction1.get(Transaction.POSITION_MONTH),
                                   transaction1.get(Transaction.POSITION_DAY)));
  }
}
