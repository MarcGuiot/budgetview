package com.budgetview.desktop.transactions.utils;

import com.budgetview.desktop.transactions.columns.TransactionRendererColors;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;

import javax.swing.*;
import java.awt.*;

public class TransactionLabelCustomizer implements LabelCustomizer {
  private TransactionRendererColors colors;

  public TransactionLabelCustomizer(TransactionRendererColors colors) {
    this.colors = colors;
  }

  public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
    if (transaction == null || !transaction.exists()) {
      return;
    }

    if (isSelected) {
      label.setForeground(Color.WHITE);
    }
    else if (Transaction.isToReconcile(transaction)) {
      label.setForeground(colors.getTransactionReconciliationColor());
    }
    else if (Series.UNCATEGORIZED_SERIES_ID.equals(transaction.get(Transaction.SERIES))) {
      label.setForeground(colors.getTransactionErrorTextColor());
    }
    else {
      label.setForeground(colors.getTransactionTextColor());
    }
    colors.setBackground(label, transaction, isSelected, row);
  }
}
