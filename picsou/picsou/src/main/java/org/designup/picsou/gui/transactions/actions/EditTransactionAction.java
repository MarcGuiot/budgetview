package org.designup.picsou.gui.transactions.actions;

import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.transactions.edition.EditTransactionDialog;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.actions.MultiSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Set;

public class EditTransactionAction extends MultiSelectionAction {

  private EditTransactionDialog dialog;

  public EditTransactionAction(GlobRepository repository, Directory directory) {
    super(Lang.get("transaction.edition.action"), Transaction.TYPE, repository, directory);
  }

  protected void process(GlobList transactions, GlobRepository repository, Directory directory) {
    for (Glob transaction : transactions) {
      if (transaction.isTrue(Transaction.PLANNED)) {
        MessageDialog.show("transaction.edition.planned.title",
                           directory,
                           "transaction.edition.planned.message");
        return;
      }
    }

    Set<Glob> set = new HashSet<Glob>();
    set.addAll(transactions);
    for (Glob transaction : transactions) {
      set.addAll(repository.findLinkedTo(transaction, Transaction.NOT_IMPORTED_TRANSACTION));
    }

    if (dialog == null) {
      dialog = new EditTransactionDialog(repository, directory);
    }
    
    GlobList toModify = new GlobList();
    toModify.addAll(set);
    dialog.show(toModify);
  }
}