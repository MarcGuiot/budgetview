package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.description.AccountStringifier;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.SortedSet;

public class AccountPositionEditionPanel {

  private GlobRepository repository;
  private Glob account;
  private Date balanceDate;
  private AccountStringifier accountStringifier;

  private JTextArea initialMessage;
  private GlobNumericEditor editor;
  private JLabel transactionDateField;
  private JLabel transactionLabelField;
  private JLabel transactionAmountField;
  private JLabel accountNameField;
  private JPanel panel;

  public AccountPositionEditionPanel(boolean accountInitialization,
                                     AbstractAction validateAction,
                                     GlobRepository repository,
                                     Directory directory,
                                     Window parent) {
    this.account = account;
    this.repository = repository;

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountPositionEditionPanel.splits",
                                                      repository, directory);

    editor = builder.addEditor("amountField", Account.POSITION)
      .setValidationAction(validateAction)
      .setNotifyOnKeyPressed(true);

    initialMessage = builder.add("initialMessage", new JTextArea());

    accountStringifier = new AccountStringifier();
    accountNameField = builder.add("accountName", new JLabel());

    transactionDateField = builder.add("dateInfo", new JLabel());
    transactionLabelField = builder.add("labelInfo", new JLabel());
    transactionAmountField = builder.add("amountInfo", new JLabel());

    panel = builder.load();
  }

  public void setAccount(Glob account, GlobRepository transactionsRepository) {
    this.account = account;
    this.editor.forceSelection(account.getKey());
    this.accountNameField.setText(Lang.get("accountPositionEdition.account.name",
                                           accountStringifier.toString(account, repository)));
    updateTransactionInfo(account, transactionsRepository);
  }

  public void setInitialMessageVisible(boolean visible) {
    initialMessage.setVisible(visible);
  }

  public void setText(String text) {
    getEditor().setText(text);
  }

  public JTextField getEditor() {
    return editor.getComponent();
  }

  public JPanel getPanel() {
    return panel;
  }

  private void updateTransactionInfo(Glob account, GlobRepository transactionsRepository) {
    Integer transactionId = account.get(Account.TRANSACTION_ID);
    Glob transaction;
    if (transactionId == null) {
      transaction = getLatestTransactions(account, transactionsRepository);
    }
    else {
      transaction = transactionsRepository.get(Key.create(Transaction.TYPE, transactionId));
      if (transaction.get(Transaction.BANK_MONTH) > TimeService.getCurrentMonth() ||
          (transaction.get(Transaction.BANK_MONTH) == TimeService.getCurrentMonth() &&
           transaction.get(Transaction.BANK_DAY) == TimeService.getCurrentDay())) {
        transaction = getLatestTransactions(account, transactionsRepository);
      }
    }

    String date = "";
    String label = "";
    String amount = "";
    if (transaction != null) {
      Integer monthId = transaction.get(Transaction.BANK_MONTH);
      int month = Month.toMonth(monthId);
      Integer day = transaction.get(Transaction.BANK_DAY);
      date = Lang.get("transactionView.dateFormat",
                          (day < 10 ? "0" : "") + day,
                          (month < 10 ? "0" : "") + month,
                          Integer.toString(Month.toYear(monthId)));
      balanceDate = Month.toDate(monthId, day);
      label = transaction.get(Transaction.LABEL);
      amount = Formatting.DECIMAL_FORMAT.format(transaction.get(Transaction.AMOUNT));
    }
    else {
      balanceDate = Month.toDate(TimeService.getCurrentMonth(), TimeService.getCurrentDay());
    }


    transactionDateField.setText(date);
    transactionLabelField.setText(label);
    transactionAmountField.setText(amount);

    if (transaction == null) {
      transactionDateField.setVisible(false);
      transactionLabelField.setVisible(false);
      transactionAmountField.setVisible(false);
    }
  }

  private Glob getLatestTransactions(Glob account, GlobRepository repository) {
    SortedSet<Glob> globSortedSet =
      repository.getSorted(
        Transaction.TYPE, TransactionComparator.ASCENDING_BANK_SPLIT_AFTER,
        GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                         GlobMatchers.fieldEquals(Transaction.PLANNED, false),
                         GlobMatchers.or(
                           (GlobMatchers.fieldStrictlyLessThan(Transaction.BANK_MONTH, TimeService.getCurrentMonth())),
                           GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.BANK_MONTH, TimeService.getCurrentMonth()),
                                            GlobMatchers.fieldLesserOrEqual(Transaction.DAY, TimeService.getCurrentDay())))));
    if (!globSortedSet.isEmpty()) {
      return globSortedSet.last();
    }
    return null;
  }

  public void apply() {
    if (balanceDate != null) {
      repository.update(account.getKey(), Account.POSITION_DATE, balanceDate);
    }
    repository.update(account.getKey(), Account.TRANSACTION_ID, null);
  }
}