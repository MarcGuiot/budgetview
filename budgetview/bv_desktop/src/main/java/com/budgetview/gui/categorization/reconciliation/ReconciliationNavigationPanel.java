package com.budgetview.gui.categorization.reconciliation;

import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public abstract class ReconciliationNavigationPanel implements GlobSelectionListener {
  private GlobMatcher toReconcileMatcher;
  private GlobRepository repository;
  private Directory directory;

  private JPanel panel;
  private JButton switchToReconciliation;
  private JButton switchToCategorization;

  public ReconciliationNavigationPanel(GlobMatcher toReconcileMatcher, GlobRepository repository, Directory directory) {
    this.toReconcileMatcher = toReconcileMatcher;
    this.repository = repository;
    this.directory = directory;
    createPanel();
    directory.get(SelectionService.class).addListener(this, Transaction.TYPE);
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/categorization/reconciliationNavigationPanel.splits",
                            repository, directory);

    switchToReconciliation =
      new JButton(new AbstractAction(Lang.get("reconciliation.navigation.reconciliation")) {
        public void actionPerformed(ActionEvent actionEvent) {
          showReconciliation();
        }
      });
    builder.add("switchToReconciliation", switchToReconciliation);

    switchToCategorization =
      new JButton(new AbstractAction(Lang.get("reconciliation.navigation.categorization")) {
        public void actionPerformed(ActionEvent actionEvent) {
          showCategorization();
        }
      });

    builder.add("switchToCategorization", switchToCategorization);

    panel = builder.load();

    noSelectionShown();
  }
  
  public void categorizationShown() {
    switchToCategorization.setVisible(false);
    switchToReconciliation.setVisible(true);
  }

  public void reconciliationShown() {
    switchToCategorization.setVisible(true);
    switchToReconciliation.setVisible(false);
  }

  public void noSelectionShown() {
    switchToCategorization.setVisible(false);
    switchToReconciliation.setVisible(false);
  }

  protected abstract void showCategorization();

  protected abstract void showReconciliation();

  public JPanel getPanel() {
    return panel;
  }

  public void selectionUpdated(GlobSelection selection) {
    boolean toReconcile = isToReconcile(selection.getAll(Transaction.TYPE));
    panel.setVisible(toReconcile);
  }

  private boolean isToReconcile(GlobList transactions) {
    if (transactions.size() != 1) {
      return false;
    }
    for (Glob transaction : transactions) {
      if (toReconcileMatcher.matches(transaction, repository)) {
        return true;
      }
    }
    return false;
  }
}
