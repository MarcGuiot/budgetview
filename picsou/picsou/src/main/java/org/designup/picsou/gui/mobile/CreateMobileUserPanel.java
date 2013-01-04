package org.designup.picsou.gui.mobile;

import org.designup.picsou.model.UserPreferences;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CreateMobileUserPanel {
  private Directory directory;
  private GlobRepository repository;
  private JEditorPane message;
  private JProgressBar progressBar;

  public CreateMobileUserPanel(Directory directory, GlobRepository repository) {
    this.directory = directory;
    this.repository = repository;
    message = new JEditorPane();
    this.message.setContentType("text/html");
    progressBar = new JProgressBar();
  }

  public JPanel create() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/mobile/createAccountPanel.splits",
                                                      repository, directory);
    builder.addEditor("mail", UserPreferences.MAIL_FOR_MOBILE);
    builder.addEditor("password", UserPreferences.PASSWORD_FOR_MOBILE);
    builder.add("message", message);
    builder.add("progress", progressBar);
    return builder.load();
  }

  public void setMessage(String message){
    this.message.setText(message);
  }

  public void stopProgress() {
    progressBar.setVisible(false);
  }

  public void startProgress() {
    progressBar.setVisible(true);
    progressBar.setIndeterminate(true);
  }
}
