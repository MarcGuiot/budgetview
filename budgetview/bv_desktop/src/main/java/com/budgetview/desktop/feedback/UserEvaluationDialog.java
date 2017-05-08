package com.budgetview.desktop.feedback;

import com.budgetview.client.mail.ServerMailingService;
import com.budgetview.desktop.Application;
import com.budgetview.desktop.components.dialogs.CancelAction;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.model.User;
import com.budgetview.model.UserPreferences;
import com.budgetview.shared.license.LicenseConstants;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UserEvaluationDialog {
  private GlobRepository repository;
  private Directory directory;
  private PicsouDialog dialog;
  private JTextArea commentEditor;
  private JTextField emailField;
  private JToggleButton yesToggle;
  private JToggleButton noToggle = new JToggleButton();
  private JButton sendButton;
  private JProgressBar sendingState = new JProgressBar();

  public static void showIfNeeded(GlobRepository repository, Directory directory) {
    if ("true".equalsIgnoreCase(System.getProperty(Application.USER_FEEDBACK_DISABLED))) {
      return;
    }

    Glob userPrefs = repository.find(UserPreferences.KEY);
    if (userPrefs == null){
      return;
    }
    int exitCount = userPrefs.get(UserPreferences.EXIT_COUNT, 0);
    repository.update(UserPreferences.KEY, UserPreferences.EXIT_COUNT, exitCount + 1);
    if (!repository.get(User.KEY).isTrue(User.CONNECTED)) {
      return;
    }

    if (userPrefs.isTrue(UserPreferences.EVALUATION_SHOWN)) {
      return;
    }
    doShow(repository, directory);
  }

  public static void doShow(GlobRepository repository, Directory directory) {
    repository.update(UserPreferences.KEY, UserPreferences.EVALUATION_SHOWN, true);
    UserEvaluationDialog dialog = new UserEvaluationDialog(repository, directory);
    dialog.show();
  }

  private UserEvaluationDialog(GlobRepository repository, final Directory directory) {
    this.repository = repository;
    this.directory = directory;

    dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);

    final GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/feedback/userEvaluationDialog.splits",
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
    builder.add("sendingState", sendingState);

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

    dialog.registerDisposable(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });
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
    return "User evaluation: " + value + " [" + Application.APPLICATION_VERSION + "," + Lang.getLang() + "]";
  }

  private String getMessageText() {
    return commentEditor.getText();
  }

  private void show() {
    dialog.pack();
    dialog.showCentered();
  }

  private class SendAction extends AbstractAction {
    private WaitClosedThread schedule;

    public SendAction() {
      super(Lang.get("feedback.send"));
    }

    public void actionPerformed(ActionEvent e) {
      sendingState.setVisible(true);
      sendingState.setIndeterminate(true);
      directory.get(ServerMailingService.class).sendMail(LicenseConstants.ADMIN_EMAIL,
                                                         emailField.getText(),
                                                         getHeaderText(),
                                                         getMessageText(),
                                                         new ServerMailingService.Listener() {
                                                    public void sent(String mail, String title, String content) {
                                                      messageSent();
                                                      Log.write("[UserEvaluation] Mail sent from " + mail + " - title : " + title + "\n" + content);
                                                    }

                                                    public void sendFailed(String mail, String title, String content) {
                                                      messageSent();
                                                      Log.write("[UserEvaluation] Failed to send mail from " + mail + " - title : " + title + "\n" + content);
                                                    }
                                                  },
                                                         repository);
      schedule = new WaitClosedThread(dialog);
      schedule.setDaemon(true);
      schedule.start();
      setEnabled(false);
    }

    private void messageSent() {
      schedule.close();
      try {
        schedule.join();
      }
      catch (InterruptedException e) {
      }
      dialog.setVisible(false);
    }
  }

  private static class WaitClosedThread extends Thread {
    private boolean closed = false;
    private Dialog dialog;

    private WaitClosedThread(Dialog dialog) {
      this.dialog = dialog;
    }

    public void close(){
      synchronized (this){
        dialog = null;
        closed = true;
        notifyAll();
      }
    }
    public void run() {
      try {
        synchronized (this){
          if (!closed){
            wait(5000);
          }
        }
        if (!closed) {
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              Dialog tmp = dialog;
              if (tmp != null){
                tmp.setVisible(false);
              }
            }
          });
        }
      }
      catch (InterruptedException e1) {
      }
    }
  }
}
