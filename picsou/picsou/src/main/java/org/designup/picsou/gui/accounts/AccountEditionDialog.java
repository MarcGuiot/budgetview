package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.*;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;
import static org.globsframework.model.utils.GlobMatchers.or;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class AccountEditionDialog {
  private PicsouDialog dialog;
  private AccountEditionPanel accountEditionPanel;
  private LocalGlobRepository localRepository;
  private Glob currentAccount;
  private Window owner;
  private GlobRepository parentRepository;
  private Directory directory;

  public AccountEditionDialog(Window owner, final GlobRepository parentRepository, Directory directory) {
    this.owner = owner;
    this.parentRepository = parentRepository;
    this.directory = directory;

    this.localRepository = LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(Bank.TYPE, BankEntity.TYPE, AccountUpdateMode.TYPE)
      .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountEditionDialog.splits",
                                                      localRepository, directory);

    JLabel messageLabel = builder.add("message", new JLabel());

    accountEditionPanel = new AccountEditionPanel(localRepository, directory, messageLabel);
    builder.add("panel", accountEditionPanel.getPanel());

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsUpdates(Account.ACCOUNT_TYPE)) {
          Integer accountType = repository.get(currentAccount.getKey()).get(Account.ACCOUNT_TYPE);
          if (!accountType.equals(AccountType.SAVINGS.getId())) {
            accountEditionPanel.setMessageSavingsWarning(false);
            return;
          }
          GlobList transactions = parentRepository.getAll(Transaction.TYPE, new GlobMatcher() {
            public boolean matches(Glob item, GlobRepository repository) {
              if (!item.get(Transaction.ACCOUNT).equals(currentAccount.get(Account.ID))) {
                return false;
              }
              Glob series = repository.findLinkTarget(item, Transaction.SERIES);
              if (!series.get(Series.BUDGET_AREA).equals(BudgetArea.SAVINGS.getId())) {
                return true;
              }
              return !(series.get(Series.FROM_ACCOUNT).equals(currentAccount.get(Account.ID))
                       || series.get(Series.TO_ACCOUNT).equals(currentAccount.get(Account.ID)));
            }
          });
          accountEditionPanel.setMessageSavingsWarning(!transactions.isEmpty());
        }
      }
    });

    localRepository.addTrigger(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(Account.TYPE)) {
          Set<Key> keySet = changeSet.getUpdated(Account.UPDATE_MODE);
          keySet.addAll(changeSet.getCreated(Account.TYPE));
          for (Key key : keySet) {
            if (repository.get(key).get(Account.UPDATE_MODE).equals(AccountUpdateMode.MANUAL.getId())) {
              localRepository.update(key, Account.IS_IMPORTED_ACCOUNT, true);
            }
            else {
              localRepository.update(key, Account.IS_IMPORTED_ACCOUNT, false);
            }
          }
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    });

    dialog = PicsouDialog.create(owner, directory);
    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new OkAction(), new CancelAction(),
                               new DeleteAction());
  }

  public void show(Glob account) {
    localRepository.reset(new GlobList(account), Account.TYPE);
    accountEditionPanel.setBalanceEditorVisible(false);
    accountEditionPanel.setUpdateModeEditable(false);
    doShow(localRepository.get(account.getKey()));
  }

  public void showWithNewAccount(AccountType type, AccountUpdateMode updateMode, boolean updateModeEditable) {
    accountEditionPanel.setUpdateModeEditable(updateModeEditable);
    doShow(localRepository.create(Account.TYPE,
                                  value(Account.ACCOUNT_TYPE, type.getId()),
                                  value(Account.UPDATE_MODE, updateMode.getId())));
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
        ChangeSet currentChanges = localRepository.getCurrentChanges();
        currentChanges.safeVisit(Account.TYPE, new DefaultChangeSetVisitor() {
          public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
            if (values.contains(Account.ACCOUNT_TYPE)) {
              uncategorize(key, parentRepository);
            }
          }
        });
        Set<Key> keySet = currentChanges.getCreated(Account.TYPE);
        for (Key key : keySet) {
          localRepository.update(key, Account.POSITION_DATE, TimeService.getToday());
        }
        localRepository.commitChanges(true);
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

  private void uncategorize(Key account, GlobRepository repository) {
    GlobList transactions = repository.getAll(Transaction.TYPE, GlobMatchers.fieldEquals(Transaction.ACCOUNT,
                                                                                         account.get(Account.ID)));
    for (Glob transaction : transactions) {
      repository.update(transaction.getKey(),
                        FieldValue.value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                        FieldValue.value(Transaction.SUB_SERIES, null));
    }

  }

}
