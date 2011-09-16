package org.designup.picsou.gui.feedback;

import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.components.CancelAction;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.model.User;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserEvaluationDialog {
  private PicsouDialog dialog;
  private JTextArea commentEditor;
  private JTextField emailField;
  private JToggleButton yesToggle;
  private JToggleButton noToggle = new JToggleButton();
  private Directory directory;
  private JButton sendButton;

  public static void showIfNeeded(GlobRepository repository, Directory directory) {

    // [Regis => Marc] Comment influer là-dessus dans les tests ?
    if (!repository.get(User.KEY).isTrue(User.CONNECTED)) {
      System.out.println("UserEvaluationDialog.showIfNeeded: no connection");
      System.out.flush();
      return;
    }

    Glob userPrefs = repository.get(UserPreferences.KEY);
    if (userPrefs.isTrue(UserPreferences.EVALUATION_SHOWN)) {
      return;
    }
    int exitCount = userPrefs.get(UserPreferences.EXIT_COUNT, 0);
    if (exitCount >= 1) {
      doShow(repository, directory);
    }
    repository.update(UserPreferences.KEY, UserPreferences.EXIT_COUNT, exitCount + 1);
  }

  public static void doShow(GlobRepository repository, Directory directory) {
    System.out.println("UserEvaluationDialog.doShow: ");
    repository.update(UserPreferences.KEY, UserPreferences.EVALUATION_SHOWN, true);
    UserEvaluationDialog dialog = new UserEvaluationDialog(repository, directory);
    dialog.show();
  }

  private UserEvaluationDialog(GlobRepository repository, final Directory directory) {
    this.directory = directory;

    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/feedback/userEvaluationDialog.splits",
                                                      repository, directory);

    Action toggleAction = new AbstractAction() {
      public void actionPerformed(ActionEvent actionEvent) {
        updateSendButton();
      }
    };
    yesToggle = new JToggleButton(toggleAction);
    noToggle = new JToggleButton(toggleAction);
    builder.add("yesToggle", yesToggle);
    builder.add("noToggle", noToggle);
    ButtonGroup group = new ButtonGroup();
    group.add(yesToggle);
    group.add(noToggle);

    this.commentEditor = new JTextArea();
    builder.add("comment", commentEditor);

    JScrollPane scrollPane = new JScrollPane();
    builder.add("scroll", scrollPane);
    scrollPane.addMouseListener(new MouseAdapter() {
      public void mouseEntered(MouseEvent mouseEvent) {
        commentEditor.requestFocus();
      }
    });

    String userMail = repository.get(User.KEY).get(User.EMAIL);
    emailField = new JTextField(userMail);
    builder.add("email", emailField);

    dialog.addPanelWithButtons(builder.<JPanel>load(),
                               new SendAction(),
                               new CancelAction(Lang.get("userEvaluation.discard"), dialog));
    sendButton = dialog.getOkButton();
    updateSendButton();
  }

  private void updateSendButton() {
    sendButton.setEnabled(yesToggle.isSelected() || noToggle.isSelected());
  }

  private String getHeaderText() {
    String value;
    if (yesToggle.isSelected()) {
      value = "yes";
    }
    else if (noToggle.isSelected()) {
      value = "no ";
    }
    else {
      value = "-  ";
    }
    return "User evaluation: " + value;
  }

  private String getMessageText() {

    StringBuilder builder = new StringBuilder();
    builder
      .append(commentEditor.getText())
      .append("\n\n--------------\n\n")
      .append("version: ").append(PicsouApplication.APPLICATION_VERSION).append("\n");

    return builder.toString();
  }

  private void show() {
    dialog.pack();
    dialog.showCentered();
  }

  private class SendAction extends AbstractAction {

    public SendAction() {
      super(Lang.get("feedback.send"));
    }

    public void actionPerformed(ActionEvent e) {
      directory.get(ConfigService.class).sendMail(ConfigService.MAIL_CONTACT,
                                                  emailField.getText(),
                                                  getHeaderText(),
                                                  getMessageText(),
                                                  new ConfigService.Listener() {
                                                    public void sent(String mail, String title, String content) {
                                                      Log.write("Mail sent from " + mail + " title : " + title + "\n" + content);
                                                    }

                                                    public void sendFail(String mail, String title, String content) {
                                                      Log.write("Fail to sent mail from " + mail + " title : " + title + "\n" + content);
                                                    }
                                                  });
      dialog.setVisible(false);
    }
  }
}