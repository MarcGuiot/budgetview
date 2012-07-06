package org.designup.picsou.gui.transactions.utils;

import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.model.ReconciliationStatus;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
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
    if (transaction == null) {
      return;
    }

    if (isSelected) {
      label.setForeground(Color.WHITE);
    }
    else if (ReconciliationStatus.isToReconcile(transaction)) {
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
