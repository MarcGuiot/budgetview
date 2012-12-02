package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ReconciliationCustomizer implements LabelCustomizer {

  private Icon icon;
  private Directory directory;

  public ReconciliationCustomizer(Directory directory) {
    this.directory = directory;
  }

  public void process(JLabel label, Glob transaction, boolean isSelected, boolean hasFocus, int row, int column) {
    if (transaction.isTrue(Transaction.TO_RECONCILE)) {
      label.setIcon(getIcon());
    }
    else {
      label.setIcon(null);
    }
  }

  private Icon getIcon() {
    if (icon == null) {
      icon = directory.get(ImageLocator.class).get("to_reconcile.png");
    }
    return icon;
  }
}
