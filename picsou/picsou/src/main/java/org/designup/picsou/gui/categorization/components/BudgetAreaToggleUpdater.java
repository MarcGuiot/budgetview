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
import java.util.List;
import java.util.Set;

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
    GlobList series = GlobUtils.getTargets(selectedTransactions, Transaction.SERIES, repository);
    Set<Integer> areas = series.getValueSet(Series.BUDGET_AREA);
    if (areas.size() != 1) {
      invisibleToggle.setSelected(true);
      return;
    }

    final Integer selectedAreaId = areas.iterator().next();
    if (budgetArea.getId().equals(selectedAreaId)) {
      toggle.setSelected(true);
      toggle.getAction().actionPerformed(null);
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    updateState();
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
    this.selectedTransactions = GlobList.EMPTY;
  }

  public void dispose() {
    repository.removeChangeListener(this);
    selectionService.removeListener(this);
  }
}
