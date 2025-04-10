package com.budgetview.desktop.transactions.columns;

import com.budgetview.model.Transaction;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class SplitTransactionCustomizer implements LabelCustomizer {

  private Icon icon;
  private Directory directory;

  public SplitTransactionCustomizer(Directory directory) {
    this.directory = directory;
  }

  public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
    if (transaction.exists() && Transaction.isSplitTransaction(transaction)) {
      label.setIcon(getIcon());
    }
    else {
      label.setIcon(null);
    }
  }

  private Icon getIcon() {
    if (icon == null) {
      icon = directory.get(ImageLocator.class).get("split_on.png");
    }
    return icon;
  }
}
