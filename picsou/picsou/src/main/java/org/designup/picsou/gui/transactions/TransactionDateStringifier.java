package org.designup.picsou.gui.transactions;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

import java.text.MessageFormat;
import java.util.Comparator;

public class TransactionDateStringifier extends AbstractGlobStringifier {
  private MessageFormat format;
  protected IntegerField monthField;
  protected IntegerField dayField;

  public TransactionDateStringifier(Comparator<Glob> comparator, IntegerField monthField, IntegerField dayField) {
    super(comparator);
    format = Lang.getFormat("transactionView.dateFormat");
    this.monthField = monthField;
    this.dayField = dayField;
  }

  public TransactionDateStringifier(Comparator<Glob> comparator) {
    this(comparator, Transaction.MONTH, Transaction.DAY);
  }

  public String toString(Glob transaction, GlobRepository globRepository) {
    int yearMonth = transaction.get(monthField);
    int year = Month.toYear(yearMonth);
    int month = Month.toMonth(yearMonth);
    int day = transaction.get(dayField);
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
