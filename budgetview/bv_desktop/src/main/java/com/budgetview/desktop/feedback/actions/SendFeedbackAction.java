package com.budgetview.desktop.feedback.actions;

import com.budgetview.desktop.feedback.FeedbackDialog;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SendFeedbackAction extends AbstractAction {

  private GlobRepository repository;
  private Directory directory;

  public SendFeedbackAction(String text, GlobRepository repository, Directory directory) {
    super(text);
    this.repository = repository;
    this.directory = directory;
  }

  public void actionPerformed(ActionEvent actionEvent) {
    FeedbackDialog dialog = new FeedbackDialog(directory.get(JFrame.class), repository, directory);
    dialog.show();
  }
}
