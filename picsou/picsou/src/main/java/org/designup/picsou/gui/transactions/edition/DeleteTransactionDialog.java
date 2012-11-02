package org.designup.picsou.gui.transactions.edition;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountPositionMode;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DeleteTransactionDialog {
  private PicsouDialog dialog;
  private GlobList transactions;
  private GlobRepository repository;
  private Directory directory;
  private JComboBox shouldUpdateAccountCombo;

  public DeleteTransactionDialog(GlobList transactions,
                                 GlobRepository repository,
                                 Directory directory) {
    this.transactions = transactions;
    this.repository = repository;
    this.directory = directory;
    createDialog(directory.get(JFrame.class));
  }

  private void createDialog(JFrame owner) {
    dialog = PicsouDialog.create(owner, true, directory);
    OkAction okAction = new OkAction();

    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/transactions/deleteTransactionDialog.splits",
                            repository, directory);


    JEditorPane editorPane = new JEditorPane("text/html", Lang.get(getContentKey(transactions)));

    builder.add("message", editorPane);

    shouldUpdateAccountCombo = new JComboBox();
    builder.add("impactAccountPosition", shouldUpdateAccountCombo);
    shouldUpdateAccountCombo.addItem(Lang.get("transactionCreation.updateAccount.yes"));
    shouldUpdateAccountCombo.addItem(Lang.get("transactionCreation.updateAccount.no"));

    dialog.addPanelWithButtons(builder.<JPanel>load(), okAction, new CancelAction());
    dialog.pack();
  }

  public final void show() {
    dialog.showCentered();
  }

  private static String getContentKey(GlobList transactions) {
    int totalCount = transactions.size();
    int splitCount = getSplitCount(transactions);
    if (splitCount == 0) {
      if (transactions.size() == 1) {
        return "transaction.delete.default.single";
      }
      else {
        return "transaction.delete.default.multi";
      }
    }
    else if (splitCount < totalCount) {
      return "transaction.delete.split.mixed";
    }
    else {
      if (transactions.size() == 1) {
        return "transaction.delete.split.single";
      }
      else {
        return "transaction.delete.split.multi";
      }
    }

  }

  private static int getSplitCount(GlobList transactions) {
    int result = 0;
    for (Glob transaction : transactions) {
      if (transaction.get(Transaction.SPLIT_SOURCE) != null) {
        result++;
      }
    }
    return result;
  }

  private class OkAction extends AbstractAction {

    private OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      postValidate();
      dialog.setVisible(false);
    }
  }

  protected void postValidate() {
    try {
      repository.startChangeSet();
      repository.create(AccountPositionMode.TYPE,
                        FieldValue.value(AccountPositionMode.UPDATE_ACCOUNT_POSITION,
                                         shouldUpdateAccountCombo.getSelectedItem().equals(Lang.get("transactionCreation.updateAccount.yes"))));

      while (!transactions.isEmpty()) {
        Glob toDelete = transactions.remove(0);

        if (Transaction.isSplitSource(toDelete)) {
          GlobList parts = repository.findLinkedTo(toDelete, Transaction.SPLIT_SOURCE);
          repository.delete(parts);
          transactions.removeAll(parts);
        }

        if (Transaction.isSplitPart(toDelete)) {
          Glob source = repository.findLinkTarget(toDelete, Transaction.SPLIT_SOURCE);
          repository.update(source.getKey(), Transaction.AMOUNT,
                            source.get(Transaction.AMOUNT) + toDelete.get(Transaction.AMOUNT));
        }

        Glob account = repository.findLinkTarget(toDelete, Transaction.ACCOUNT);
        if (account != null && toDelete.get(Transaction.ID).equals(account.get(Account.TRANSACTION_ID))) {
          repository.update(account.getKey(), Account.TRANSACTION_ID, null);
        }
        repository.delete(toDelete.getKey());
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private class CancelAction extends AbstractAction {

    private CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      dialog.setVisible(false);
    }
  }

}
