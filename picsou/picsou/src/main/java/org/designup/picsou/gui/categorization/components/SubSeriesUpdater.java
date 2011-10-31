package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.*;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import javax.swing.*;
import java.util.Set;

public class SubSeriesUpdater implements GlobSelectionListener, ChangeSetListener {
  private JToggleButton toggle;
  private JToggleButton invisibleButton;
  private Key seriesKey;
  private Key subSeriesKey;
  private BudgetArea budgetArea;
  private GlobRepository repository;
  private SelectionService selectionService;

  public SubSeriesUpdater(JToggleButton toggle, JToggleButton invisibleButton,
                         Key seriesKey, Key subSeriesKey, BudgetArea budgetArea,
                         GlobRepository repository, SelectionService selectionService) {
    this.toggle = toggle;
    this.invisibleButton = invisibleButton;
    this.seriesKey = seriesKey;
    this.subSeriesKey = subSeriesKey;
    this.budgetArea = budgetArea;
    this.repository = repository;
    this.selectionService = selectionService;
    this.selectionService.addListener(this, Transaction.TYPE);
    this.repository.addChangeListener(this);
    updateToggle(selectionService.getSelection(Transaction.TYPE));
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList selectedTransactions = selection.getAll(Transaction.TYPE);
    updateToggle(selectedTransactions);
  }

  private void updateToggle(GlobList selectedTransactions) {
    Set<Integer> seriesIds = selectedTransactions.getValueSet(Transaction.SERIES);
    Integer seriesId = seriesIds.size() == 1 ? seriesIds.iterator().next() : Series.UNCATEGORIZED_SERIES_ID;

    Set<Integer> subSeriesIds = selectedTransactions.getValueSet(Transaction.SUB_SERIES);
    Integer subSeriesId = subSeriesIds.size() == 1 ? subSeriesIds.iterator().next() : null;

    Glob series = repository.find(KeyBuilder.newKey(Series.TYPE, seriesId));
    // Sur reset series peut etre null parceque le reset sur timeView declenche une selection avant que le reset ne soit
    //arrivé ici.
    if (series == null){
      return;
    }
    boolean isGoodBudgetArea = series.get(Series.BUDGET_AREA).equals(budgetArea.getId());

    if (subSeriesId == null || !isGoodBudgetArea) {
      return;
    }

    Glob subSeries = repository.find(KeyBuilder.newKey(SubSeries.TYPE, subSeriesId));
    if (subSeries != null){
      boolean isGoodSeries = series.getKey().equals(seriesKey);
      boolean isGoodSubSeries = subSeries.getKey().equals(subSeriesKey);
      if (isGoodSeries && isGoodSubSeries) {
        toggle.setSelected(true);
      }
    }
  }

  public void dispose() {
    selectionService.removeListener(this);
    repository.removeChangeListener(this);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      GlobList transactions = selectionService.getSelection(Transaction.TYPE);
      updateToggle(transactions);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}

