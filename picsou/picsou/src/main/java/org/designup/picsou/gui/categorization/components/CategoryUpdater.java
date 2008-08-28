package org.designup.picsou.gui.categorization.components;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.*;

import javax.swing.*;
import java.util.Set;

public class CategoryUpdater implements GlobSelectionListener {
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
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList selectedTransactions = selection.getAll(Transaction.TYPE);

    Set<Integer> seriesIds = selectedTransactions.getValueSet(Transaction.SERIES);
    Integer seriesId = seriesIds.size() == 1 ? seriesIds.iterator().next() : Series.UNCATEGORIZED_SERIES_ID;

    Set<Integer> categoryIds = selectedTransactions.getValueSet(Transaction.CATEGORY);
    Integer categoryId = categoryIds.size() == 1 ? categoryIds.iterator().next() : null;

    if ((Series.UNCATEGORIZED_SERIES_ID.equals(seriesId)) || (categoryId == null)) {
      invisibleButton.setSelected(true);
      return;
    }

    Glob series = repository.get(KeyBuilder.newKey(Series.TYPE, seriesId));
    Glob category = repository.get(KeyBuilder.newKey(Category.TYPE, categoryId));

    boolean isGoodBudgetArea = series.get(Series.BUDGET_AREA).equals(budgetArea.getId());
    boolean isGoodSeries = series.getKey().equals(seriesKey);
    boolean isGoodCategory = category.getKey().equals(categoryKey);
    toggle.setSelected(isGoodBudgetArea && isGoodSeries && isGoodCategory);
  }

  public void dispose() {
    selectionService.removeListener(this);
  }
}

