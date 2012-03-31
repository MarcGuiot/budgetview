package org.designup.picsou.functests.checkers.converters;

import org.designup.picsou.gui.description.stringifiers.TransactionDateStringifier;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.uispec4j.TableCellValueConverter;

import java.awt.*;

public class BankDateCellConverter implements TableCellValueConverter {

  private TransactionDateStringifier stringifier;
  private boolean useDisplayedDates;

  public BankDateCellConverter(boolean useDisplayedDates) {
    this.useDisplayedDates = useDisplayedDates;
    this.
      stringifier = new TransactionDateStringifier(null);
  }

  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
    Glob transaction = (Glob)modelObject;
    if (useDisplayedDates) {
      return stringifier.toString(transaction, null);
    }

    int yearMonth = transaction.get(Transaction.BANK_MONTH);
    int year = Month.toYear(yearMonth);
    int month = Month.toMonth(yearMonth);
    int day = transaction.get(Transaction.BANK_DAY);
    return (day < 10 ? "0" : "") + day +
           "/" + (month < 10 ? "0" : "") + month +
           "/" + year;
  }
}
