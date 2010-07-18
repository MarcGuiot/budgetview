package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.utils.datacheck.DataCheckingService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DataCheckerAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public DataCheckerAction(GlobRepository repository, Directory directory) {
    super("[Check data (see logs)]");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    check();
  }

  public boolean check() {
    DataCheckingService checker = new DataCheckingService(repository, directory);
    return checker.check();
  }
}
