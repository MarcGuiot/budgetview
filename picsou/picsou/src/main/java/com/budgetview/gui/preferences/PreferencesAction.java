package com.budgetview.gui.preferences;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PreferencesAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public PreferencesAction(GlobRepository repository, Directory directory) {
    super(Lang.get("preferences"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    JFrame parent = directory.get(JFrame.class);
    PreferencesDialog dialog = new PreferencesDialog(parent, repository, directory);
    dialog.show();
  }
}
