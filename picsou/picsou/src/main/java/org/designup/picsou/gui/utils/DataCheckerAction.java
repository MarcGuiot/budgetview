package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.utils.datacheck.DataCheckingService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class DataCheckerAction extends AbstractAction {

  public static final String LABEL = "Check data (see logs)";

  private GlobRepository repository;
  private Directory directory;

  public DataCheckerAction(GlobRepository repository, Directory directory) {
    super(LABEL);
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    check(null);
  }

  public boolean check(Throwable exception) {
    DataCheckingService checker = new DataCheckingService(repository, directory);
    return checker.check(exception);
  }
}
