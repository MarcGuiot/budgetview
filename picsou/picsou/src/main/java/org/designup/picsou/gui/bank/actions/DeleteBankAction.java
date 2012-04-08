package org.designup.picsou.gui.bank.actions;

import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.BankEntity;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.actions.SingleSelectionAction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.Collections;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class DeleteBankAction extends SingleSelectionAction {
  private Window owner;
  private Set<Integer> excludedAccountIds = Collections.EMPTY_SET;

  public DeleteBankAction(Window owner, GlobRepository repository, Directory directory) {
    super(Lang.get("bank.delete.action"), Bank.TYPE, isTrue(Bank.USER_CREATED), repository, directory);
    this.owner = owner;
  }

  public void setExcludedAccounts(Set<Integer> excludedAccountIds) {
    this.excludedAccountIds = excludedAccountIds;
  }

  protected void process(final Glob bank, final GlobRepository repository, Directory directory) {
    GlobList accounts = repository.findLinkedTo(bank, Account.BANK);
    accounts.filterSelf(not(fieldIn(Account.ID,  excludedAccountIds)), repository);
    if (accounts.size() > 0) {
      MessageDialog.show("bank.delete.title",
                         owner, directory,
                         "bank.delete.used", accounts.get(0).get(Account.NAME));
      return;
    }

    ConfirmationDialog confirmationDialog =
      new ConfirmationDialog("bank.delete.title", Lang.get("bank.delete.confirm"), owner, directory) {
        protected void postValidate() {
          try {
            repository.startChangeSet();
            repository.delete(BankEntity.TYPE, linkedTo(bank, BankEntity.BANK));
            repository.delete(bank.getKey());
          }
          finally {
            repository.completeChangeSet();
          }
        }
      };
    confirmationDialog.show();
  }
}
