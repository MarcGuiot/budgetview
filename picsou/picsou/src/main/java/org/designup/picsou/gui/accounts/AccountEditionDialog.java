package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AccountEditionDialog {
  private PicsouDialog dialog;
  private AccountEditionPanel accountEditionPanel;
  private LocalGlobRepository localRepository;
  private Glob currentAccount;
  private Window owner;
  private GlobRepository parentRepository;
  private Directory directory;

  public AccountEditionDialog(Window owner, GlobRepository parentRepository, Directory directory) {
    this.owner = owner;
    this.parentRepository = parentRepository;
    this.directory = directory;

    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(Bank.TYPE, BankEntity.TYPE)
      .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountEditionDialog.splits",
                                                      localRepository, directory);

    JLabel messageLabel = builder.add("message", new JLabel());

    accountEditionPanel = new AccountEditionPanel(localRepository, directory, messageLabel);
    builder.add("panel", accountEditionPanel.getPanel());

    dialog = PicsouDialog.create(owner, directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new OkAction(), new CancelAction(),
                               new DeleteAction());
  }

  public void show(Glob account) {
    localRepository.reset(new GlobList(account), Account.TYPE);
    accountEditionPanel.setBalanceEditorVisible(false);
    doShow(localRepository.get(account.getKey()));
  }

  public void showWithNewAccount(AccountType type) {
    doShow(localRepository.create(Account.TYPE, value(Account.ACCOUNT_TYPE, type.getId())));
  }

  private void doShow(Glob localAccount) {
    currentAccount = localAccount;
    accountEditionPanel.setAccount(localAccount);
    dialog.pack();
    GuiUtils.showCentered(dialog);
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!accountEditionPanel.check()) {
        return;
      }
      try {
        localRepository.commitChanges(true);
//        directory.get(SelectionService.class).select(currentAccount);
      }
      finally {
        dialog.setVisible(false);
      }
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

  private class DeleteAction extends AbstractAction {
    private GlobMatcher transactionMatcher;
    private GlobMatcher seriesMatcher;

    private DeleteAction() {
      super(Lang.get("accountEdition.delete"));
    }

    public void actionPerformed(ActionEvent e) {
      transactionMatcher = linkedTo(currentAccount, Transaction.ACCOUNT);
      seriesMatcher = or(linkedTo(currentAccount, Series.FROM_ACCOUNT),
                         linkedTo(currentAccount, Series.TO_ACCOUNT));

      ConfirmationDialog confirmDialog = new ConfirmationDialog("accountDeletion.confirm.title",
                                                                getMessageKey(), owner, directory) {
        protected void postValidate() {
          try {
            parentRepository.startChangeSet();
            parentRepository.delete(currentAccount.getKey());
            parentRepository.delete(parentRepository.getAll(Series.TYPE, seriesMatcher));
            parentRepository.delete(parentRepository.getAll(Transaction.TYPE, transactionMatcher));
          }
          finally {
            parentRepository.completeChangeSet();
          }
          dialog.setVisible(false);
        }
      };
      confirmDialog.show();
    }

    private String getMessageKey() {

      boolean hasTransactions = parentRepository.contains(Transaction.TYPE, transactionMatcher);
      boolean hasSeries = parentRepository.contains(Series.TYPE, seriesMatcher);

      if (hasTransactions && hasSeries) {
        return "accountDeletion.confirm.all";
      }
      if (hasTransactions) {
        return "accountDeletion.confirm.transactions";
      }
      if (hasSeries) {
        return "accountDeletion.confirm.series";
      }
      return "accountDeletion.confirm.unused";
    }
  }
}
