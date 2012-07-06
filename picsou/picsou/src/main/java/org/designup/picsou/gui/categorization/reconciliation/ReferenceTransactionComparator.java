package org.designup.picsou.gui.categorization.reconciliation;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class ReferenceTransactionComparator implements Comparator<Glob> {

  private String label;
  private Integer month;
  private Integer day;
  private Double amount;

  public void setCurrent(Glob transaction) {
    this.amount = transaction.get(Transaction.AMOUNT);
    this.label = transaction.get(Transaction.LABEL);
    this.month = transaction.get(Transaction.POSITION_MONTH);
    this.day = transaction.get(Transaction.POSITION_DAY);
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

    if (amount == null) {
      return Utils.compare(transaction1.get(Transaction.ID), transaction2.get(Transaction.ID));
    }

    double amountDistance1 = getAmountDistance(transaction1);
    double amountDistance2 = getAmountDistance(transaction2);
    if (amountDistance1 < amountDistance2) {
      return -1;
    }
    else if (amountDistance1 > amountDistance2) {
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

    String transactionLabel1 = transaction1.get(Transaction.LABEL);
    if (label.equalsIgnoreCase(transactionLabel1)) {
      return -1;
    }
    String transactionLabel2 = transaction1.get(Transaction.LABEL);
    if (label.equalsIgnoreCase(transactionLabel2)) {
      return 1;
    }

    return Utils.compare(transactionLabel1, transactionLabel2);
  }

  private double getAmountDistance(Glob transaction) {
    return Math.abs(amount - transaction.get(Transaction.AMOUNT));
  }

  public static int getDateDistance(Glob transaction1, Integer month, Integer day) {
    return Month.distance(month, day,
                          transaction1.get(Transaction.POSITION_MONTH),
                          transaction1.get(Transaction.POSITION_DAY));
  }
}
