package com.budgetview.gui.seriesgroups;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RenameSeriesGroupAction extends AbstractAction {
  private Key groupKey;
  private GlobRepository repository;
  private Directory directory;

  public RenameSeriesGroupAction(Key groupKey, GlobRepository repository, Directory directory) {
    super(Lang.get("seriesGroup.menu.rename"));
    this.groupKey = groupKey;
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    RenameSeriesGroupDialog.show(groupKey, repository, directory);
  }
}
