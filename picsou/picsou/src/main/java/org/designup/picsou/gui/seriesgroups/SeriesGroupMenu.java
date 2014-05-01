package org.designup.picsou.gui.seriesgroups;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class SeriesGroupMenu {

  private JMenu menu = new JMenu(Lang.get("seriesGroup.menu"));
  private Key seriesKey;
  private GlobRepository repository;
  private Directory directory;
  private AddToNewSeriesGroupAction addToNewSeriesGroupAction;

  public SeriesGroupMenu(Key seriesKey, GlobRepository repository, Directory directory) {
    this.seriesKey = seriesKey;
    this.repository = repository;
    this.directory = directory;
    this.addToNewSeriesGroupAction = new AddToNewSeriesGroupAction(seriesKey, repository, directory);
  }

  public JMenu getMenu() {
    return menu;
  }

  public void update() {
    menu.removeAll();

    Glob series = repository.get(seriesKey);
    GlobList groups = repository.getAll(SeriesGroup.TYPE,
                                        fieldEquals(SeriesGroup.BUDGET_AREA, series.get(Series.BUDGET_AREA)))
      .sort(SeriesGroup.NAME);
    for (Glob group : groups) {
      menu.add(new JMenuItem(new AddToExistingSeriesGroupAction(seriesKey, group, repository)));
    }
    if (!groups.isEmpty()) {
      menu.addSeparator();
    }

    menu.add(addToNewSeriesGroupAction);
  }
}
