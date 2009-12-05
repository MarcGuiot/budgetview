package org.designup.picsou.gui.importer.edition;

import org.designup.picsou.gui.components.PicsouTableHeaderPainter;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.ImportedTransaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import static org.globsframework.gui.views.utils.LabelCustomizers.*;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;

public class ImportedTransactionsTable {
  private JTable transactionTable;

  private static final int[] COLUMN_SIZES = {10, 45};

  public ImportedTransactionsTable(GlobRepository sessionRepository, DefaultDirectory sessionDirectory, ImportedTransactionDateRenderer dateRenderer) {

    GlobTableView tableView = GlobTableView.init(ImportedTransaction.TYPE, sessionRepository,
                                                 dateRenderer.getComparator(), sessionDirectory)
      .addColumn(Lang.get("import.bankDate"), ImportedTransaction.BANK_DATE,
                 chain(fontSize(9), dateRenderer))
      .addColumn(Lang.get("label"), new TransactionLabelGlobStringifier(), LabelCustomizers.autoTooltip())
      .addColumn(Lang.get("amount"), ImportedTransaction.AMOUNT);

    PicsouTableHeaderPainter.install(tableView, sessionDirectory);

    transactionTable = tableView.getComponent();
    dateRenderer.setTable(tableView);
    ApplicationColors.setSelectionColors(transactionTable, sessionDirectory);
    Gui.setColumnSizes(transactionTable, COLUMN_SIZES);
  }

  public JTable getTable() {
    return transactionTable;
  }
}
