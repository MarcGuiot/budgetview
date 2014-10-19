package org.globsframework.gui.actions;

import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SelectGlobAction extends AbstractAction {
  private final SelectionService selectionService;
  private Key key;
  private GlobRepository repository;

  public SelectGlobAction(Key key, String label, GlobRepository repository, Directory directory) {
    super(label);
    this.key = key;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
  }

  public void actionPerformed(ActionEvent actionEvent) {
    Glob glob = repository.find(key);
    if (glob != null) {
      selectionService.select(glob);
    }
  }
}
