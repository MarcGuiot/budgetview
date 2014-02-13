package org.designup.picsou.gui.seriesgroups;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesGroup;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddToExistingSeriesGroupAction extends AbstractAction {

  private final Key seriesKey;
  private Key seriesGroupKey;
  private final GlobRepository repository;
  private final Directory directory;

  public AddToExistingSeriesGroupAction(Key seriesKey, Glob seriesGroup, GlobRepository repository, Directory directory) {
    super(seriesGroup.get(SeriesGroup.NAME));
    this.seriesKey = seriesKey;
    this.seriesGroupKey = seriesGroup.getKey();
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    repository.update(seriesKey, Series.GROUP, seriesGroupKey.get(SeriesGroup.ID));
  }
}
