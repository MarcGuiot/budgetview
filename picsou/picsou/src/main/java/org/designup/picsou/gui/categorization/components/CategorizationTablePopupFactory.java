package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.categorization.actions.CategorizationTableActions;
import org.designup.picsou.gui.printing.actions.PrintTransactionsAction;
import org.designup.picsou.gui.transactions.creation.TransactionCreationPanel;
import org.designup.picsou.gui.transactions.reconciliation.annotations.ShowReconciliationAction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CategorizationTablePopupFactory implements PopupMenuFactory {
  private final CategorizationTableActions actions;
  private Action showHideAction;
  private ShowReconciliationAction showReconciliationAction;
  private Action copyTableAction;
  private PrintTransactionsAction printTransactionsAction;

  public CategorizationTablePopupFactory(GlobTableView transactionTable,
                                         TransactionCreationPanel transactionCreation,
                                         CategorizationTableActions actions,
                                         GlobRepository repository,
                                         Directory directory) {
    this.actions = actions;
    this.showHideAction = transactionCreation.getShowHideAction();
    this.showReconciliationAction = new ShowReconciliationAction(repository, directory);
    this.copyTableAction = transactionTable.getCopyTableAction(Lang.get("copyTable"));
    this.printTransactionsAction = new PrintTransactionsAction(transactionTable, repository, directory);
  }

  public JPopupMenu createPopup() {
    JPopupMenu tableMenu = new JPopupMenu();
    actions.addPopupActions(tableMenu, false);
    tableMenu.addSeparator();
    tableMenu.add(showHideAction);
    tableMenu.add(showReconciliationAction);
    tableMenu.add(copyTableAction);
    tableMenu.add(printTransactionsAction);
    return tableMenu;
  }
}