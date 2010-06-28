package org.designup.picsou.gui.importer.edition;

import org.designup.picsou.bank.specific.AbstractBankPlugin;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ChooseOrCreateAccountDialog {
  private LocalGlobRepository localRepository;
  private GlobRepository parentSessionRepository;
  private Directory directory;
  private PicsouDialog dialog;
  private GlobsPanelBuilder builder;
  private GlobList accountToAccounts = new GlobList();

  public ChooseOrCreateAccountDialog(GlobRepository repository, Directory directory) {
    parentSessionRepository = repository;
    this.directory = directory;
    localRepository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE)
      .get();
  }


  private void createComponent(GlobList newAccounts) {
    int id = 0;
    for (Glob account : newAccounts) {
      this.accountToAccounts.add(
        localRepository.create(Key.create(AccountToAccountType.TYPE, id),
                               value(AccountToAccountType.FROM_ACCOUNT, account.get(Account.ID))));
      id++;
    }
    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/chooseOrCreateAccountDialog.splits",
                                    localRepository, directory);

    builder.addRepeat("repeat",
                      accountToAccounts,
                      new RepeatComponentFactory<Glob>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, final Glob accountToAccount) {
                          GlobLabelView globLabelView =
                            GlobLabelView.init(Account.TYPE, localRepository, directory)
                              .forceSelection(
                                localRepository.findLinkTarget(accountToAccount,
                                                               AccountToAccountType.FROM_ACCOUNT).getKey());
                          cellBuilder.add("accounts", globLabelView.getComponent());

                          final GlobLinkComboEditor linkComboEditor = GlobLinkComboEditor.init(AccountToAccountType.TO_ACCOUNT, localRepository, directory)
                            .setEmptyOptionLabel(Lang.get("chooseOrCreate.create"))
                            .setFilter(filterAccounts(accountToAccount))
                            .forceSelection(accountToAccount.getKey());
                          // on force un setFilter pour que la combo soit mise a jour
                          // ==> le filtre est trop particulier pour la mecanique generique
                          localRepository.addChangeListener(new DefaultChangeSetListener() {
                            public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
                              if (changeSet.containsUpdates(AccountToAccountType.TO_ACCOUNT)) {
                                linkComboEditor.setFilter(filterAccounts(accountToAccount));
                              }
                            }
                          });
                          cellBuilder.add("accountsCombo", linkComboEditor.getComponent());
                        }
                      });

  }

  private GlobMatcher filterAccounts(final Glob currentAccountsToAccount) {
    return GlobMatchers.and(
      GlobMatchers.fieldEquals(Account.IS_IMPORTED_ACCOUNT, Boolean.FALSE),
      GlobMatchers.not(GlobMatchers.fieldIn(Account.ID, Account.SUMMARY_ACCOUNT_IDS)),
      new GlobMatcher() {
        public boolean matches(Glob item, GlobRepository repository) {
          for (Glob account : accountToAccounts) {
            if (account != currentAccountsToAccount) {
              if (account.get(AccountToAccountType.TO_ACCOUNT) != null &&
                  account.get(AccountToAccountType.TO_ACCOUNT).equals(item.get(Account.ID))) {
                return false;
              }
            }
          }
          return true;
        }
      }
    );
  }

  public void show(Window parent, GlobList newAccounts) {
    createComponent(newAccounts);
    dialog = PicsouDialog.createWithButtons(parent, directory,
                                            builder.<JPanel>load(),
                                            new ValidateAction(),
                                            new CancelAction()
    );
    dialog.pack();
    dialog.showCentered();
    builder.dispose();
  }

  private class ValidateAction extends AbstractAction {

    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
      for (Glob account : accountToAccounts) {
        Integer toAccount = account.get(AccountToAccountType.TO_ACCOUNT);
        if (toAccount != null) {
          Glob newAccount = localRepository.findLinkTarget(account, AccountToAccountType.FROM_ACCOUNT);
          Glob existingAccount = localRepository.findLinkTarget(account, AccountToAccountType.TO_ACCOUNT);
          localRepository.update(existingAccount.getKey(),
                                 Account.NUMBER, newAccount.get(Account.NUMBER));
          if (newAccount.get(Account.POSITION) != null) {
            localRepository.update(existingAccount.getKey(),
                                   value(Account.POSITION_DATE, newAccount.get(Account.POSITION_DATE)),
                                   value(Account.POSITION, newAccount.get(Account.POSITION)),
                                   value(Account.TRANSACTION_ID, null));
          }
          AbstractBankPlugin.updateImportedTransaction(parentSessionRepository, newAccount, existingAccount);
        }
        else {
          Glob newAccount = localRepository.findLinkTarget(account, AccountToAccountType.FROM_ACCOUNT);
          localRepository.update(newAccount.getKey(), Account.IS_VALIDATED, Boolean.TRUE);
        }
      }
      localRepository.delete(accountToAccounts);
      localRepository.commitChanges(true);
    }
  }

  private class CancelAction extends AbstractAction {
    public CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}