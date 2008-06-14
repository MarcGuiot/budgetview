package org.designup.picsou.functests.checkers.converters;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.uispec4j.TableCellValueConverter;

import java.awt.*;

public class DateCellConverter implements TableCellValueConverter {
  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {
    Glob transaction = (Glob)modelObject;
    int yearMonth = transaction.get(Transaction.MONTH);
    int year = Month.toYear(yearMonth);
    int month = Month.toMonth(yearMonth);
    int day = transaction.get(Transaction.DAY);
    return (day < 10 ? "0" : "") + day +
           "/" + (month < 10 ? "0" : "") + month +
           "/" + year;
  }
}
