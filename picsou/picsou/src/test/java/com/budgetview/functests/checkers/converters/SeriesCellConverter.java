package com.budgetview.functests.checkers.converters;

import com.budgetview.functests.checkers.TransactionChecker;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionType;
import org.globsframework.model.Glob;
import org.uispec4j.Button;
import org.uispec4j.TableCellValueConverter;

import javax.swing.*;
import java.awt.*;

public class SeriesCellConverter implements TableCellValueConverter {
  private boolean withTransactionType;

  public SeriesCellConverter(boolean withTransactionType) {
    this.withTransactionType = withTransactionType;
  }

  public Object getValue(int row, int column, Component renderedComponent, Object modelObject) {

    StringBuilder builder = new StringBuilder();
    if (withTransactionType) {
      Glob transaction = (Glob)modelObject;
      Integer transactionType = transaction.get(Transaction.TRANSACTION_TYPE);
      builder.append("(");
      builder.append(TransactionType.getType(transactionType).getName());
      builder.append(")");
    }

    builder.append(extractSeries(renderedComponent));
    return builder.toString().trim();
  }

  public static String extractSeries(Component renderedComponent) {
    org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)renderedComponent);
    Button hyperlink = panel.getButton();
    String text = hyperlink.getLabel();
    if (text.equals(TransactionChecker.TO_CATEGORIZE)) {
      return TransactionChecker.TO_CATEGORIZE;
    }
    else {
      return text;
    }
  }
}
