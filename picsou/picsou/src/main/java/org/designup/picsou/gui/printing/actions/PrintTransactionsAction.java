package org.designup.picsou.gui.printing.actions;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.printing.PrinterService;
import org.designup.picsou.gui.printing.transactions.TransactionsReport;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.OperationFailed;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.event.ActionEvent;

public class PrintTransactionsAction extends AbstractAction {

  private final GlobTableView tableView;
  private final GlobRepository repository;
  private final Directory directory;

  public PrintTransactionsAction(final GlobTableView tableView, GlobRepository repository, Directory directory) {
    super(Lang.get("print.transactions.menu"));
    this.tableView = tableView;
    this.repository = repository;
    this.directory = directory;
    tableView.getComponent().getModel().addTableModelListener(new TableModelListener() {
      public void tableChanged(TableModelEvent e) {
        setEnabled(!tableView.getGlobs().isEmpty());
      }
    });
  }

  public void actionPerformed(ActionEvent event) {
    try {
      TransactionsReport report = new TransactionsReport(tableView.getGlobs(), repository);
      directory.get(PrinterService.class).print(Lang.get("application"), report);
    }
    catch (OperationFailed e) {
      MessageDialog.show("print.completion.failed.title", MessageType.ERROR, directory,
                         "print.completion.failed.message", e.getMessage());
    }
  }
}
