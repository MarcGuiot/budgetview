package org.designup.picsou.utils;

import org.designup.picsou.gui.transactions.split.TransactionSplitComparator;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class TransactionComparator implements Comparator<Glob> {

  public static final TransactionComparator ASCENDING =
    new TransactionComparator(true, Transaction.MONTH, Transaction.DAY);
  public static final TransactionComparator DESCENDING =
    new TransactionComparator(false, Transaction.MONTH, Transaction.DAY);

  public static final TransactionComparator ASCENDING_BANK =
    new TransactionComparator(true, Transaction.BANK_MONTH, Transaction.BANK_DAY);
  public static final TransactionComparator DESCENDING_BANK =
    new TransactionComparator(false, Transaction.BANK_MONTH, Transaction.BANK_DAY);

  private int dateMultiplier = 1;
  private TransactionSplitComparator splitComparator = new TransactionSplitComparator();
  protected IntegerField monthField;
  protected IntegerField dayField;

  public TransactionComparator(boolean ascendingDates, IntegerField monthField, IntegerField dayField) {
    this.dateMultiplier = ascendingDates ? 1 : -1;
    this.monthField = monthField;
    this.dayField = dayField;
  }

  public int compare(Glob transaction1, Glob transaction2) {
    long dateDiff = (transaction1.get(monthField) - transaction2.get(monthField)) * dateMultiplier;
    if (dateDiff != 0) {
      return (int)dateDiff;
    }
    int dayDiff = (transaction1.get(dayField) - transaction2.get(dayField)) * dateMultiplier;
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
