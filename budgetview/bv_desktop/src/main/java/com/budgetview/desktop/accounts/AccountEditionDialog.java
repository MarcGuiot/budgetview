package com.budgetview.desktop.accounts;

import com.budgetview.desktop.accounts.utils.DeleteAccountHandler;
import com.budgetview.desktop.accounts.utils.MonthDay;
import com.budgetview.desktop.components.DatePicker;
import com.budgetview.desktop.components.dialogs.CancelAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.model.*;
import com.budgetview.shared.model.AccountType;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.layout.TabHandler;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.*;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.repository.LocalGlobRepositoryBuilder;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.DefaultChangeSetVisitor;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class AccountEditionDialog extends AbstractAccountPanel<LocalGlobRepository> {
  private PicsouDialog dialog;
  private GlobRepository parentRepository;
  private JLabel titleLabel;
  private GlobsPanelBuilder builder;
  private TabHandler tabs;

  public AccountEditionDialog(final GlobRepository parentRepository, Directory directory, boolean createAccount) {
    this(directory.get(JFrame.class), parentRepository, directory, createAccount);
  }

  public AccountEditionDialog(Window owner, final GlobRepository parentRepository, Directory directory, boolean createAccount) {
    super(createLocalRepository(parentRepository), directory);
    this.parentRepository = parentRepository;

    dialog = PicsouDialog.create(this, owner, localDirectory);

    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountEditionDialog.splits",
                                                            localRepository, localDirectory);

    titleLabel = builder.add("title", new JLabel("accountEditionDialog")).getComponent();

    tabs = builder.addTabHandler("tabs");

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

    super.createComponents(builder, dialog, Account.POSITION_WITH_PENDING);

    localRepository.addChangeListener(new DefaultChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (currentAccount == null || !changeSet.containsChanges(Account.TYPE)) {
          return;
        }
        Glob account = repository.get(currentAccount.getKey());
        setWarning(account.get(Account.ACCOUNT_TYPE), account.get(Account.CARD_TYPE));
      }
    });

    localRepository.addTrigger(new AbstractChangeSetListener() {
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
    });

    this.builder = builder;
    dialog.registerDisposable(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });
    DeleteAction deleteAction = new DeleteAction();
    if (createAccount) {
      deleteAction.setEnabled(false);
    }
    dialog.addPanelWithButtons(this.builder.<JPanel>load(),
                               new OkAction(), new CancelAction(dialog),
                               deleteAction);
  }

  public static LocalGlobRepository createLocalRepository(GlobRepository parentRepository) {
    return LocalGlobRepositoryBuilder.init(parentRepository)
      .copy(Bank.TYPE, BankEntity.TYPE, AccountUpdateMode.TYPE, MonthDay.TYPE, CurrentMonth.TYPE,
            Account.TYPE, AccountCardType.TYPE, AccountType.TYPE, Month.TYPE, DeferredCardDate.TYPE, RealAccount.TYPE)
      .get();
  }

  public void show(Key accountKey) {
    localRepository.rollback();

    Glob localAccount = localRepository.get(accountKey);
    setBalanceEditorVisible(false);

    doShow(localAccount, false);
  }

  public void showWithNewAccount(AccountType type, boolean accountTypeEditable, AccountUpdateMode updateMode) {
    localRepository.rollback();
    accountTypeCombo.setEnabled(accountTypeEditable);
    Glob newAccount = localRepository.create(Account.TYPE,
                                             value(Account.ACCOUNT_TYPE, type.getId()),
                                             value(Account.UPDATE_MODE, updateMode.getId()),
                                             value(Account.BANK, null),
                                             value(Account.POSITION_WITH_PENDING, null),
                                             value(Account.POSITION_DATE, null));
    doShow(newAccount, true);
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

  protected void accountDefinitionErrorShown() {
    super.accountDefinitionErrorShown();
    tabs.select(0);
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
        localRepository.getCurrentChanges().safeVisit(Account.TYPE, new DefaultChangeSetVisitor() {
          public void visitCreation(Key key, FieldValues values) throws Exception {
            localRepository.update(key, value(Account.POSITION_DATE, TimeService.getToday()),
                                   value(Account.SHOW_CHART, Account.isMain(localRepository.get(key))));
          }
        });
        localRepository.commitChanges(true);
      }
      finally {
        dialog.setVisible(false);
      }
    }
  }

  private class DeleteAction extends AbstractAction {
    DeleteAccountHandler handler;

    public DeleteAction() {
      super(Lang.get("accountEdition.delete"));
      handler = new DeleteAccountHandler(dialog, parentRepository, localRepository, localDirectory);
    }

    public void actionPerformed(ActionEvent e) {
      handler.delete(currentAccount, true);
    }
  }
}
