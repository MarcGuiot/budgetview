package org.designup.picsou.gui.transactions.creation;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.AccountEditionDialog;
import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.CustomFocusTraversalPolicy;
import org.designup.picsou.gui.components.dialogs.ConfirmationDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
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

  private JPanel panel = new JPanel();
  private JLabel monthLabel;
  private JLabel errorMessageLabel;
  private JTextField amountField;
  private JTextField dayField;
  private JTextField labelField;
  private JComboBox accountCombo;
  private AbstractAction showHideAction = new ShowHideAction();
  private boolean isShowing;
  private AmountEditor amountEditor;

  public TransactionCreationPanel(GlobRepository repository, Directory directory) {
    super(createLocalRepository(repository), directory);
    this.parentRepository = repository;
    this.directory = directory;
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
      .forceSelection(PROTOTYPE_TRANSACTION_KEY)
      .getComponent();

    amountEditor = new AmountEditor(Transaction.AMOUNT, repository, directory, false, null)
      .forceSelection(PROTOTYPE_TRANSACTION_KEY)
      .update(false, false);
    amountField = amountEditor.getNumericEditor().getComponent();
    builder.add("amount", amountField);
    builder.add("positiveAmounts", amountEditor.getPositiveRadio());
    builder.add("negativeAmounts", amountEditor.getNegativeRadio());

    dayField = builder.addEditor("day", Transaction.DAY).forceSelection(PROTOTYPE_TRANSACTION_KEY).getComponent();
    labelField = builder.addEditor("label", Transaction.LABEL).forceSelection(PROTOTYPE_TRANSACTION_KEY).getComponent();

    monthLabel = builder.add("month", new JLabel()).getComponent();

    errorMessageLabel = builder.add("errorMessage", new JLabel()).getComponent();
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
    repository.findOrCreate(PROTOTYPE_TRANSACTION_KEY);
    repository.update(PROTOTYPE_TRANSACTION_KEY,
                      value(Transaction.MONTH, currentMonth),
                      value(Transaction.BUDGET_MONTH, currentMonth),
                      value(Transaction.POSITION_MONTH, currentMonth),
                      value(Transaction.BANK_MONTH, currentMonth));
    monthLabel.setText(Month.getFullLabel(currentMonth));
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Account.TYPE)) {
      updateAccount();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE)) {
      updateAccount();
      amountEditor.getNegativeRadio().doClick();
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

    private boolean updateInProgress = false;

    private CreateTransactionAction() {
      super(Lang.get("transactionCreation.create"));
    }

    public void actionPerformed(ActionEvent e) {
      if (updateInProgress) {
        return;
      }
      Glob prototypeTransaction = repository.find(PROTOTYPE_TRANSACTION_KEY);
      updateInProgress = true;
      try {
        amountField.postActionEvent();
        dayField.postActionEvent();
        labelField.postActionEvent();
      }
      finally {
        updateInProgress = false;
      }

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
        .set(Transaction.POSITION_DAY, day)
        .set(Transaction.BUDGET_DAY, day)
        .set(Transaction.LABEL, upperCaseLabel)
        .set(Transaction.ORIGINAL_LABEL, upperCaseLabel)
        .set(Transaction.LABEL_FOR_CATEGORISATION, Transaction.anonymise(upperCaseLabel))
        .set(Transaction.TRANSACTION_TYPE, TransactionType.MANUAL.getId())
        .get();

      Glob createdTransaction = parentRepository.create(Transaction.TYPE, values.toArray());

      amountField.setText("");
      dayField.setText("");
      labelField.setText("");

      repository.update(PROTOTYPE_TRANSACTION_KEY,
                        value(Transaction.AMOUNT, null),
                        value(Transaction.DAY, null),
                        value(Transaction.BANK_DAY, null),
                        value(Transaction.POSITION_DAY, null),
                        value(Transaction.BUDGET_DAY, null),
                        value(Transaction.LABEL, ""));

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

    if (User.isDemoUser(repository.get(User.KEY))){
      MessageDialog dialog = MessageDialog.createMessageDialog("demo.transaction.creation.title", "demo.transaction.creation.content", directory.get(JFrame.class),
                                               directory);
      dialog.show();
      return;
    }
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
