package org.designup.picsou.gui.card;

import org.designup.picsou.gui.model.Card;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class NavigationService {
  private SelectionService selectionService;
  private GlobRepository repository;

  public NavigationService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
  }

  public void gotoHome() {
    select(Card.HOME);
  }

  public void gotoCategorization() {
    select(Card.CATEGORIZATION);
  }

  public void gotoData() {
    select(Card.DATA);
  }

  private void select(final Card card) {
    selectionService.select(repository.get(card.getKey()));
  }
}
