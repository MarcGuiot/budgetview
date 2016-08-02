package com.budgetview.desktop.seriesgroups;

import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddToExistingSeriesGroupAction extends AbstractAction {

  private final Key seriesKey;
  private Key seriesGroupKey;
  private final GlobRepository repository;

  public AddToExistingSeriesGroupAction(Key seriesKey, Glob seriesGroup, GlobRepository repository) {
    super(seriesGroup.get(SeriesGroup.NAME));
    this.seriesKey = seriesKey;
    this.seriesGroupKey = seriesGroup.getKey();
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    repository.update(seriesKey, Series.GROUP, seriesGroupKey.get(SeriesGroup.ID));
  }
}
