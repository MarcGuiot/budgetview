package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.description.stringifiers.AccountStringifier;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.editors.GlobNumericEditor;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Date;

public class AccountPositionEditionPanel {

  private GlobRepository repository;
  private Glob account;
  private Date balanceDate;
  private AccountStringifier accountStringifier;

  private JEditorPane initialMessage;
  private GlobNumericEditor editor;
  private JLabel transactionDateField;
  private JLabel transactionLabelField;
  private JLabel transactionAmountField;
  private JLabel accountNameField;
  private JPanel panel;
  private GlobsPanelBuilder builder;

  public AccountPositionEditionPanel(AbstractAction validateAction,
                                     GlobRepository repository,
                                     Directory directory) {
    this.repository = repository;

    builder = new GlobsPanelBuilder(getClass(), "/layout/accounts/accountPositionEditionPanel.splits",
                          repository, directory);

    editor = builder.addEditor("amountField", Account.POSITION)
      .setValidationAction(validateAction)
      .setNotifyOnKeyPressed(true);

    initialMessage = Gui.createHtmlDisplay();
    builder.add("initialMessage", initialMessage);

    accountStringifier = new AccountStringifier();
    accountNameField = builder.add("accountName", new JLabel()).getComponent();

    transactionDateField = builder.add("dateInfo", new JLabel()).getComponent();
    transactionLabelField = builder.add("labelInfo", new JLabel()).getComponent();
    transactionAmountField = builder.add("amountInfo", new JLabel()).getComponent();

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
      balanceDate = Month.toDate(TimeService.getCurrentMonth(), TimeService.getCurrentDay());
      transactionDateField.setVisible(false);
      transactionLabelField.setVisible(false);
      transactionAmountField.setVisible(false);
  }

  public void apply() {
    if (balanceDate != null) {
      repository.update(account.getKey(), Account.POSITION_DATE, balanceDate);
    }
    repository.update(account.getKey(), Account.TRANSACTION_ID, null);
  }

  public void dispose() {
    builder.dispose();
  }
}