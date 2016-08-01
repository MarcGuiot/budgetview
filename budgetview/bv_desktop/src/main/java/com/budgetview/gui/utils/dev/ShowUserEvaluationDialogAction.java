package com.budgetview.gui.utils.dev;

import com.budgetview.gui.feedback.UserEvaluationDialog;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowUserEvaluationDialogAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;

  public ShowUserEvaluationDialogAction(GlobRepository repository, Directory directory) {
    super("Show user evaluation dialog");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    UserEvaluationDialog.doShow(repository, directory);
  }
}
