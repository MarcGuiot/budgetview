package com.budgetview.desktop.categorization.components;

import com.budgetview.desktop.transactions.columns.TransactionRendererColors;
import com.budgetview.model.Transaction;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;

import javax.swing.*;

public class TransactionAmountCustomizer implements LabelCustomizer {
  private TransactionRendererColors colors;

  public TransactionAmountCustomizer(TransactionRendererColors colors) {
    this.colors = colors;
  }

  public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
    if (transaction == null || !transaction.exists()) {
      return;
    }
    colors.setForeground(label, isSelected, transaction,
                         TransactionRendererColors.getMode(transaction.get(Transaction.AMOUNT)),
                         false);
  }
}
