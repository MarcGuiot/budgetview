package com.budgetview.desktop.feedback;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class FeedbackService {

  private GlobRepository repository;
  private Directory directory;

  public FeedbackService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void send() {
    FeedbackDialog dialog = new FeedbackDialog(directory.get(JFrame.class), repository, directory);
    dialog.show();
  }
}
