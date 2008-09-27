package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.startup.PreferencesDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PreferencesAction extends AbstractAction {
  private PreferencesDialog dialog;

  public PreferencesAction(GlobRepository repository, Directory directory) {
    super(Lang.get("preferences"));
    JFrame parent = directory.get(JFrame.class);
    dialog = new PreferencesDialog(parent, repository, directory);
  }

  public void actionPerformed(ActionEvent e) {
    dialog.show();
  }
}
