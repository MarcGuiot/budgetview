package com.budgetview.gui.categorization.reconciliation;

import com.budgetview.gui.categorization.components.CategorizationTableView;
import com.budgetview.gui.transactions.columns.TransactionRendererColors;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ReconciliationPanel {
  private TransactionRendererColors colors;
  private GlobRepository repository;
  private Directory localDirectory;

  private JPanel panel;
  private GlobTableView tableView;
  private ReferenceTransactionComparator comparator;
  private ReconcileAction reconcileAction;
  private KeepManualTransactionAction keepManualTransactionAction;

  public ReconciliationPanel(TransactionRendererColors colors, GlobRepository repository, Directory directory) {
    this.colors = colors;
    this.repository = repository;
    this.localDirectory = new DefaultDirectory(directory);
    this.localDirectory.add(new SelectionService());
    createPanel();
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/categorization/reconciliationPanel.splits",
                            repository, localDirectory);

    comparator = new ReferenceTransactionComparator();
    tableView =
      CategorizationTableView.createTransactionTable("possibleTransactions", builder, colors,
                                                     comparator,
                                                     LabelCustomizer.NO_OP,
                                                     repository, localDirectory)
        .setFilter(GlobMatchers.NONE);

    reconcileAction = new ReconcileAction(repository, localDirectory);
    builder.add("reconcile", reconcileAction);

    keepManualTransactionAction = new KeepManualTransactionAction(repository);
    builder.add("keepManual", keepManualTransactionAction);

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void update(Glob transaction) {
    comparator.setCurrent(transaction);
    tableView.setFilter(new ReferenceTransactionFilter(transaction));
    reconcileAction.setTransactionToReconcile(transaction);
    keepManualTransactionAction.setTransaction(transaction);
  }

}
