package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.components.PicsouTableHeaderCustomizer;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.utils.directory.Directory;

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
