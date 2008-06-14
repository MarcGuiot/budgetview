package org.designup.picsou.gui.transactions;

import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;

import java.util.Comparator;

public class TransactionSplitComparator implements Comparator<Glob> {
  public int compare(Glob transaction1, Glob transaction2) {
    if (Transaction.isSplitPart(transaction1) && Transaction.isSplitSource(transaction2)) {
      return 1;
    }
    if (Transaction.isSplitSource(transaction1) && Transaction.isSplitPart(transaction2)) {
      return -1;
    }
    return 0;
  }
}
