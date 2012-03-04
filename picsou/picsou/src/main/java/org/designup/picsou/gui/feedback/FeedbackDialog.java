package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.server.DisconnectionTip;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.components.AppLogger;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class FeedbackDialog {
  private PicsouDialog dialog;
  private JTextArea contentEditor;
  private JTextField userMail;
  private JTextField mailSubject;
  private JCheckBox addLogsCheckbox;
  private GlobRepository repository;
  private Directory directory;
  private DisconnectionTip disconnectionTip;

  public FeedbackDialog(Window parent, GlobRepository repository, final Directory directory) {
    this.repository = repository;
    this.directory = directory;

    dialog = PicsouDialog.create(parent, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/feedback/feedbackDialog.splits",
                                                      repository, directory);
    this.contentEditor = new JTextArea();
    builder.add("mailContent", contentEditor);

    String userMail = repository.get(User.KEY).get(User.EMAIL);

    this.userMail = new JTextField(userMail);
    builder.add("fromMail", this.userMail);

    this.mailSubject = new JTextField();
    builder.add("mailSubject", this.mailSubject);

    this.addLogsCheckbox = new JCheckBox(Lang.get("feedback.addLogs"));
    builder.add("addLogs", addLogsCheckbox);

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(),
                               new CancelAction(dialog));
    disconnectionTip = new DisconnectionTip(dialog.getOkButton(), repository, directory);
  }

  private String getMessageText() {

    StringBuilder builder = new StringBuilder();
    builder
      .append(contentEditor.getText())
      .append("\n\n--------------\n\n")
      .append("version: ").append(PicsouApplication.APPLICATION_VERSION).append("\n");
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

  private class ValidateAction extends AbstractAction {

    public ValidateAction() {
      super(Lang.get("feedback.send"));
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(ConfigService.class).sendMail(ConfigService.MAIL_CONTACT,
                                                  userMail.getText(),
                                                  mailSubject.getText(),
                                                  getMessageText(),
                                                  new ConfigService.Listener() {
                                                    public void sent(String mail, String title, String content) {
                                                      Log.write("Mail sent from " + mail + " title : " + title + "\n" + content);
                                                    }

                                                    public void sendFail(String mail, String title, String content) {
                                                      Log.write("Fail to sent mail from " + mail + " title : " + title + "\n" + content);
//                                                      MessageDialog.show("");
                                                    }
                                                  });
      dialog.setVisible(false);
    }
  }
}
