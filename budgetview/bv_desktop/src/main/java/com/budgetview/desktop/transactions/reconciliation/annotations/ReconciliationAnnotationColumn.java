package com.budgetview.desktop.transactions.reconciliation.annotations;

import com.budgetview.desktop.components.table.ButtonTableColumn;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
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

public class ReconciliationAnnotationColumn extends ButtonTableColumn {

  public static Icon RECONCILED_ICON = Gui.IMAGE_LOCATOR.get("reconciled_yes.png");
  public static Icon UNRECONCILED_ICON = Gui.IMAGE_LOCATOR.get("reconciled_no.png");
  public static Icon RECONCILED_ICON_DISABLED = Gui.IMAGE_LOCATOR.get("reconciled_yes_disabled.png");
  public static Icon UNRECONCILED_ICON_DISABLED = Gui.IMAGE_LOCATOR.get("reconciled_no_disabled.png");

  private Glob transaction;

  public ReconciliationAnnotationColumn(GlobTableView view, GlobRepository repository, Directory directory) {
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
      Glob source = repository.findLinkTarget(transaction, Transaction.SPLIT_SOURCE);
      if (source.isTrue(Transaction.RECONCILIATION_ANNOTATION_SET)) {
        button.setDisabledIcon(RECONCILED_ICON_DISABLED);
        setTooltip(button, panel, Lang.get("reconciliation.annotation.tooltip.yes.disabled"));
      }
      else {
        button.setDisabledIcon(UNRECONCILED_ICON_DISABLED);
        setTooltip(button, panel, Lang.get("reconciliation.annotation.tooltip.no.disabled"));
      }
      button.setEnabled(false);
    }
    else if (transaction.isTrue(Transaction.RECONCILIATION_ANNOTATION_SET)) {
      button.setIcon(RECONCILED_ICON);
      button.setEnabled(true);
      setTooltip(button, panel, Lang.get("reconciliation.annotation.tooltip.yes"));
    }
    else {
      button.setIcon(UNRECONCILED_ICON);
      button.setEnabled(true);
      setTooltip(button, panel, Lang.get("reconciliation.annotation.tooltip.no"));
    }
  }

  private void setTooltip(JButton button, JPanel panel, String tooltip) {
    button.setToolTipText(tooltip);
    panel.setToolTipText(tooltip);
  }

  protected void processClick() {
    if (transaction == null) {
      return;
    }
    tableView.select(transaction);
    repository.update(transaction.getKey(), Transaction.RECONCILIATION_ANNOTATION_SET, !transaction.isTrue(Transaction.RECONCILIATION_ANNOTATION_SET));
  }

  public String getName() {
    return "";
  }

  public GlobStringifier getStringifier() {
    return new AbstractGlobStringifier() {
      public String toString(Glob transaction, GlobRepository repository) {
        if ((transaction != null) && (transaction.isTrue(Transaction.RECONCILIATION_ANNOTATION_SET))) {
          return "x";
        }
        return "";
      }
    };
  }

  public Comparator<Glob> getComparator() {
    return new GlobFieldsComparator(Transaction.RECONCILIATION_ANNOTATION_SET, true, Transaction.LABEL, false);
  }

  private void selectTransactionIfNeeded(Glob transaction) {
    GlobList selection = tableView.getCurrentSelection();
    if (!selection.contains(transaction)) {
      tableView.select(transaction);
    }
  }
}
