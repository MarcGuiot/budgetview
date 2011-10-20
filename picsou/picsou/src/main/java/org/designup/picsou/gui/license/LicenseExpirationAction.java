package org.designup.picsou.gui.license;

import org.globsframework.utils.directory.Directory;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class LicenseExpirationAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public LicenseExpirationAction(GlobRepository repository, Directory directory) {
    super("[show expiration]");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    LicenseExpirationDialog dialog = new LicenseExpirationDialog(directory.get(JFrame.class), repository, directory);
    dialog.show();
  }
}
