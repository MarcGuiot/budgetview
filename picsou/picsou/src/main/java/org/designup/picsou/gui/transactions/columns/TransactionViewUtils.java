package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.gui.components.PicsouTableHeaderCustomizer;
import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.transactions.categorization.CategoryChooserAction;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class TransactionViewUtils {
  private TransactionViewUtils() {
  }

  public static void installKeyboardCategorization(final JTable table, CategoryChooserAction categoryChooserAction,
                                                   int noteColumnIndex) {
    table.addKeyListener(new TransactionKeyListener(table, categoryChooserAction, noteColumnIndex));
  }

  public static void configureHeader(GlobTableView view, Directory directory) {
    view.setHeaderCustomizer(new PicsouTableHeaderCustomizer(directory, PicsouColors.TRANSACTION_TABLE_HEADER_TITLE),
                             new PicsouTableHeaderPainter(directory,
                                                          PicsouColors.TRANSACTION_TABLE_HEADER_DARK,
                                                          PicsouColors.TRANSACTION_TABLE_HEADER_MEDIUM,
                                                          PicsouColors.TRANSACTION_TABLE_HEADER_LIGHT,
                                                          PicsouColors.TRANSACTION_TABLE_HEADER_BORDER));
  }
}
