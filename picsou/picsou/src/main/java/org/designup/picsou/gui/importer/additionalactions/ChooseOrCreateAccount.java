package org.designup.picsou.gui.importer.additionalactions;

import org.designup.picsou.gui.importer.AdditionalImportAction;
import org.designup.picsou.gui.importer.edition.ChooseOrCreateAccountDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChooseOrCreateAccount implements AdditionalImportAction {
  private Window parent;
  private GlobRepository repository;
  private Directory directory;
  private GlobList accounts;

  public ChooseOrCreateAccount(Window parent, GlobRepository repository, Directory directory) {
    this.parent = parent;
    this.repository = repository;
    this.directory = directory;
  }

  public boolean shouldApplyAction() {
    accounts = repository.getAll(Account.TYPE, isFalse(Account.IS_VALIDADED));
    boolean hasNotImportedAccount = hasNotImported(repository);
    if (!accounts.isEmpty() && !hasNotImportedAccount){
      for (Glob account : accounts) {
        repository.update(account.getKey(), Account.IS_VALIDADED, true);
      }
    }
    return !accounts.isEmpty() && hasNotImportedAccount;
  }

  public static boolean hasNotImported(final GlobRepository repository) {
    GlobList nonImportedAccounts =
      repository.getAll(Account.TYPE)
        .filterSelf(
          and(fieldEquals(Account.IS_IMPORTED_ACCOUNT, Boolean.FALSE),
              not(fieldIn(Account.ID, Account.SUMMARY_ACCOUNT_IDS))),
          repository);
    return !nonImportedAccounts.isEmpty();
  }

  public String getMessage() {
    return Lang.get("import.chooseOrCreate.message");
  }

  public String getButtonMessage() {
    return Lang.get("import.chooseOrCreate.button");
  }

  public Action getAction() {
    return new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        ChooseOrCreateAccountDialog dialog = new ChooseOrCreateAccountDialog(repository, directory);
        dialog.show(parent, accounts);
      }
    };
  }
}
