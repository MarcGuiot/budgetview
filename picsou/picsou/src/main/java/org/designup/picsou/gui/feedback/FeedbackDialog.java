package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.components.tips.ErrorTip;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Set;

public class FeedbackDialog {
  private PicsouDialog dialog;
  private JTextArea contentEditor;
  private JTextField userMail;
  private JTextField mailSubject;
  private JCheckBox addLogsCheckbox;
  private ErrorTip errorTip = null;
  private ChangeSetListener tipsListener;
  private GlobRepository repository;
  private Directory directory;

  public FeedbackDialog(Window parent, GlobRepository repository, final Directory directory) {
    this.repository = repository;
    this.directory = directory;

    dialog = PicsouDialog.create(parent, directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/feedback/feedbackDialog.splits",
                                                      repository, directory);
    this.contentEditor = new JTextArea();
    builder.add("mailContent", contentEditor);

    String userMail = repository.get(User.KEY).get(User.MAIL);

    this.userMail = new JTextField(userMail);
    builder.add("fromMail", this.userMail);

    this.mailSubject = new JTextField();
    builder.add("mailSubject", this.mailSubject);

    this.addLogsCheckbox = new JCheckBox(Lang.get("feedback.addLogs"));
    builder.add("addLogs", addLogsCheckbox);

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new ValidateAction(),
                               new CancelAction(dialog));
    tipsListener = new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsUpdates(User.CONNECTED)) {
          showConnection();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      }
    };
    repository.addChangeListener(tipsListener);

    showConnection();
  }

  private String getMessageText() {

    StringBuilder builder = new StringBuilder();
    builder
      .append(contentEditor.getText())
      .append("\n\n--------------\n\n")
      .append("version: ").append(PicsouApplication.APPLICATION_VERSION).append("\n");
    if (addLogsCheckbox.isSelected()) {
      File logFile = PicsouApplication.getLogFile();
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

  private void showConnection() {
    Glob user = repository.get(User.KEY);
    if (!user.isTrue(User.CONNECTED) && errorTip == null) {
      errorTip = ErrorTip.showLeft(dialog.getOkButton(), Lang.get("feedback.notConnected"), directory);
    }

    if (user.isTrue(User.CONNECTED) && errorTip != null) {
      errorTip.dispose();
    }

    dialog.getOkButton().setEnabled(user.isTrue(User.CONNECTED));
  }

  public void show() {
    dialog.pack();
    dialog.showCentered();
    repository.removeChangeListener(tipsListener);
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
