package org.designup.picsou.utils;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class TransactionComparator implements Comparator<Glob> {

  public static final TransactionComparator ASCENDING =
    new TransactionComparator(true, Transaction.MONTH, Transaction.DAY, false);
  public static final TransactionComparator ASCENDING_SPLIT_AFTER =
    new TransactionComparator(true, Transaction.MONTH, Transaction.DAY, true);
  public static final TransactionComparator DESCENDING_SPLIT_AFTER =
    new TransactionComparator(false, Transaction.MONTH, Transaction.DAY, true);


  public static final TransactionComparator ASCENDING_ACCOUNT =
    new TransactionComparator(true, Transaction.POSITION_MONTH, Transaction.POSITION_DAY, false) {
      int lastCompare(Glob transaction1, Glob transaction2) {
        Integer month1 = transaction1.get(Transaction.BANK_MONTH);
        Integer month2 = transaction2.get(Transaction.BANK_MONTH);
        if (month1 == null) {
          if (month2 == null) {
            month1 = transaction1.get(Transaction.MONTH);
            month2 = transaction2.get(Transaction.MONTH);
            int monthCompare = comparisonMultiplier * month1.compareTo(month2);
            if (monthCompare != 0){
              return monthCompare;
            }
            else {
              Integer day1 = transaction1.get(Transaction.DAY);
              Integer day2 = transaction2.get(Transaction.DAY);
              int dayCompare = comparisonMultiplier * day1.compareTo(day2);
              if (dayCompare != 0){
                return dayCompare;
              }
              else {
                return super.lastCompare(transaction1, transaction2);
              }
            }
          }
          else {
            return comparisonMultiplier;
          }
        }
        int monthCompare = comparisonMultiplier * month1.compareTo(month2);
        if (monthCompare != 0) {
          return monthCompare;
        }
        int bankDateCompare = comparisonMultiplier * transaction1.get(Transaction.BANK_DAY)
          .compareTo(transaction2.get(Transaction.BANK_DAY));
        if (bankDateCompare != 0) {
          return bankDateCompare;
        }
        return super.lastCompare(transaction1, transaction2);
      }
    };

  public static final TransactionComparator ASCENDING_BANK =
    new TransactionComparator(true, Transaction.BANK_MONTH, Transaction.BANK_DAY, false);
  public static final TransactionComparator ASCENDING_BANK_SPLIT_AFTER =
    new TransactionComparator(true, Transaction.BANK_MONTH, Transaction.BANK_DAY, true);
  public static final TransactionComparator DESCENDING_BANK_SPLIT_AFTER =
    new TransactionComparator(false, Transaction.BANK_MONTH, Transaction.BANK_DAY, true);

  protected int comparisonMultiplier;
  private int splitAfter;
  protected IntegerField monthField;
  protected IntegerField dayField;

  private TransactionComparator(boolean ascendingDates, IntegerField monthField,
                                IntegerField dayField, boolean splitAfter) {
    this.comparisonMultiplier = ascendingDates ? 1 : -1;
    if (ascendingDates) {
      this.splitAfter = splitAfter ? -1 : 1;
    }
    else {
      this.splitAfter = 1;
    }
    this.monthField = monthField;
    this.dayField = dayField;
  }

  public int compare(Glob transaction1, Glob transaction2) {
    int tmp;
    tmp = Utils.compare(transaction1.get(monthField), transaction2.get(monthField));
    if (tmp != 0) {
      return comparisonMultiplier * tmp;
    }
    final Integer day1 = transaction1.get(dayField);
    final Integer day2 = transaction2.get(dayField);
    tmp = Utils.compare(day1, day2);
    if (tmp != 0) {
      return comparisonMultiplier * tmp;
    }


    Integer tt1 = transaction1.get(Transaction.TRANSACTION_TYPE);
    Integer tt2 = transaction2.get(Transaction.TRANSACTION_TYPE);
    if (tt1 != null && tt1 == TransactionType.OPEN_ACCOUNT_EVENT.getId()){
      if (tt2 == null || tt2 != TransactionType.OPEN_ACCOUNT_EVENT.getId()){
        return -comparisonMultiplier;
      }
    }
    else if (tt2 != null && tt2 == TransactionType.OPEN_ACCOUNT_EVENT.getId()){
      return comparisonMultiplier;
    }

    if (tt1 != null && tt1 == TransactionType.CLOSE_ACCOUNT_EVENT.getId()){
      if (tt2 == null || tt2 != TransactionType.CLOSE_ACCOUNT_EVENT.getId()){
        return comparisonMultiplier;
      }
    }
    else if (tt2 != null && tt2 == TransactionType.CLOSE_ACCOUNT_EVENT.getId()){
      return -comparisonMultiplier;
    }

    if (!transaction1.get(Transaction.PLANNED).equals(transaction2.get(Transaction.PLANNED))) {
      if (transaction1.isTrue(Transaction.PLANNED)) {
        return comparisonMultiplier;
      }
      else {
        return -comparisonMultiplier;
      }
    }
    if (!transaction1.get(Transaction.TO_RECONCILE, Boolean.FALSE).equals(transaction2.get(Transaction.TO_RECONCILE, Boolean.FALSE))) {
      if (transaction1.get(Transaction.TO_RECONCILE, Boolean.FALSE)) {
        return comparisonMultiplier;
      }
      else {
        return -comparisonMultiplier;
      }
    }


    int accountCompare =
      Utils.compare(transaction1.get(Transaction.ACCOUNT), transaction2.get(Transaction.ACCOUNT));
    if (accountCompare != 0) {
      return comparisonMultiplier * accountCompare;
    }
    Integer source1 = transaction1.get(Transaction.SPLIT_SOURCE);
    Integer source2 = transaction2.get(Transaction.SPLIT_SOURCE);
    if (source1 != null) {
      if (source2 != null) {
        if (source1.equals(source2)) {
          return comparisonMultiplier * transaction1.get(Transaction.ID).compareTo(transaction2.get(Transaction.ID));
        }
        else {
          return comparisonMultiplier * source1.compareTo(source2);
        }
      }
      else {
        if (source1.equals(transaction2.get(Transaction.ID))) {
          return -comparisonMultiplier * splitAfter;
        }
        return comparisonMultiplier * source1.compareTo(transaction2.get(Transaction.ID));
      }
    }
    else if (source2 != null) {
      if (source2.equals(transaction1.get(Transaction.ID))) {
        return comparisonMultiplier * splitAfter;
      }
      return comparisonMultiplier * transaction1.get(Transaction.ID).compareTo(source2);
    }
    return lastCompare(transaction1, transaction2);
  }

  int lastCompare(Glob transaction1, Glob transaction2) {
    if (transaction1.isTrue(Transaction.PLANNED)) { // les deux sont des planned
      int amountCompareOnSameDay = comparisonMultiplier * Utils.compare(transaction2.get(Transaction.AMOUNT), transaction1.get(Transaction.AMOUNT));
      if (amountCompareOnSameDay != 0) {
        return amountCompareOnSameDay;
      }
    }
    return comparisonMultiplier * transaction1.get(Transaction.ID).compareTo(transaction2.get(Transaction.ID));
  }
}
