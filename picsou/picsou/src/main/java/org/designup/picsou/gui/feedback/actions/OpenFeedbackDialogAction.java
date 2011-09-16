package org.designup.picsou.gui.feedback.actions;

import org.designup.picsou.gui.feedback.FeedbackDialog;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class OpenFeedbackDialogAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;

  public OpenFeedbackDialogAction(GlobRepository repository, Directory directory) {
    super("[OpenFeedback]");
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    FeedbackDialog dialog = new FeedbackDialog(directory.get(JFrame.class), repository, directory);
    dialog.show();
  }
}
