package org.designup.picsou.gui.transactions.columns;

import javax.swing.*;

public class TransactionViewUtils {
  private TransactionViewUtils() {
  }

  public static void installKeyboardCategorization(final JTable table,
                                                   Action categoryChooserAction,
                                                   int noteColumnIndex) {
    table.addKeyListener(new TransactionKeyListener(table, categoryChooserAction, noteColumnIndex));
  }
}
