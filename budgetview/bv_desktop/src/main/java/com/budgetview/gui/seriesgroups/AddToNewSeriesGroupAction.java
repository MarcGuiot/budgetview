package com.budgetview.gui.seriesgroups;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AddToNewSeriesGroupAction extends AbstractAction {

  private final Key seriesKey;
  private final GlobRepository repository;
  private final Directory directory;

  public AddToNewSeriesGroupAction(Key seriesKey, GlobRepository repository, Directory directory) {
    super(Lang.get("seriesGroup.menu.addToNew"));
    this.seriesKey = seriesKey;
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    CreateSeriesGroupDialog.show(seriesKey, repository, directory);
  }
}
