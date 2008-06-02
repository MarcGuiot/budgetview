package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.utils.AbstractGlobStringifier;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;

import java.util.Comparator;
import java.text.MessageFormat;
import java.text.FieldPosition;

public class TransactionDateStringifier extends AbstractGlobStringifier {
  private MessageFormat format;

  public TransactionDateStringifier(Comparator<Glob> comparator) {
    super(comparator);
    format = Lang.getFormat("transactionView.dateFormat");
  }

  public String toString(Glob transaction, GlobRepository globRepository) {
    int yearMonth = transaction.get(Transaction.MONTH);
    int year = Month.toYear(yearMonth);
    int month = Month.toMonth(yearMonth);
    int day = transaction.get(Transaction.DAY);
    return toString(year, month, day);
  }

  public String toString(int year, int month, int day) {
    return format.format(
      new Object[]{
        (day < 10 ? "0" : "") + day,
        (month < 10 ? "0" : "") + month,
        Integer.toString(year)
      });
  }
}
