package com.budgetview.desktop.backup;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RestoreSnapshotMenuAction extends AbstractAction {
  private Directory directory;
  private GlobRepository repository;

  public RestoreSnapshotMenuAction(Directory directory, GlobRepository repository) {
    super(Lang.get("restore.snapshot"));
    this.directory = directory;
    this.repository = repository;
  }

  public void actionPerformed(ActionEvent e) {
    RestoreSnapshotDialog dialog = new RestoreSnapshotDialog(directory);
    dialog.show(repository);
  }
}
