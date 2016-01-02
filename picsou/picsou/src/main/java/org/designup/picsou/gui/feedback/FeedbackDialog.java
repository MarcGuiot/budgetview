package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.components.dialogs.CancelAction;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.dialogs.MessageType;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.server.DisconnectionTip;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.components.AppLogger;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class FeedbackDialog {
  private PicsouDialog dialog;
  private Directory directory;

  private JTextArea contentEditor;
  private JTextField userMail;
  private JCheckBox addLogsCheckbox;

  private DisconnectionTip disconnectionTip;

  public FeedbackDialog(Window parent, GlobRepository repository, final Directory directory) {
    this.directory = directory;

    dialog = PicsouDialog.create(this, parent, directory);

    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/feedback/feedbackDialog.splits",
                                                            repository, directory);
    this.contentEditor = new JTextArea();
    builder.add("mailContent", contentEditor);

    String userMail = repository.get(User.KEY).get(User.EMAIL);
    this.userMail = new JTextField(userMail);
    builder.add("fromMail", this.userMail);

    this.addLogsCheckbox = new JCheckBox(Lang.get("feedback.addLogs"));
    builder.add("addLogs", addLogsCheckbox);

    dialog.registerDisposable(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });
    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new SendAction(),
                               new CancelAction(dialog));
    disconnectionTip = new DisconnectionTip(dialog.getOkButton(), repository, directory);
  }

  private String getMessageText() {

    StringBuilder builder = new StringBuilder();
    builder
      .append("[version:").append(PicsouApplication.APPLICATION_VERSION)
      .append(", lang:").append(Lang.getLang()).append("] ")
      .append(contentEditor.getText());
    if (addLogsCheckbox.isSelected()) {
      File logFile = AppLogger.getLogFile();
      if (logFile.exists()) {
        builder
          .append("logs:\n")
          .append(Files.loadFileToString(logFile.getAbsolutePath()));
      }
      else {
        builder.append("[no log file found]");
      }
    }

    return builder.toString();
  }

  public void show() {
    dialog.pack();
    dialog.showCentered();
    disconnectionTip.dispose();
  }

  private class SendAction extends AbstractAction {

    public SendAction() {
      super(Lang.get("feedback.send"));
    }

    public void actionPerformed(ActionEvent e) {
      String email = userMail.getText();
      directory.get(ConfigService.class).sendMail(ConfigService.SUPPORT_EMAIL,
                                                  email,
                                                  getSubject(email),
                                                  getMessageText(),
                                                  new ConfigService.Listener() {
                                                    public void sent(String mail, String title, String content) {
                                                      showConfirmation();
                                                    }

                                                    public void sendFailed(String mail, String title, String content) {
                                                      showFailure();
                                                    }
                                                  });
    }

    private void showConfirmation() {
      MessageDialog.show("feedback.confirmation.ok.title", MessageType.SUCCESS, directory, "feedback.confirmation.ok.message");
      dialog.setVisible(false);
    }

    private void showFailure() {
      MessageDialog.show("feedback.confirmation.failed.title", MessageType.ERROR, directory, "feedback.confirmation.failed.message");
      dialog.setVisible(false);
    }

    private String getSubject(String email) {
      return "Message sent from: " + email;
    }
  }
}
