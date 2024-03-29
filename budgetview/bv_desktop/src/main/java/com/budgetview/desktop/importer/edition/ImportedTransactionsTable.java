package com.budgetview.desktop.importer.edition;

import com.budgetview.desktop.components.table.TableHeaderPainter;
import com.budgetview.desktop.components.table.TransactionTableHeaderPainter;
import com.budgetview.desktop.utils.ApplicationColors;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.ImportedTransaction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;

import static org.globsframework.gui.views.utils.LabelCustomizers.chain;
import static org.globsframework.gui.views.utils.LabelCustomizers.fontSize;
import static org.globsframework.model.utils.GlobMatchers.isNotTrue;

public class ImportedTransactionsTable {
  private JTable transactionTable;

  private static final int[] COLUMN_SIZES = {10, 45};
  private TableHeaderPainter headerPainter;

  public ImportedTransactionsTable(GlobRepository repository, DefaultDirectory directory,
                                   ImportedTransactionDateRenderer dateRenderer) {

    GlobTableView tableView = GlobTableView.init(ImportedTransaction.TYPE, repository,
                                                 dateRenderer.getComparator(), directory)
      .setFilter(isNotTrue(ImportedTransaction.DELETED))
      .addColumn(Lang.get("import.bankDate"), ImportedTransaction.BANK_DATE,
                 chain(fontSize(9), dateRenderer))
      .addColumn(Lang.get("label"), new ImportedTransactionLabelStringifier(), LabelCustomizers.autoTooltip())
      .addColumn(Lang.get("amount"), ImportedTransaction.AMOUNT);

    headerPainter = TransactionTableHeaderPainter.install(tableView, directory);

    transactionTable = tableView.getComponent();
    dateRenderer.setTable(tableView);
    ApplicationColors.setSelectionColors(transactionTable, directory);
    Gui.setColumnSizes(transactionTable, COLUMN_SIZES);
  }

  public JTable getTable() {
    return transactionTable;
  }

  public void dispose() {
    headerPainter.dispose();
  }
}
