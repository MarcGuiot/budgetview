package org.designup.picsou.utils;

import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;

import java.util.Comparator;

public class TransactionComparator implements Comparator<Glob> {

  public static final TransactionComparator ASCENDING =
    new TransactionComparator(true, Transaction.MONTH, Transaction.DAY, false);
  public static final TransactionComparator ASCENDING_SPLIT_AFTER =
    new TransactionComparator(true, Transaction.MONTH, Transaction.DAY, true);
  public static final TransactionComparator DESCENDING_SPLIT_AFTER =
    new TransactionComparator(false, Transaction.MONTH, Transaction.DAY, true);

  public static final TransactionComparator ASCENDING_BANK =
    new TransactionComparator(true, Transaction.BANK_MONTH, Transaction.BANK_DAY, false);
  public static final TransactionComparator ASCENDING_BANK_SPLIT_AFTER =
    new TransactionComparator(true, Transaction.BANK_MONTH, Transaction.BANK_DAY, true);
  public static final TransactionComparator DESCENDING_BANK_SPLIT_AFTER =
    new TransactionComparator(false, Transaction.BANK_MONTH, Transaction.BANK_DAY, true);

  private int dateMultiplier;
  private int splitAfter;
  protected IntegerField monthField;
  protected IntegerField dayField;

  public TransactionComparator(boolean ascendingDates, IntegerField monthField,
                               IntegerField dayField, boolean splitAfter) {
    this.dateMultiplier = ascendingDates ? 1 : -1;
    if (ascendingDates) {
      this.splitAfter = splitAfter ? -1 : 1;
    }
    else {
      this.splitAfter = 1;
    }
    this.monthField = monthField;
    this.dayField = dayField;
  }

  public int compare(Glob o1, Glob o2) {
    int tmp;
    tmp = o1.get(monthField).compareTo(o2.get(monthField));
    if (tmp != 0) {
      return dateMultiplier * tmp;
    }
    tmp = o1.get(dayField).compareTo(o2.get(dayField));
    if (tmp != 0) {
      return dateMultiplier * tmp;
    }
    Integer source1 = o1.get(Transaction.SPLIT_SOURCE);
    Integer source2 = o2.get(Transaction.SPLIT_SOURCE);
    if (source1 != null) {
      if (source2 != null) {
        if (source1.equals(source2)) {
          return dateMultiplier * o1.get(Transaction.ID).compareTo(o2.get(Transaction.ID));
        }
        else {
          return dateMultiplier * source1.compareTo(source2);
        }
      }
      else {
        if (source1.equals(o2.get(Transaction.ID))) {
          return -dateMultiplier * splitAfter;
        }
        return dateMultiplier * source1.compareTo(o2.get(Transaction.ID));
      }
    }
    else if (source2 != null) {
      if (source2.equals(o1.get(Transaction.ID))) {
        return dateMultiplier * splitAfter;
      }
      return dateMultiplier * o1.get(Transaction.ID).compareTo(source2);
    }
    if (!o1.get(Transaction.PLANNED).equals(o2.get(Transaction.PLANNED))) {
      if (o1.get(Transaction.PLANNED)) {
        return dateMultiplier;
      }
      else {
        return -dateMultiplier;
      }
    }
    return dateMultiplier * o1.get(Transaction.ID).compareTo(o2.get(Transaction.ID));
  }
}
