package org.designup.picsou.gui.transactions.creation;

import com.jidesoft.swing.AutoCompletion;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.utils.AccountCreation;
import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.components.AmountEditor;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.components.utils.CustomFocusTraversalPolicy;
import org.designup.picsou.gui.components.MonthRangeBound;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MonthChooserDialog;
import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.description.stringifiers.MonthFieldListStringifier;
import org.designup.picsou.gui.description.stringifiers.MonthRangeFormatter;
import org.designup.picsou.gui.help.actions.HelpAction;
import org.designup.picsou.gui.startup.components.AutoCategorizationFunctor;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.repository.ReplicationGlobRepository;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class TransactionCreationPanel extends View implements GlobSelectionListener, ChangeSetListener {

  private static final Key PROTOTYPE_TRANSACTION_KEY = Key.create(Transaction.TYPE, 0);

  private GlobRepository parentRepository;
  private SelectionService parentSelectionService;

  private JPanel panel = new JPanel();
  private JLabel errorMessageLabel;
  private JTextField amountField;
  private JTextField dayField;
  private JTextField labelField;
  private JComboBox accountCombo;
  private AbstractAction showHideAction = new ShowHideAction();
  private JButton showHideButton;
  private AmountEditor amountEditor;
  private DetailsTip buttonTip;
  private boolean isShowing;
  private AutoCategorizationFunctor autoCategorizationFunctor;
  private JCheckBox updateAccountCheckBox;

  public TransactionCreationPanel(GlobRepository repository, Directory directory, Directory parentDirectory) {
    super(createLocalRepository(repository), directory);
    this.parentRepository = repository;
    this.directory = directory;
    this.selectionService.addListener(this, Month.TYPE);
    this.parentSelectionService = parentDirectory.get(SelectionService.class);
    this.parentRepository.addChangeListener(this);
    this.autoCategorizationFunctor = new AutoCategorizationFunctor(parentRepository);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList all = selection.getAll(Account.TYPE);
        if (all.isEmpty()) {
          return;
        }
        for (Glob glob : all) {
          updateAccountCheckBox.setVisible(glob.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId()));
          return;
        }
      }
    }, Account.TYPE);
  }

  private static GlobRepository createLocalRepository(GlobRepository repository) {
    return new ReplicationGlobRepository(repository, Transaction.TYPE);
  }

  public Action getShowHideAction() {
    return showHideAction;
  }

  public void registerComponents(GlobsPanelBuilder builder) {
    builder.add("transactionCreation", createPanel());
    if (showHideButton == null) {
      showHideButton = new JButton(showHideAction);
    }
    builder.add("showHideTransactionCreation", showHideButton);
    updateAccount();
    hide();
  }

  private JPanel createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/transactions/transactionCreationPanel.splits",
                            repository, directory);

    accountCombo = builder.addComboEditor("account", Transaction.ACCOUNT)
      .setShowEmptyOption(false)
      .setFilter(AccountMatchers.userCreatedAccounts())
      .forceSelection(PROTOTYPE_TRANSACTION_KEY)
      .getComponent();

    JCheckBox shouldBeReconciledCheckBox = builder.addCheckBox("shouldBeReconciled", Transaction.TO_RECONCILE)
      .forceSelection(PROTOTYPE_TRANSACTION_KEY).getComponent();

    updateAccountCheckBox = new JCheckBox();
    updateAccountCheckBox.setSelected(true);
    builder.add("updateAccountPosition", updateAccountCheckBox);

    amountEditor = new AmountEditor(Transaction.AMOUNT, repository, directory, false, null)
      .forceSelection(PROTOTYPE_TRANSACTION_KEY)
      .update(false, false);
    amountField = amountEditor.getNumericEditor().getComponent();
    builder.add("amountEditor", amountEditor.getPanel());

    dayField = builder.addEditor("day", Transaction.DAY).forceSelection(PROTOTYPE_TRANSACTION_KEY).getComponent();
    labelField = builder.addEditor("label", Transaction.LABEL).forceSelection(PROTOTYPE_TRANSACTION_KEY).getComponent();

    builder.addButton("month",
                      Transaction.TYPE,
                      new MonthFieldListStringifier(Transaction.MONTH, MonthRangeFormatter.STANDARD),
                      new EditMonthCallback())
      .forceSelection(PROTOTYPE_TRANSACTION_KEY);

    errorMessageLabel = builder.add("errorMessage", new JLabel()).getComponent();
    errorMessageLabel.setVisible(false);

    CreateTransactionAction createAction = new CreateTransactionAction();
    amountField.addActionListener(createAction);
    dayField.addActionListener(createAction);
    labelField.addActionListener(createAction);
    JButton createButton = new JButton(createAction);
    builder.add("create", createButton);

    builder.add("help", new HelpAction(Lang.get("help"), "manualInput", Lang.get("help"), directory));

    panel = builder.load();

    panel.setFocusCycleRoot(true);
    panel.setFocusTraversalPolicy(new CustomFocusTraversalPolicy(dayField, labelField, amountField,
                                                                 shouldBeReconciledCheckBox, createButton, accountCombo));

    return panel;
  }

  public void selectionUpdated(GlobSelection selection) {
    repository.startChangeSet();
    try {
      GlobList months = selection.getAll(Month.TYPE);
      if (months.isEmpty()) {
        return;
      }

      Integer currentMonth = months.getSortedSet(Month.ID).last();
      repository.findOrCreate(PROTOTYPE_TRANSACTION_KEY, value(Transaction.TO_RECONCILE, false));
      updateMonth(repository, currentMonth);
    }
    finally {
      repository.completeChangeSet();
    }
  }

  static private void updateMonth(GlobRepository repository, Integer currentMonth) {
    repository.update(PROTOTYPE_TRANSACTION_KEY,
                      value(Transaction.MONTH, currentMonth),
                      value(Transaction.BUDGET_MONTH, currentMonth),
                      value(Transaction.POSITION_MONTH, currentMonth),
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
      amountEditor.setNegativeAmounts();
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

  public void showTip() {
    if (buttonTip != null) {
      buttonTip.dispose();
    }
    if (isShowing) {
      buttonTip = new DetailsTip(accountCombo,
                                 Lang.get("transactionCreation.tip.open"), directory);
    }
    else {
      buttonTip = new DetailsTip(showHideButton,
                                 Lang.get("transactionCreation.tip.closed"), directory);
    }
    buttonTip.show();
    buttonTip.setClickThrough();
  }

  private class EditMonthCallback implements GlobListFunctor {
    public void run(GlobList list, GlobRepository repository) {
      MonthChooserDialog monthChooser = new MonthChooserDialog(directory.get(JFrame.class), directory);
      Integer currentMonthId = repository.get(PROTOTYPE_TRANSACTION_KEY).get(Transaction.MONTH);
      int selectedMonthId = monthChooser.show(currentMonthId,
                                              MonthRangeBound.LOWER,
                                              CurrentMonth.getLastMonth(repository));
      if (selectedMonthId < 0) {
        return;
      }
      updateMonth(repository, selectedMonthId);
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

      if (buttonTip != null) {
        buttonTip.dispose();
        buttonTip = null;
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

      Integer accountId = prototypeTransaction.get(Transaction.ACCOUNT);
      if (accountId == null) {
        showErrorMessage("transactionCreation.error.account");
        return;
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

      Integer month = prototypeTransaction.get(Transaction.MONTH);
      int maxDay = Month.getLastDayNumber(month);
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

      Glob createdTransaction;
      try {

        parentRepository.startChangeSet();

        SignpostStatus.setCompleted(SignpostStatus.CREATED_TRANSACTIONS_MANUALLY, parentRepository);

        for (int monthId = month;
             monthId < CurrentMonth.getCurrentMonth(parentRepository);
             monthId = Month.next(monthId)) {
          Key monthKey = Key.create(Month.TYPE, monthId);
          if (parentRepository.contains(monthKey)) {
            break;
          }
          parentRepository.create(Month.TYPE, value(Month.ID, monthId));
        }

        parentRepository.create(AccountPositionMode.TYPE,
                                value(AccountPositionMode.UPDATE_ACCOUNT_POSITION,
                                      updateAccountCheckBox.isSelected()));

        createdTransaction = parentRepository.create(Transaction.TYPE, values.toArray());

        Glob account = parentRepository.get(KeyBuilder.newKey(Account.TYPE, accountId));
        if (!account.get(Account.IS_IMPORTED_ACCOUNT)) {
          parentRepository.update(account.getKey(), value(Account.IS_IMPORTED_ACCOUNT, true));
        }

        autoCategorizationFunctor.run(createdTransaction, parentRepository);
      }
      catch (Exception e1) {
        throw new RuntimeException(e1);
      }
      finally {
        parentRepository.completeChangeSet();
      }

      amountField.setText("");
      dayField.setText("");
      labelField.setText("");

      repository.update(PROTOTYPE_TRANSACTION_KEY,
                        value(Transaction.AMOUNT, null),
                        value(Transaction.DAY, null),
                        value(Transaction.BANK_DAY, null),
                        value(Transaction.POSITION_DAY, null),
                        value(Transaction.BUDGET_DAY, null),
                        value(Transaction.LABEL, ""),
                        value(Transaction.TO_RECONCILE, false));

      dayField.requestFocus();

      Glob monthToSelect = repository.get(Key.create(Month.TYPE, month));
      if (!parentSelectionService.getSelection(Month.TYPE).contains(monthToSelect)) {
        parentSelectionService.select(monthToSelect);
      }
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

    if (User.isDemoUser(repository.get(User.KEY))) {
      MessageDialog.show("demo.transaction.creation.title", MessageType.INFO, directory.get(JFrame.class), directory, "demo.transaction.creation.content");
      return;
    }

    AutoCompletion autoCompletion =
      new AutoCompletion(labelField, new AutoCompletionModel(parentRepository));
    autoCompletion.setStrict(false);

    try {
      repository.startChangeSet();
      repository.update(SignpostStatus.KEY,
                        value(SignpostStatus.INIT_STARTED, true),
                        value(SignpostStatus.GOTO_CATEGORIZATION_DONE, true),
                        value(SignpostStatus.IMPORT_STARTED, true),
                        value(SignpostStatus.WELCOME_SHOWN, true));
      switch (SignpostStatus.getCurrentSection(repository)) {
        case NOT_STARTED:
        case IMPORT:
          SignpostStatus.setSection(SignpostSectionType.CATEGORIZATION, repository);
          break;
        case CATEGORIZATION:
        case BUDGET:
        case COMPLETED:
          break;
      }
    }
    finally {
      repository.completeChangeSet();
    }

    if (!AccountCreation.containsUserAccount(parentRepository, directory,
                                             Lang.get("accountCreation.transactionCreation.message"))) {
      return;
    }

    setVisible(true, "transactionCreation.hide");
    dayField.requestFocus();
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
