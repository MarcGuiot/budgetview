package org.designup.picsou.functests.checkers.converters;

import org.designup.picsou.functests.checkers.TransactionChecker;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.TransactionType;
import org.globsframework.model.Glob;
import org.uispec4j.Button;
import org.uispec4j.TableCellValueConverter;

import javax.swing.*;
import java.awt.*;

public class SeriesCellConverter implements TableCellValueConverter {

  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {

    StringBuilder builder = new StringBuilder();
    Glob transaction = (Glob)modelObject;
    Integer transactionType = transaction.get(Transaction.TRANSACTION_TYPE);
    builder.append("(");
    builder.append(TransactionType.getType(transactionType).getName());
    builder.append(")");

    org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
    Button hyperlink = panel.getButton();
    String text = hyperlink.getLabel();
    if (text.equals(TransactionChecker.TO_CATEGORIZE)) {
      builder.append(TransactionChecker.TO_CATEGORIZE);
    }
    else {
      builder.append(text);
    }
    return builder.toString().trim();
  }
}
