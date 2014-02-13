package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.budget.components.NameLabelPopupButton;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.series.utils.SeriesGroupPopupFactory;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.utils.DisposablePopupMenuFactory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

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
