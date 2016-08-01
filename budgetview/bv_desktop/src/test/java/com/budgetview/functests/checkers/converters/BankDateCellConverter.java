package com.budgetview.functests.checkers.converters;

import com.budgetview.gui.description.stringifiers.TransactionDateStringifier;
import com.budgetview.model.Month;
import com.budgetview.model.Transaction;
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
    Glob transaction = (Glob) modelObject;
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
