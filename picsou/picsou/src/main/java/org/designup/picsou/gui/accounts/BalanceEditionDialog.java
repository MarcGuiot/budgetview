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
import java.util.SortedSet;

public class BalanceEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;
  private Glob account;

  public BalanceEditionDialog(Window parent, GlobRepository repository, Directory directory, Glob account) {
    this.account = account;

    LocalGlobRepositoryBuilder localGlobRepositoryBuilder = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE);
    this.localRepository = localGlobRepositoryBuilder.get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/balanceEditionDialog.splits",
                                                      localRepository, directory);
    builder.addEditor("amount", Account.BALANCE).setNotifyAtKeyPressed(true)
      .forceSelection(account);
    builder.add("accountName", new JLabel(Lang.get("balance.edition.account.name",
                                                   Strings.toString(account.get(Account.NAME)),
                                                   Strings.toString(account.get(Account.NUMBER)))));
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
      int month = Month.toMonth(transaction.get(Transaction.BANK_MONTH));
      Integer day = transaction.get(Transaction.BANK_DAY);
      dateInfo = Lang.get("transactionView.dateFormat",
                          (day < 10 ? "0" : "") + day,
                          (month < 10 ? "0" : "") + month,
                          Integer.toString(Month.toYear(transaction.get(Transaction.BANK_MONTH)))
      );
      labelInfo = transaction.get(Transaction.LABEL);
      amountInfo = PicsouDescriptionService.DECIMAL_FORMAT.format(transaction.get(Transaction.AMOUNT));
    }
    JLabel date = new JLabel(dateInfo);
    builder.add("dateInfo", date);

    JLabel label = new JLabel(labelInfo);
    builder.add("labelInfo", label);

    JLabel amount = new JLabel(amountInfo);
    builder.add("amountInfo", amount);

    if (transaction == null) {
      date.setVisible(false);
      label.setVisible(false);
      amount.setVisible(false);
    }

    dialog = PicsouDialog.createWithButtons(parent, builder.<JPanel>load(),
                                            new ValidateAction(),
                                            new CancelAction(), directory);
    dialog.pack();
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
