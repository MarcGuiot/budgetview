package org.designup.picsou.gui.description.stringifiers;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.utils.AbstractGlobStringifier;

import java.util.Comparator;

public class TransactionDateStringifier extends AbstractGlobStringifier {
  protected IntegerField monthField;
  protected IntegerField dayField;

  public TransactionDateStringifier(Comparator<Glob> comparator, IntegerField monthField, IntegerField dayField) {
    super(comparator);
    this.monthField = monthField;
    this.dayField = dayField;
  }

  public TransactionDateStringifier(Comparator<Glob> comparator) {
    this(comparator, Transaction.MONTH, Transaction.DAY);
  }

  public String toString(Glob transaction, GlobRepository globRepository) {
    if (transaction == null){
      return null;
    }
    int yearMonth = transaction.get(monthField);
    int year = Month.toYear(yearMonth);
    int month = Month.toMonth(yearMonth);
    int day = transaction.get(dayField);
    return toString(year, month, day);
  }

  public static String toString(int year, int month, int day) {
    return Formatting.toString(year, month, day);
  }
}
