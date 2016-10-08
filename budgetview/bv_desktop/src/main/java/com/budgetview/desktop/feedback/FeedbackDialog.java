package com.budgetview.desktop.feedback;

import com.budgetview.client.mail.ServerMailingService;
import com.budgetview.desktop.Application;
import com.budgetview.desktop.components.dialogs.CancelAction;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.components.server.DisconnectionTip;
import com.budgetview.desktop.startup.components.AppLogger;
import com.budgetview.model.User;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.utils.Lang;
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
  private GlobRepository repository;
  private Directory directory;

  private JTextArea contentEditor;
  private JTextField userMail;
  private JCheckBox addLogsCheckbox;

  private DisconnectionTip disconnectionTip;

  public FeedbackDialog(Window parent, GlobRepository repository, final Directory directory) {
    this.repository = repository;
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
      .append("[version:").append(Application.APPLICATION_VERSION)
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
      directory.get(ServerMailingService.class).sendMail(LicenseConstants.SUPPORT_EMAIL,
                                                         email,
                                                         getSubject(email),
                                                         getMessageText(),
                                                         new ServerMailingService.Listener() {
                                                    public void sent(String mail, String title, String content) {
                                                      showConfirmation();
                                                    }

                                                    public void sendFailed(String mail, String title, String content) {
                                                      showFailure();
                                                    }
                                                  },
                                                         repository);
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
