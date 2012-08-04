package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.transactions.columns.TransactionRendererColors;
import org.designup.picsou.model.Transaction;
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
