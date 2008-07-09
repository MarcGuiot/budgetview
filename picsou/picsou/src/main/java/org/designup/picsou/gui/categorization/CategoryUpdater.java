package org.designup.picsou.gui.categorization;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;

class CategoryUpdater implements GlobSelectionListener {
  private JToggleButton toggle;
  private JToggleButton invisibleButton;
  private Key seriesKey;
  private Key categoryKey;
  private BudgetArea budgetArea;
  private GlobRepository repository;

  public CategoryUpdater(JToggleButton toggle, JToggleButton invisibleButton,
                         Key seriesKey, Key categoryKey, BudgetArea budgetArea,
                         GlobRepository repository) {
    this.toggle = toggle;
    this.invisibleButton = invisibleButton;
    this.seriesKey = seriesKey;
    this.categoryKey = categoryKey;
    this.budgetArea = budgetArea;
    this.repository = repository;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList selectedTransactions = selection.getAll(Transaction.TYPE);
    if (selectedTransactions.size() != 1) {
      return;
    }
    Glob transaction = selectedTransactions.get(0);
    Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
    Glob category = repository.findLinkTarget(transaction, Transaction.CATEGORY);
    if ((series == null) || (category == null)) {
      invisibleButton.setSelected(true);
      return;
    }

    boolean isGoodBudgetArea = series.get(Series.BUDGET_AREA).equals(budgetArea.getGlob().get(BudgetArea.ID));
    boolean isGoodSeries = series.getKey().equals(seriesKey);
    boolean isGoodCategory = category.getKey().equals(categoryKey);
    toggle.setSelected(isGoodBudgetArea && isGoodSeries && isGoodCategory);
  }
}

