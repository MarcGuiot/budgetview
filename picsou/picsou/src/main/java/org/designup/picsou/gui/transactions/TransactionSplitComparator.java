package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.Utils;
import org.designup.picsou.model.Transaction;

import java.util.Comparator;

public class TransactionSplitComparator implements Comparator<Glob> {
  public int compare(Glob transaction1, Glob transaction2) {
    if (Transaction.isSplitPart(transaction1) && Transaction.isSplitSource(transaction2)) {
      return 1;
    }
    if (Transaction.isSplitSource(transaction1) && Transaction.isSplitPart(transaction2)) {
      return -1;
    }
    return Utils.compare(transaction1.get(Transaction.ID), transaction2.get(Transaction.ID));
  }
}
