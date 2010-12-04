package org.designup.picsou.gui.actions;

import org.designup.picsou.gui.feedback.FeedbackDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SendFeedbackAction extends AbstractAction {
  private GlobRepository repository;
  private Directory directory;

  public SendFeedbackAction(GlobRepository repository, Directory directory) {
    super(Lang.get("feedback"));
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent e) {
    FeedbackDialog dialog = new FeedbackDialog(directory.get(JFrame.class), repository, directory);
    dialog.show();
  }
}
