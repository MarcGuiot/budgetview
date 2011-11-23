package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.DatePicker;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.model.CurrentAccountInfo;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;

import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.*;
import static org.globsframework.model.utils.GlobMatchers.linkedTo;
import static org.globsframework.model.utils.GlobMatchers.or;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class AccountEditionDialog extends AbstractAccountPanel<LocalGlobRepository> {
  private PicsouDialog dialog;
  private GlobRepository parentRepository;
  private GlobLinkComboEditor updateModeCombo;
  private JLabel titleLabel;
  private GlobsPanelBuilder builder;
  private Glob accountInfo;

  public AccountEditionDialog(final GlobRepository parentRepository, Directory directory) {
    this(directory.get(JFrame.class), parentRepository, directory);
  }

  public AccountEditionDialog(Window owner, final GlobRepository parentRepository, Directory directory) {
    super(createLocalRepository(parentRepository), directory);
    this.parentRepository = parentRepository;

    dialog = PicsouDialog.create(owner, localDirectory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountEditionDialog.splits",
                                                      localRepository, localDirectory);

    titleLabel = builder.add("title", new JLabel("accountEditionDialog")).getComponent();

    updateModeCombo = builder.addComboEditor("updateMode", Account.UPDATE_MODE).setShowEmptyOption(false);

    DatePicker startDatePicker = new DatePicker(Account.OPEN_DATE, localRepository, localDirectory);
    builder.add("startDatePicker", startDatePicker.getComponent());

    builder.add("removeStartDate", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob account = currentAccount;
        if (account != null) {
          localRepository.update(account.getKey(), Account.OPEN_DATE, null);
        }
      }
    });

    DatePicker endDatePicker = new DatePicker(Account.CLOSED_DATE, localRepository, localDirectory);
    builder.add("endDatePicker", endDatePicker.getComponent());
    builder.add("removeEndDate", new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob account = currentAccount;
        if (account != null) {
          localRepository.update(account.getKey(), Account.CLOSED_DATE, null);
        }
      }
    });

    super.createComponents(builder, dialog);

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (currentAccount == null || !changeSet.containsChanges(Account.TYPE)) {
          return;
        }
        Glob account = repository.get(currentAccount.getKey());
        setWarning(account.get(Account.ACCOUNT_TYPE), account.get(Account.CARD_TYPE));
        if (changeSet.containsUpdates(Account.ACCOUNT_TYPE)) {
          Integer accountType = account.get(Account.ACCOUNT_TYPE);
          if (!AccountType.SAVINGS.getId().equals(accountType)) {
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
          setSavingsWarning(!transactions.isEmpty());
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

    this.builder = builder;
    dialog.addPanelWithButtons(this.builder.<JPanel>load(),
                               new OkAction(), new CancelAction(dialog),
                               new DeleteAction());
  }

  private static LocalGlobRepository createLocalRepository(GlobRepository parentRepository) {
    return LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(Bank.TYPE, BankEntity.TYPE, AccountUpdateMode.TYPE, MonthDay.TYPE, CurrentMonth.TYPE,
            AccountCardType.TYPE, AccountType.TYPE, Month.TYPE, DeferredCardDate.TYPE)
      .get();
  }

  public void show(Glob account) {
    GlobList globs = parentRepository.findByIndex(DeferredCardDate.ACCOUNT_AND_DATE, DeferredCardDate.ACCOUNT, account.get(Account.ID))
      .getGlobs();
    globs.add(account);
    localRepository.reset(globs, Account.TYPE, DeferredCardDate.TYPE);
    setBalanceEditorVisible(false);
    updateModeCombo.setEnabled(!accountHasTransactions(account));
    doShow(localRepository.get(account.getKey()), false);
  }

  public void showWithNewAccount(AccountType type, boolean accountTypeEditable,
                                 AccountUpdateMode updateMode, boolean updateModeEditable) {
    accountTypeCombo.setEnabled(accountTypeEditable);
    updateModeCombo.setEnabled(updateModeEditable);
    Glob newAccount = localRepository.create(Account.TYPE,
                                             value(Account.ACCOUNT_TYPE, type.getId()),
                                             value(Account.UPDATE_MODE, updateMode.getId()),
                                             value(Account.BANK, accountInfo != null ? accountInfo.get(CurrentAccountInfo.BANK) : null),
                                             value(Account.POSITION, accountInfo != null ? accountInfo.get(CurrentAccountInfo.POSITION) : null),
                                             value(Account.POSITION_DATE, accountInfo != null ? accountInfo.get(CurrentAccountInfo.POSITION_DATE) : null));
    doShow(newAccount, true);
    builder.dispose();
  }

  public void showWithNewAccount(FieldValue... defaultValues) {
    localRepository.reset(GlobList.EMPTY, Account.TYPE);

    FieldValues values = FieldValuesBuilder.init()
      .set(Account.NAME, Lang.get("account.default.current.name"))
      .set(defaultValues)
      .get();

    Glob account = localRepository.create(Account.TYPE, values.toArray());
    localRepository.update(account.getKey(),
                           value(Account.BANK, accountInfo != null ? accountInfo.get(CurrentAccountInfo.BANK) : null),
                           value(Account.POSITION, accountInfo != null ? accountInfo.get(CurrentAccountInfo.POSITION) : null),
                           value(Account.POSITION_DATE, accountInfo != null ? accountInfo.get(CurrentAccountInfo.POSITION_DATE) : null));
    setBalanceEditorVisible(false);
    updateModeCombo.setEnabled(!accountHasTransactions(account));
    doShow(localRepository.get(account.getKey()), true);
    builder.dispose();
  }

  private void doShow(Glob localAccount, boolean creation) {
    titleLabel.setText(Lang.get(creation ? "account.panel.title.creation" : "account.panel.title.edition"));
    setAccount(localAccount);
    dialog.pack();
    if (creation) {
      GuiUtils.selectAndRequestFocus(nameField.getComponent());
    }
    dialog.showCentered();
  }

  private boolean accountHasTransactions(Glob account) {
    return parentRepository.contains(Transaction.TYPE, GlobMatchers.linkedTo(account, Transaction.ACCOUNT));
  }

  public void setAccountInfo(Glob accountInfo) {
    this.accountInfo = accountInfo;
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      if (!check()) {
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
                                                                Lang.get(getMessageKey()),
                                                                dialog, localDirectory) {
        protected void postValidate() {
          try {
            parentRepository.startChangeSet();
            parentRepository.delete(currentAccount.getKey());
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
                        value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                        value(Transaction.SUB_SERIES, null));
    }
  }
}
