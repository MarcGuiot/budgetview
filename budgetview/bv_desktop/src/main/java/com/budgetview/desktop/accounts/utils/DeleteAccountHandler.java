package com.budgetview.desktop.accounts.utils;

import com.budgetview.desktop.cloud.accounts.CloudAccountStatus;
import com.budgetview.desktop.components.dialogs.ConfirmationDialog;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionType;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.LocalGlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Functor;
import org.globsframework.utils.directory.Directory;

import java.awt.*;

import static org.globsframework.model.utils.GlobMatchers.*;

public class DeleteAccountHandler {

  private GlobMatcher transactionMatcher;
  private GlobMatcher seriesMatcher;

  private GlobRepository parentRepository;
  private LocalGlobRepository localRepository;
  private Window owner;
  private Directory localDirectory;

  public DeleteAccountHandler(Window owner, GlobRepository parentRepository, LocalGlobRepository localRepository, Directory localDirectory) {
    this.parentRepository = parentRepository;
    this.localRepository = localRepository;
    this.localDirectory = localDirectory;
    this.owner = owner;
  }

  public void delete(final Glob currentAccount, final boolean closeOwnerOnConfirmation) {
    transactionMatcher = and(GlobMatchers.linkedTo(currentAccount, Transaction.ACCOUNT), not(fieldEquals(Transaction.TRANSACTION_TYPE,
                                                                                                         TransactionType.OPEN_ACCOUNT_EVENT.getId())),
                             not(fieldEquals(Transaction.TRANSACTION_TYPE,
                                             TransactionType.CLOSE_ACCOUNT_EVENT.getId())));
    seriesMatcher = or(linkedTo(currentAccount, Series.TARGET_ACCOUNT),
                       linkedTo(currentAccount, Series.FROM_ACCOUNT),
                       linkedTo(currentAccount, Series.TO_ACCOUNT));

    ConfirmationDialog confirmDialog = new ConfirmationDialog("accountDeletion.confirm.title",
                                                              Lang.get(getMessageKey()),
                                                              owner, localDirectory) {
      protected void processOk() {
        Gui.setWaitCursor(GuiUtils.getEnclosingFrame(owner));
        try {
          CloudAccountStatus.processDeletion(currentAccount.getKey(), localRepository, parentRepository, localDirectory, new Functor() {
            public void run() throws Exception {
              localRepository.delete(currentAccount);
              localRepository.commitChanges(false);
              if (closeOwnerOnConfirmation) {
                owner.setVisible(false);
              }
            }
          });
        }
        finally {
          Gui.setDefaultCursor(GuiUtils.getEnclosingFrame(owner));
        }
      }
    };
    confirmDialog.show();
  }

  private String getMessageKey() {

    boolean hasTransactions = parentRepository.contains(Transaction.TYPE, transactionMatcher);
    boolean hasSeries = parentRepository.contains(Series.TYPE, seriesMatcher);

    if (hasTransactions && hasSeries) {
      return "accountDeletion.confirm.all";
    }
    if (hasTransactions) {
      return "accountDeletion.confirm.transactions";
    }
    if (hasSeries) {
      return "accountDeletion.confirm.series";
    }
    return "accountDeletion.confirm.unused";
  }
}
