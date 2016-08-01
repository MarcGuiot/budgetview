package com.budgetview.gui.preferences.dev;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DevOptionsAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public DevOptionsAction(GlobRepository repository, Directory directory) {
    super("Show Dev Options Dialog");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    DevOptionsDialog dialog = new DevOptionsDialog(directory.get(JFrame.class), repository, directory);
    dialog.show();
  }
}
