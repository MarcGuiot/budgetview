package org.designup.picsou.gui.card;

import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.series.view.SeriesView;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NavigationService {
  private SelectionService selectionService;
  private CategorizationView categorizationView;
  private CategoryView categoryView;
  private SeriesView seriesView;
  private GlobRepository repository;

  public NavigationService(CategorizationView categorizationView,
                           CategoryView categoryView,
                           SeriesView seriesView,
                           GlobRepository repository,
                           Directory directory) {
    this.categorizationView = categorizationView;
    this.categoryView = categoryView;
    this.seriesView = seriesView;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
  }

  public void gotoHome() {
    select(Card.HOME);
  }

  public void gotoBudget() {
    select(Card.BUDGET);
  }

  public void gotoCategorization() {
    select(Card.CATEGORIZATION);
  }

  public void gotoCategorization(GlobList transactions) {
    categorizationView.show(transactions);
    gotoCategorization();
  }

  public void gotoDataForSeries(Glob series) {
    seriesView.selectSeries(series);
    categoryView.select(MasterCategory.ALL.getId());
    select(Card.DATA);
  }

  public void gotoData(BudgetArea budgetArea) {
    seriesView.selectBudgetArea(budgetArea);
    categoryView.select(MasterCategory.ALL.getId());
    select(Card.DATA);
  }

  public void gotoData(BudgetArea budgetArea, Glob category) {
    seriesView.selectBudgetArea(budgetArea);
    categoryView.select(category);
    select(Card.DATA);
  }

  private void select(final Card card) {
    selectionService.select(repository.get(card.getKey()));
  }
}
