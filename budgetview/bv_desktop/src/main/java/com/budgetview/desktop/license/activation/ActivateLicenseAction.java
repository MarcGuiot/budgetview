package com.budgetview.desktop.license.activation;

import com.budgetview.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ActivateLicenseAction extends AbstractAction {
  private Frame parent;
  private GlobRepository repository;
  private Directory directory;

  public ActivateLicenseAction(GlobRepository repository, Directory directory) {
    this(Lang.get("license.register"), repository, directory);
  }

  public ActivateLicenseAction(String text, GlobRepository repository, Directory directory) {
    super(text);
    this.parent = directory.get(JFrame.class);
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    LicenseActivationDialog dialog = new LicenseActivationDialog(parent, repository, directory);
    dialog.show();
  }
}
