package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.SortedSet;

public class BalanceEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Glob account;
  private Date balanceDate;
  private JTextField editor;

  public BalanceEditionDialog(Glob account, boolean accountInitialization,
                              GlobRepository repository, Directory directory, Window parent) {
    this.account = account;

    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Account.TYPE)
        .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/balanceEditionDialog.splits",
                                                      localRepository, directory);

    ValidateAction validateAction = new ValidateAction();

    editor = builder.addEditor("amountField", Account.BALANCE)
      .setValidationAction(validateAction)
      .setNotifyOnKeyPressed(true)
      .forceSelection(account)
      .getComponent();

    JTextArea initialMessage = builder.add("initialMessage", new JTextArea());

    builder.add("accountName", new JLabel(Lang.get("balance.edition.account.name",
                                                   Strings.toString(account.get(Account.NAME)),
                                                   Strings.toString(account.get(Account.NUMBER)))));

    JLabel date = builder.add("dateInfo", new JLabel());
    JLabel label = builder.add("labelInfo", new JLabel());
    JLabel amount = builder.add("amountInfo", new JLabel());

    updateOperationInfo(account, date, label, amount, repository);

    if (accountInitialization) {
      dialog = PicsouDialog.createWithButton(parent, builder.<JPanel>load(), validateAction, directory);
      dialog.disableEscShortcut();
      initialMessage.setVisible(true);
      editor.setText("0.0");
      dialog.setPreferredSize(new Dimension(400, 350));
    }
    else {
      dialog = PicsouDialog.createWithButtons(parent, builder.<JPanel>load(),
                                              validateAction, new CancelAction(), directory);
      dialog.setPreferredSize(new Dimension(400, 300));
      initialMessage.setVisible(false);
    }

    dialog.addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        editor.selectAll();
        editor.requestFocusInWindow();
      }
    });

    dialog.pack();
  }

  private void updateOperationInfo(Glob account, JLabel date, JLabel label, JLabel amount, GlobRepository repository) {
    Integer transactionId = account.get(Account.TRANSACTION_ID);
    Glob transaction = null;
    if (transactionId == null) {
      SortedSet<Glob> globSortedSet =
        repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_BANK_SPLIT_AFTER,
                             GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                                              GlobMatchers.fieldEquals(Transaction.PLANNED, false)));
      if (!globSortedSet.isEmpty()) {
        transaction = globSortedSet.last();
      }
    }
    else {
      transaction = repository.get(Key.create(Transaction.TYPE, transactionId));
    }

    String dateInfo = "";
    String labelInfo = "";
    String amountInfo = "";
    if (transaction != null) {
      Integer monthId = transaction.get(Transaction.BANK_MONTH);
      int month = Month.toMonth(monthId);
      Integer day = transaction.get(Transaction.BANK_DAY);
      dateInfo = Lang.get("transactionView.dateFormat",
                          (day < 10 ? "0" : "") + day,
                          (month < 10 ? "0" : "") + month,
                          Integer.toString(Month.toYear(monthId)));
      balanceDate = Month.toDate(monthId, day);
      labelInfo = transaction.get(Transaction.LABEL);
      amountInfo = PicsouDescriptionService.DECIMAL_FORMAT.format(transaction.get(Transaction.AMOUNT));
    }

    date.setText(dateInfo);
    label.setText(labelInfo);
    amount.setText(amountInfo);

    if (transaction == null) {
      date.setVisible(false);
      label.setVisible(false);
      amount.setVisible(false);
    }
  }

  public void show() {
    GuiUtils.center(dialog);
    dialog.setVisible(true);
  }

  private class ValidateAction extends AbstractAction {
    public ValidateAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      if (balanceDate != null) {
        localRepository.update(account.getKey(), Account.BALANCE_DATE, balanceDate);
      }
      localRepository.update(account.getKey(), Account.TRANSACTION_ID, null);
      localRepository.commitChanges(true);
      dialog.setVisible(false);
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

}
