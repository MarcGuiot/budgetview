package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import javax.swing.*;
import java.util.Set;

public class CategoryUpdater implements GlobSelectionListener, ChangeSetListener {
  private JToggleButton toggle;
  private JToggleButton invisibleButton;
  private Key seriesKey;
  private Key categoryKey;
  private BudgetArea budgetArea;
  private GlobRepository repository;
  private SelectionService selectionService;

  public CategoryUpdater(JToggleButton toggle, JToggleButton invisibleButton,
                         Key seriesKey, Key categoryKey, BudgetArea budgetArea,
                         GlobRepository repository, SelectionService selectionService) {
    this.toggle = toggle;
    this.invisibleButton = invisibleButton;
    this.seriesKey = seriesKey;
    this.categoryKey = categoryKey;
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

    Set<Integer> categoryIds = selectedTransactions.getValueSet(Transaction.CATEGORY);
    Integer categoryId = categoryIds.size() == 1 ? categoryIds.iterator().next() : null;

    Glob series = repository.get(KeyBuilder.newKey(Series.TYPE, seriesId));
    boolean isGoodBudgetArea = series.get(Series.BUDGET_AREA).equals(budgetArea.getId());

    if (categoryId == null || !isGoodBudgetArea) {
      invisibleButton.setSelected(true);
      return;
    }

    Glob category = repository.find(KeyBuilder.newKey(Category.TYPE, categoryId));
    boolean isGoodSeries = series.getKey().equals(seriesKey);
    boolean isGoodCategory = category.getKey().equals(categoryKey);
    if (isGoodSeries && isGoodCategory) {
      toggle.setSelected(true);
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

