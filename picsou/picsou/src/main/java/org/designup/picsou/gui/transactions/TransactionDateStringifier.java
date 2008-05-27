package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.utils.AbstractGlobStringifier;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;

import java.util.Comparator;

public class TransactionDateStringifier extends AbstractGlobStringifier {

  public TransactionDateStringifier(Comparator<Glob> comparator) {
    super(comparator);
  }

  public String toString(Glob transaction, GlobRepository globRepository) {
    int yearMonth = transaction.get(Transaction.MONTH);
    int month = Month.toMonth(yearMonth);
    int day = transaction.get(Transaction.DAY);
    return toString(month, day);
  }

  public static String toString(int month, int day) {
    return (day < 10 ? "0" : "") + day +
           "/" + (month < 10 ? "0" : "") + month;
  }
}
