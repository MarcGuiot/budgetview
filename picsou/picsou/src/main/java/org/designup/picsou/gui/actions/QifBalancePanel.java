package org.designup.picsou.gui.actions;

import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.gui.views.GlobLabelView;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.NotSupported;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.TransactionImport;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Date;

public class QifBalancePanel {
  protected JPanel panel;
  protected JDialog jDialog;
  private GlobRepository repository;
  private Key importKey;

  public interface OnClose {
    void close(boolean status);
  }

  public QifBalancePanel(GlobRepository repository, Directory directory, final Key importKey) {
    this.repository = repository;
    this.importKey = importKey;

    GlobsPanelBuilder builder = GlobsPanelBuilder.init(repository, directory);
    builder.addLabel(TransactionImport.TYPE, new TransactionImportStringifier());
    builder.addEditor(TransactionImport.BALANCE);
    builder.add("OK", createAction("qifDetailPanel.ok", true));
    builder.add("unknown", createAction("qifDetailPanel.unknown", false));

    panel = (JPanel) builder.parse(getClass(), "/layout/qifBalance.splits");
  }

  private AbstractAction createAction(String titleKey, final boolean status) {
    return new AbstractAction(Lang.get(titleKey)) {
      public void actionPerformed(ActionEvent e) {
        jDialog.dispose();
        if (status) {
          GlobList importList = repository.getAll(TransactionImport.TYPE);
          Glob lastImport = repository.get(importKey);
          Date lastTransactionDateOfLastImport = lastImport.get(TransactionImport.LAST_TRANSACTION_DATE);
          if (lastTransactionDateOfLastImport != null) {
            for (Glob glob : importList) {
              Date date = glob.get(TransactionImport.LAST_TRANSACTION_DATE);
              if (date != null && date.getTime() > lastTransactionDateOfLastImport.getTime()) {
                return;
              }
            }
          }
          GlobList account = repository.getAll(Account.TYPE);
          if (account.size() > 2) {
            throw new NotSupported("Multi account with Qif import");
          }
          if (account.size() == 2) {
            Key accountKey;
            if (account.get(0).get(Account.ID) == -1) {
              accountKey = account.get(1).getKey();
            }
            else {
              accountKey = account.get(0).getKey();
            }
            repository.enterBulkDispatchingMode();
            try {
              repository.update(accountKey, Account.BALANCE, lastImport.get(TransactionImport.BALANCE));
              repository.update(accountKey, Account.UPDATE_DATE, lastImport.get(TransactionImport.LAST_TRANSACTION_DATE));
            }
            finally {
              repository.completeBulkDispatchingMode();
            }
          }
        }
      }
    };
  }

  public void showDialog(Frame parent) {
    jDialog = new JDialog(parent, Lang.get("qifDetailPanel.title"), true);
    jDialog.add(panel);
    jDialog.setSize(350, 180);
    GuiUtils.showCentered(jDialog);
  }

  private static class TransactionImportStringifier implements GlobLabelView.Stringifier {
    public String toString(GlobList selected) {
      StringBuilder source = new StringBuilder();
      for (java.util.Iterator it = selected.iterator(); it.hasNext();) {
        Glob glob = (Glob) it.next();
        source.append(glob.get(TransactionImport.SOURCE));
        if (it.hasNext()) {
          source.append("; ");
        }
      }
      return source.toString();
    }
  }
}
