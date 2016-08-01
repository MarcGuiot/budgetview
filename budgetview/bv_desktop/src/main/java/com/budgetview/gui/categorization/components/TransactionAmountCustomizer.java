package com.budgetview.gui.categorization.components;

import com.budgetview.model.Transaction;
import com.budgetview.gui.transactions.columns.TransactionRendererColors;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;

import javax.swing.*;

public class TransactionAmountCustomizer implements LabelCustomizer {
  private TransactionRendererColors colors;

  public TransactionAmountCustomizer(TransactionRendererColors colors) {
    this.colors = colors;
  }

  public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
    if (transaction == null) {
      return;
    }
    colors.setForeground(label, isSelected, transaction,
                         TransactionRendererColors.getMode(transaction.get(Transaction.AMOUNT)),
                         false);
  }
}
