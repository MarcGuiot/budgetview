package com.budgetview.gui.accounts.utils;

import com.budgetview.gui.accounts.AccountEditionDialog;
import com.budgetview.gui.components.dialogs.ConfirmationDialog;
import com.budgetview.model.Account;
import com.budgetview.model.AccountType;
import com.budgetview.model.AccountUpdateMode;
import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AccountCreation {
  public static boolean containsUserAccount(final GlobRepository repository, final Directory directory, final String message) {
    if (repository.contains(Account.TYPE, AccountMatchers.userCreatedMainAccounts())) {
      return true;
    }

    final JFrame frame = directory.get(JFrame.class);
    ConfirmationDialog dialog = new ConfirmationDialog("accountCreation.title",
                                                       message,
                                                       frame, directory,
                                                       ConfirmationDialog.Mode.EXPANDED) {

      protected String getOkButtonText() {
        return Lang.get("accountCreation.okButton");
      }

      protected void processOk() {
        AccountEditionDialog accountEdition = new AccountEditionDialog(frame, repository, directory, true);
        accountEdition.showWithNewAccount(AccountType.MAIN, true, AccountUpdateMode.MANUAL);
      }
    };
    dialog.show();
    return repository.contains(Account.TYPE, AccountMatchers.userCreatedMainAccounts());
  }
}
