package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobUtils;

import javax.swing.*;
import java.util.Set;
import java.util.SortedSet;

public class BudgetAreaToggleUpdater implements GlobSelectionListener, ChangeSetListener {
  private final JToggleButton toggle;
  private final BudgetArea budgetArea;
  private GlobRepository repository;
  private SelectionService selectionService;
  private GlobList selectedTransactions = GlobList.EMPTY;
  private JToggleButton invisibleToggle;

  public BudgetAreaToggleUpdater(final JToggleButton toggle, JToggleButton invisibleToggle,
                                 final Glob budgetAreaGlob,
                                 GlobRepository repository, SelectionService selectionService) {
    this.toggle = toggle;
    this.invisibleToggle = invisibleToggle;
    this.budgetArea = BudgetArea.get(budgetAreaGlob.get(BudgetArea.ID));
    this.repository = repository;
    repository.addChangeListener(this);
    this.selectionService = selectionService;
    selectionService.addListener(this, Transaction.TYPE);
  }

  public void selectionUpdated(GlobSelection selection) {
    this.selectedTransactions = selection.getAll(Transaction.TYPE);
    updateState();
  }

  private void updateState() {
    if (selectedTransactions.isEmpty()) {
      invisibleToggle.doClick(0);
      toggle.setEnabled(false);
      return;
    }
    toggle.setEnabled(true);

    GlobList series = GlobUtils.getTargets(selectedTransactions, Transaction.SERIES, repository);
    SortedSet<Integer> areas = series.getSortedSet(Series.BUDGET_AREA);
    if (areas.size() != 1 || BudgetArea.UNCLASSIFIED.getId().equals(areas.first())) {
      invisibleToggle.doClick(0);
      return;
    }

    final Integer selectedAreaId = areas.first();
    if (budgetArea.getId().equals(selectedAreaId)) {
      toggle.doClick(0);
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      updateState();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    this.selectedTransactions = GlobList.EMPTY;
  }

  public void dispose() {
    repository.removeChangeListener(this);
    selectionService.removeListener(this);
  }
}
