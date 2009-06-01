package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.PicsouDialog;
import org.designup.picsou.gui.description.AccountStringifier;
import org.designup.picsou.gui.description.Formatting;
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
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.SortedSet;

public class AccountPositionEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Glob account;
  private Date balanceDate;

  public AccountPositionEditionDialog(Glob account, boolean accountInitialization,
                                      GlobRepository repository, Directory directory, Window parent) {
    this.account = account;

    this.localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Account.TYPE)
        .get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/accountPositionEditionDialog.splits",
                                                      localRepository, directory);

    ValidateAction validateAction = new ValidateAction();

    JTextField editor = builder.addEditor("amountField", Account.POSITION)
      .setValidationAction(validateAction)
      .setNotifyOnKeyPressed(true)
      .forceSelection(account)
      .getComponent();

    JTextArea initialMessage = builder.add("initialMessage", new JTextArea());

    AccountStringifier accountStringifier = new AccountStringifier();
    builder.add("accountName", new JLabel(Lang.get("accountPositionEdition.account.name",
                                                   accountStringifier.toString(account, repository))));

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
      dialog = PicsouDialog.create(parent, directory);
      dialog.addPanelWithButtons(builder.<JPanel>load(), validateAction, new CancelAction(dialog));
      dialog.setPreferredSize(new Dimension(400, 300));
      initialMessage.setVisible(false);
    }

    dialog.setAutoFocusOnOpen(editor);

    dialog.pack();
  }

  private void updateOperationInfo(Glob account, JLabel date, JLabel label, JLabel amount, GlobRepository repository) {
    Integer transactionId = account.get(Account.TRANSACTION_ID);
    Glob transaction;
    if (transactionId == null) {
      transaction = getLatestTransactions(account, repository);
    }
    else {
      transaction = repository.get(Key.create(Transaction.TYPE, transactionId));
      if (transaction.get(Transaction.BANK_MONTH) > TimeService.getCurrentMonth() ||
          (transaction.get(Transaction.BANK_MONTH) == TimeService.getCurrentMonth() &&
           transaction.get(Transaction.BANK_DAY) == TimeService.getCurrentDay())) {
        transaction = getLatestTransactions(account, repository);
      }
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
      amountInfo = Formatting.DECIMAL_FORMAT.format(transaction.get(Transaction.AMOUNT));
    }
    else {
      balanceDate = Month.toDate(TimeService.getCurrentMonth(), TimeService.getCurrentDay());
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
        localRepository.update(account.getKey(), Account.POSITION_DATE, balanceDate);
      }
      localRepository.update(account.getKey(), Account.TRANSACTION_ID, null);
      localRepository.commitChanges(true);
      dialog.setVisible(false);
    }
  }
}
