package org.designup.picsou.gui.transactions.creation;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.components.ConfirmationDialog;
import org.designup.picsou.gui.components.CustomFocusTraversalPolicy;
import org.designup.picsou.gui.license.LicenseActivationDialog;
import org.designup.picsou.gui.license.LicenseService;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.ReplicationGlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public class TransactionCreationPanel extends View implements GlobSelectionListener, ChangeSetListener {
  private GlobRepository parentRepository;
  private static final Key PROTOTYPE_TRANSACTION_KEY = Key.create(Transaction.TYPE, 0);
  private Glob prototypeTransaction;

  private JPanel panel = new JPanel();
  private JTextField monthField;
  private JLabel errorMessageLabel;
  private JTextField amountField;
  private JTextField dayField;
  private JTextField labelField;
  private JComboBox accountCombo;
  private AbstractAction showHideAction = new ShowHideAction();
  private boolean isShowing;

  public TransactionCreationPanel(GlobRepository repository, Directory directory) {
    super(createLocalRepository(repository), directory);
    this.parentRepository = repository;
    this.directory = directory;
    this.prototypeTransaction = this.repository.create(PROTOTYPE_TRANSACTION_KEY);
    this.selectionService.addListener(this, Month.TYPE);
    this.parentRepository.addChangeListener(this);
  }

  private static GlobRepository createLocalRepository(GlobRepository repository) {
    return new ReplicationGlobRepository(repository, Transaction.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("transactionCreation", createPanel());
    builder.add("showHideTransactionCreation", showHideAction);
    updateAccount();
    hide();
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/transactionCreationPanel.splits",
                            repository, directory);

    accountCombo = builder.addComboEditor("account", Transaction.ACCOUNT)
      .setShowEmptyOption(false)
      .setFilter(GlobMatchers.fieldEquals(Account.UPDATE_MODE, AccountUpdateMode.MANUAL.getId()))
      .forceSelection(prototypeTransaction)
      .getComponent();

    amountField = builder.addEditor("amount", Transaction.AMOUNT).forceSelection(prototypeTransaction).getComponent();
    dayField = builder.addEditor("day", Transaction.DAY).forceSelection(prototypeTransaction).getComponent();
    labelField = builder.addEditor("label", Transaction.LABEL).forceSelection(prototypeTransaction).getComponent();

    monthField = builder.add("month", new JTextField());
    monthField.setEditable(false);

    errorMessageLabel = builder.add("errorMessage", new JLabel());
    errorMessageLabel.setVisible(false);

    CreateTransactionAction createAction = new CreateTransactionAction();
    amountField.addActionListener(createAction);
    dayField.addActionListener(createAction);
    labelField.addActionListener(createAction);
    JButton createButton = new JButton(createAction);
    builder.add("create", createButton);

    panel = builder.load();

    panel.setFocusCycleRoot(true);
    panel.setFocusTraversalPolicy(new CustomFocusTraversalPolicy(amountField, dayField, labelField,
                                                                 createButton, accountCombo));

    return panel;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList months = selection.getAll(Month.TYPE);
    if (months.isEmpty()) {
      return;
    }

    Integer currentMonth = months.getSortedSet(Month.ID).last();
    repository.update(PROTOTYPE_TRANSACTION_KEY,
                      value(Transaction.MONTH, currentMonth),
                      value(Transaction.BANK_MONTH, currentMonth));
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Account.TYPE)) {
      updateAccount();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE)) {
      updateAccount();
    }
  }

  private void updateAccount() {
    if (isShowing && (accountCombo.getItemCount() == 0)) {
      hide();
      return;
    }

    if ((accountCombo.getSelectedIndex() < 0) && (accountCombo.getItemCount() > 0)) {
      accountCombo.setSelectedIndex(0);
    }
  }

  private class CreateTransactionAction extends AbstractAction {

    private CreateTransactionAction() {
      super(Lang.get("transactionCreation.create"));
    }

    public void actionPerformed(ActionEvent e) {
      Double amount = prototypeTransaction.get(Transaction.AMOUNT);
      if (amount == null) {
        amountField.requestFocus();
        showErrorMessage("transactionCreation.error.amount");
        return;
      }

      Integer day = prototypeTransaction.get(Transaction.DAY);
      if (day == null) {
        dayField.requestFocus();
        showErrorMessage("transactionCreation.error.day");
        return;
      }

      int maxDay = Month.getLastDayNumber(prototypeTransaction.get(Transaction.MONTH));
      if ((day < 1) || (day > maxDay)) {
        dayField.requestFocus();
        showErrorMessage("transactionCreation.error.day.range", Integer.toString(maxDay));
        return;
      }

      String label = prototypeTransaction.get(Transaction.LABEL);
      if (Strings.isNullOrEmpty(label)) {
        labelField.requestFocus();
        showErrorMessage("transactionCreation.error.label");
        return;
      }
      String upperCaseLabel = label.toUpperCase();

      errorMessageLabel.setText("");
      errorMessageLabel.setVisible(false);

      FieldValues values = FieldValuesBuilder.init()
        .set(repository.get(PROTOTYPE_TRANSACTION_KEY).getValues())
        .set(Transaction.BANK_DAY, day)
        .set(Transaction.LABEL, upperCaseLabel)
        .set(Transaction.ORIGINAL_LABEL, upperCaseLabel)
        .set(Transaction.LABEL_FOR_CATEGORISATION, Transaction.anonymise(upperCaseLabel))
        .set(Transaction.TRANSACTION_TYPE, TransactionType.MANUAL.getId())
        .get();

      Glob createdTransaction = parentRepository.create(Transaction.TYPE, values.toArray());

      repository.update(PROTOTYPE_TRANSACTION_KEY,
                        value(Transaction.AMOUNT, null),
                        value(Transaction.DAY, null),
                        value(Transaction.BANK_DAY, null),
                        value(Transaction.LABEL, ""));

      amountField.setText("");
      dayField.setText("");
      labelField.setText("");
      amountField.requestFocus();

      selectionService.select(createdTransaction);
    }

    private void showErrorMessage(String messageKey, String... args) {
      errorMessageLabel.setText(Lang.get(messageKey, args));
      errorMessageLabel.setVisible(true);
    }
  }

  private class ShowHideAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
      if (isShowing) {
        hide();
      }
      else {
        show();
      }
    }
  }

  private void show() {

    if (LicenseService.trialExpired(parentRepository)) {
      LicenseActivationDialog dialog = new LicenseActivationDialog(directory.get(JFrame.class),
                                                                   repository, directory);
      dialog.showExpiration();
      return;
    }

    if (accountCombo.getItemCount() == 0) {
      final JFrame frame = directory.get(JFrame.class);
      ConfirmationDialog dialog = new ConfirmationDialog("transactionCreation.noAccounts.title",
                                                         "transactionCreation.noAccounts.message",
                                                         frame, directory) {
        protected void postValidate() {
          AccountEditionDialog accountEdition = new AccountEditionDialog(frame, parentRepository, directory);
          accountEdition.showWithNewAccount(AccountType.MAIN, AccountUpdateMode.MANUAL, true);
        }
      };
      dialog.show();
      if (accountCombo.getItemCount() == 0) {
        return;
      }
    }

    setVisible(true, "transactionCreation.hide");
    amountField.requestFocus();
  }

  private void hide() {
    setVisible(false, "transactionCreation.show");
  }

  private void setVisible(boolean visible, String showHideButtonKey) {
    panel.setVisible(visible);
    showHideAction.putValue(Action.NAME, Lang.get(showHideButtonKey));
    isShowing = visible;
  }
}
