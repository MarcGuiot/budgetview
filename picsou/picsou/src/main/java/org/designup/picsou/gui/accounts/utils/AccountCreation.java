package org.designup.picsou.gui.accounts.utils;

import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.AccountUpdateMode;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.designup.picsou.gui.utils.Matchers.userCreatedMainAccounts;

public class AccountCreation {
  public static boolean containsUserAccount(final GlobRepository repository, final Directory directory, final String message) {
    if (repository.contains(Account.TYPE, userCreatedMainAccounts())) {
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
    return repository.contains(Account.TYPE, userCreatedMainAccounts());
  }
}
