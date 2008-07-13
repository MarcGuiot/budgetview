package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import javax.swing.*;
import java.util.List;

public class BudgetAreaToggleUpdater implements GlobSelectionListener, ChangeSetListener {
  private final JToggleButton toggleButton;
  private final Glob budgetArea;
  private GlobRepository repository;
  private Glob transaction;


  public BudgetAreaToggleUpdater(JToggleButton toggleButton, Glob budgetArea, GlobRepository repository) {
    this.toggleButton = toggleButton;
    this.budgetArea = budgetArea;
    this.repository = repository;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList all = selection.getAll(Transaction.TYPE);
    if (all.size() != 1) {
      return;
    }
    transaction = all.get(0);
    updateState(transaction);
  }

  private void updateState(Glob transaction) {
    Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
    if (series != null) {
      boolean isSelected = series.get(Series.BUDGET_AREA).equals(budgetArea.get(BudgetArea.ID));
      toggleButton.setSelected(isSelected);
      if (isSelected) {
        toggleButton.getAction().actionPerformed(null);
      }
    }
    else {
      toggleButton.setSelected(false);
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(transaction.getKey())) {
      transaction = repository.find(transaction.getKey());
      updateState(transaction);
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    if (transaction != null) {
      transaction = repository.find(transaction.getKey());
      updateState(transaction);
    }
  }
}
