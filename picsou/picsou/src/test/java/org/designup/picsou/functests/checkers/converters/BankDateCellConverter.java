package org.designup.picsou.functests.checkers.converters;

import org.uispec4j.TableCellValueConverter;
import org.globsframework.model.Glob;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Month;

import java.awt.*;

public class BankDateCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
    Glob transaction = (Glob)modelObject;
    int yearMonth = transaction.get(Transaction.BANK_MONTH);
    int year = Month.toYear(yearMonth);
    int month = Month.toMonth(yearMonth);
    int day = transaction.get(Transaction.BANK_DAY);
    return (day < 10 ? "0" : "") + day +
           "/" + (month < 10 ? "0" : "") + month +
           "/" + year;
  }
}
