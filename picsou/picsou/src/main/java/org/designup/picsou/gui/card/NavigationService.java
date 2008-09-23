package org.designup.picsou.gui.card;

import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.model.Card;
import org.designup.picsou.gui.series.view.SeriesView;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class NavigationService {
  private SelectionService selectionService;
  private CategorizationView categorizationView;
  private SeriesView seriesView;
  private GlobRepository repository;

  public NavigationService(CategorizationView categorizationView,
                           SeriesView seriesView,
                           GlobRepository repository,
                           Directory directory) {
    this.categorizationView = categorizationView;
    this.seriesView = seriesView;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
  }

  public void gotoHome() {
    select(Card.HOME);
  }

  public void gotoCategorization() {
    select(Card.CATEGORIZATION);
  }

  public void gotoCategorization(GlobList transactions) {
    categorizationView.show(transactions);
    gotoCategorization();
  }

  public void gotoData() {
    select(Card.DATA);
  }

  public void gotoDataForSeries(Glob series) {
    seriesView.selectSeries(series);
    gotoData();
  }

  private void select(final Card card) {
    selectionService.select(repository.get(card.getKey()));
  }
}
