package org.designup.picsou.gui.categorization.actions;

import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CategorizeTransactionsAction extends AbstractAction implements GlobSelectionListener {

  private SelectionService selectionService;
  private Directory directory;

  public CategorizeTransactionsAction(Directory directory) {
    super(Lang.get("transaction.categorize.action"));
    this.directory = directory;
    selectionService = directory.get(SelectionService.class);
  }

  public void selectionUpdated(GlobSelection selection) {
    setEnabled(!selection.getAll(Transaction.TYPE).isEmpty());
  }

  public void actionPerformed(ActionEvent actionEvent) {
    NavigationService navigation = directory.get(NavigationService.class);
    navigation.gotoCategorization(selectionService.getSelection(Transaction.TYPE), false);
  }
}
