package com.budgetview.desktop.budget;

import com.budgetview.desktop.budget.components.NameLabelPopupButton;
import com.budgetview.desktop.series.utils.SeriesGroupPopupFactory;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SeriesGroupEditionButtons {

  private GlobRepository repository;
  private Directory directory;

  public SeriesGroupEditionButtons(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
}

  public NameLabelPopupButton createPopupButton(Glob seriesGroup) {
    DisposablePopupMenuFactory popupFactory = new SeriesGroupPopupFactory(seriesGroup, repository, directory);
    return new NameLabelPopupButton(seriesGroup.getKey(), popupFactory, repository, directory);
  }
}
