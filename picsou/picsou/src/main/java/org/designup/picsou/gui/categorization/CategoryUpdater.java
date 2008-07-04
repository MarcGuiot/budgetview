package org.designup.picsou.gui.categorization;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesToCategory;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

class CategoryUpdater implements GlobSelectionListener {
  private JToggleButton toggle;
  private JToggleButton invisibleButton;
  private Glob seriesToCategory;
  private BudgetArea budgetArea;
  private GlobRepository repository;

  public CategoryUpdater(JToggleButton toggle, JToggleButton invisibleButton, Glob seriesToCategory, BudgetArea budgetArea,
                         GlobRepository repository) {
    this.toggle = toggle;
    this.invisibleButton = invisibleButton;
    this.seriesToCategory = seriesToCategory;
    this.budgetArea = budgetArea;
    this.repository = repository;
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList all = selection.getAll(Transaction.TYPE);
    if (all.size() != 1) {
      return;
    }
    Glob transation = all.get(0);
    Glob series = repository.findLinkTarget(transation, Transaction.SERIES);
    if (series != null) {
      boolean isGoodBudgetArea =
        series.get(Series.BUDGET_AREA).equals(budgetArea.getGlob().get(BudgetArea.ID));
      boolean isGoodSeries = series.get(Series.ID).equals(seriesToCategory.get(SeriesToCategory.SERIES));
      boolean isGoodCategory = transation.get(Transaction.CATEGORY).equals(seriesToCategory.get(SeriesToCategory.CATEGORY));
      toggle.setSelected(isGoodBudgetArea && isGoodSeries && isGoodCategory);
    }
    else {
      invisibleButton.setSelected(true);
    }
  }
}
