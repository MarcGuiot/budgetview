package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.components.PicsouDialog;
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
import java.util.SortedSet;

public class BalanceEditionDialog {
  private PicsouDialog dialog;
  private LocalGlobRepository localRepository;

  public BalanceEditionDialog(Window parent, GlobRepository repository, Directory directory, Glob account) {

    LocalGlobRepositoryBuilder localGlobRepositoryBuilder = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE);
    this.localRepository = localGlobRepositoryBuilder.get();

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/balanceEditionDialog.splits",
                                                      localRepository, directory);
    builder.addEditor("amount", Account.BALANCE).setNotifyAtKeyPressed(true)
      .forceSelection(account);
    Integer transactionId = account.get(Account.TRANSACTION_ID);
    Glob transaction = null;
    if (transactionId == null) {
      SortedSet<Glob> globSortedSet =
        repository.getSorted(Transaction.TYPE, TransactionComparator.ASCENDING_BANK,
                             GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, account.get(Account.ID)),
                                              GlobMatchers.fieldEquals(Transaction.PLANNED, false)));
      if (!globSortedSet.isEmpty()) {
        transaction = globSortedSet.last();
      }
    }
    else {
      transaction = repository.get(Key.create(Transaction.TYPE, transactionId));
    }
    String text = "";
    if (transaction != null) {
      text = Lang.get("balance.edition.transaction.info", transaction.get(Transaction.LABEL),
                      Month.toYear(transaction.get(Transaction.BANK_MONTH)),
                      Month.toMonth(transaction.get(Transaction.BANK_MONTH)),
                      transaction.get(Transaction.BANK_DAY));
    }
    JLabel transactionInfo = new JLabel(text);
    builder.add("transactionInfo", transactionInfo);
    if (transaction == null) {
      transactionInfo.setVisible(false);
    }

    dialog = PicsouDialog.createWithButtons(parent, builder.<JPanel>load(),
                                            new ValidateAction(),
                                            new CancelAction(), directory);
    dialog.setTitle(Lang.get("balance.edition.title"));
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
