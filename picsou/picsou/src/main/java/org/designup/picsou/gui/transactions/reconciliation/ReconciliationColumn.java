package org.designup.picsou.gui.transactions.reconciliation;

import org.designup.picsou.gui.components.ButtonTableColumn;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.utils.TransparentIcon;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.GlobFieldsComparator;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;

public class ReconciliationColumn extends ButtonTableColumn {

  public static Icon RECONCILED_ICON = Gui.IMAGE_LOCATOR.get("reconciled_yes.png");
  public static Icon UNRECONCILED_ICON = Gui.IMAGE_LOCATOR.get("reconciled_no.png");
  public static Icon DISABLED_ICON = new TransparentIcon(RECONCILED_ICON.getIconHeight(), RECONCILED_ICON.getIconWidth());

  private Glob transaction;

  public ReconciliationColumn(GlobTableView view, GlobRepository repository, Directory directory) {
    super(view, directory.get(DescriptionService.class), repository, directory);
  }

  protected JButton createButton(Action action) {
    JButton button = super.createButton(action);
    Gui.configureIconButton(button, "reconcile",
                            new Dimension(RECONCILED_ICON.getIconHeight(), RECONCILED_ICON.getIconHeight()));
    return button;
  }

  protected void updateComponent(JButton button, JPanel panel, Glob transaction, boolean edit) {
    if (edit) {
      this.transaction = transaction;
      selectTransactionIfNeeded(transaction);
    }

    if ((transaction == null) || (transaction.get(Transaction.SPLIT_SOURCE) != null)) {
      button.setIcon(DISABLED_ICON);
      button.setEnabled(false);
    }
    else if (transaction.isTrue(Transaction.RECONCILED)) {
      button.setIcon(RECONCILED_ICON);
      button.setEnabled(true);
    }
    else {
      button.setIcon(UNRECONCILED_ICON);
      button.setEnabled(true);
    }
  }

  protected void processClick() {
    if (transaction == null) {
      return;
    }
    repository.update(transaction.getKey(), Transaction.RECONCILED, !transaction.isTrue(Transaction.RECONCILED));
  }

  public String getName() {
    return "";
  }

  public GlobStringifier getStringifier() {
    return new AbstractGlobStringifier() {
      public String toString(Glob transaction, GlobRepository repository) {
        if ((transaction != null) && (transaction.isTrue(Transaction.RECONCILED))) {
          return "x";
        }
        return "";
      }
    };
  }

  public Comparator<Glob> getComparator() {
    return new GlobFieldsComparator(Transaction.RECONCILED, true, Transaction.LABEL, false);
  }

  private void selectTransactionIfNeeded(Glob transaction) {
    GlobList selection = tableView.getCurrentSelection();
    if (selection.size() > 1 || !selection.contains(transaction)) {
      tableView.select(transaction);
    }
  }
}
