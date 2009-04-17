package org.designup.picsou.gui.license;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RegisterLicenseAction extends AbstractAction {
  private Frame parent;
  private GlobRepository repository;
  private Directory directory;

  public RegisterLicenseAction(GlobRepository repository, Directory directory) {
    super(Lang.get("license.register"));
    this.parent = directory.get(JFrame.class);
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    LicenseActivationDialog dialog = new LicenseActivationDialog(parent, repository, directory);
    dialog.show(false);
  }
}
