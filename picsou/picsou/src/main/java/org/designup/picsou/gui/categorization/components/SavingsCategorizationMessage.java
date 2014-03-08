package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.categorization.components.messages.DynamicMessage;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SavingsCategorizationMessage extends DynamicMessage implements GlobSelectionListener, ChangeSetListener {
  private Set<Integer> accountIds;
  private GlobList transactions;

  public SavingsCategorizationMessage(GlobRepository repository, Directory directory) {
    super(repository, directory);
    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
    repository.addChangeListener(this);
  }

  public void selectionUpdated(GlobSelection selection) {
    update();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsUpdates(Transaction.SERIES) ||
        changeSet.containsUpdates(Account.ACCOUNT_TYPE)) {
      update();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Transaction.TYPE) ||
        changedTypes.contains(Account.TYPE)) {
      update();
    }
  }

  private void update() {
    transactions = directory.get(SelectionService.class).getSelection(Transaction.TYPE);
    for (Glob account : transactions.getTargets(Transaction.ACCOUNT, repository)) {
      if (Account.isMain(account)) {
        setText("");
        transactions = null;
        accountIds = null;
        updateVisibility();
        return;
      }
    }

    accountIds = transactions.getValueSet(Transaction.ACCOUNT);
    updateText();
    updateVisibility();
  }

  private void updateText() {
    int transactionCount = transactions.size();
    if (transactionCount == 0) {
      setText("");
      return;
    }

    String transactionPart = transactionCount == 1 ? "singleTransaction" : "multiTransaction";
    String accountPart = accountIds.size() == 1 ? "singleAccount" : "multiAccount";
    setText(Lang.get("savingsCategorizationMessage." + transactionPart + "." + accountPart));
  }

  protected void processHyperlinkClick(String href) {
    if (href.equals("editAccount")) {
      if (accountIds.size() != 1) {
        return;
      }
      AccountEditionDialog dialog = new AccountEditionDialog(repository, directory, false);
      dialog.show(Key.create(Account.TYPE, accountIds.iterator().next()));
    }
  }

  protected boolean isVisible() {
    return accountIds != null;
  }
}
