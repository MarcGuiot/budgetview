package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.description.stringifiers.AccountStringifier;
import org.designup.picsou.gui.description.stringifiers.TransactionDateStringifier;
import org.designup.picsou.gui.transactions.utils.TransactionMatchers;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.CurrentMonth;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class AccountPositionEditionPanel {

  private GlobRepository repository;
  private Glob account;
  private AccountStringifier accountStringifier;

  private JEditorPane initialMessage;
  private GlobNumericEditor editor;
  private JLabel accountNameField;
  private JPanel panel;
  private GlobsPanelBuilder builder;
  private SelectionService selectionService;
  Glob transaction = null;


  public AccountPositionEditionPanel(AbstractAction validateAction,
                                     GlobRepository repository,
                                     Directory directory, final DoubleField positionField) {
    this.repository = repository;

    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountPositionEditionPanel.splits",
                                    repository, directory);

    editor = builder.addEditor("amountField", positionField)
      .setValidationAction(validateAction)
      .setNotifyOnKeyPressed(true);

    initialMessage = Gui.createHtmlDisplay();
    builder.add("initialMessage", initialMessage);

    accountStringifier = new AccountStringifier();
    accountNameField = builder.add("accountName", new JLabel()).getComponent();

    final TransactionDateStringifier dateStringifier = new TransactionDateStringifier(TransactionComparator.DESCENDING_BANK_SPLIT_AFTER,
                                                                                      Transaction.POSITION_MONTH,
                                                                                      Transaction.POSITION_DAY);
    builder.addLabel("dateInfo", Transaction.TYPE, new GlobListStringifier() {
      public String toString(GlobList list, GlobRepository repository) {
        return dateStringifier.toString(list.getFirst(), repository);
      }
    });
    builder.addLabel("labelInfo", Transaction.LABEL);
    builder.addLabel("amountInfo", Transaction.AMOUNT);
    selectionService = directory.get(SelectionService.class);
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
    GuiUtils.revalidate(initialMessage);
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
    Glob currentMonth = transactionsRepository.get(CurrentMonth.KEY);
    transaction = transactionsRepository.getAll(Transaction.TYPE,
                                                TransactionMatchers.realTransactions(account.get(Account.ID),
                                                                                     currentMonth.get(CurrentMonth.CURRENT_MONTH),
                                                                                     currentMonth.get(CurrentMonth.CURRENT_DAY)))
      .sort(TransactionComparator.ASCENDING_ACCOUNT).getLast();
    if (transaction != null) {
      selectionService.select(transaction);
    }
    else {
      selectionService.clear(Transaction.TYPE);
    }
  }

  public void apply() {
    if (transaction != null) {
      repository.update(account.getKey(), Account.LAST_TRANSACTION, transaction.get(Transaction.ID));
    }
  }

  public void dispose() {
    builder.dispose();
  }
}