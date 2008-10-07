package org.designup.picsou.gui.about;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class AboutAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public AboutAction(GlobRepository repository, Directory directory) {
    super(Lang.get("about.menu"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    AboutDialog dialog = new AboutDialog(repository, directory);
    dialog.show();
  }
}
