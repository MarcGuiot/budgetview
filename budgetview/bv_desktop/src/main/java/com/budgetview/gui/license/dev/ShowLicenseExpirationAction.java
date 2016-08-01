package com.budgetview.gui.license.dev;

import com.budgetview.gui.license.activation.LicenseExpirationDialog;
import org.globsframework.utils.directory.Directory;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowLicenseExpirationAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public ShowLicenseExpirationAction(GlobRepository repository, Directory directory) {
    super("Show expiration dialog");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    LicenseExpirationDialog dialog = new LicenseExpirationDialog(directory.get(JFrame.class), repository, directory);
    dialog.show();
  }
}
