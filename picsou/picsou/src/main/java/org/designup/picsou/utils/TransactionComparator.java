package org.designup.picsou.utils;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.Utils;
import org.designup.picsou.gui.transactions.TransactionSplitComparator;
import org.designup.picsou.model.Transaction;

import java.util.Comparator;

public class TransactionComparator implements Comparator<Glob> {

  public static final TransactionComparator ASCENDING = new TransactionComparator(true);
  public static final TransactionComparator DESCENDING = new TransactionComparator(false);

  private int dateMultiplier = 1;
  private TransactionSplitComparator splitComparator = new TransactionSplitComparator();

  public TransactionComparator(boolean ascendingDates) {
    dateMultiplier = ascendingDates ? 1 : -1;
  }

  public int compare(Glob transaction1, Glob transaction2) {
    long dateDiff = (transaction1.get(Transaction.MONTH) - transaction2.get(Transaction.MONTH)) * dateMultiplier;
    if (dateDiff != 0) {
      return (int) dateDiff;
    }
    int dayDiff = (transaction1.get(Transaction.DAY) - transaction2.get(Transaction.DAY)) * dateMultiplier;
    if (dayDiff != 0) {
      return dayDiff;
    }
    int labelDiff = Utils.compare(transaction1.get(Transaction.ORIGINAL_LABEL),
                                  transaction2.get(Transaction.ORIGINAL_LABEL));
    if (labelDiff != 0) {
      return labelDiff;
    }
    int splitDiff = splitComparator.compare(transaction1, transaction2);
    if (splitDiff != 0) {
      return splitDiff;
    }
    return Utils.compare(transaction1.get(Transaction.ID), transaction2.get(Transaction.ID));
  }
}
