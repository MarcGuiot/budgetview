package com.budgetview.desktop.license.dev;

import com.budgetview.desktop.license.activation.LicenseExpirationDialog;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

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
