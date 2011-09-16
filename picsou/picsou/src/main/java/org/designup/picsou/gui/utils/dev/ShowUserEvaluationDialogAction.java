package org.designup.picsou.gui.utils.dev;

import org.designup.picsou.gui.feedback.UserEvaluationDialog;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowUserEvaluationDialogAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;

  public ShowUserEvaluationDialogAction(GlobRepository repository, Directory directory) {
    super("[Show user evaluation dialog]");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    UserEvaluationDialog.doShow(repository, directory);
  }
}
