package com.budgetview.desktop.backup;

import com.budgetview.client.exceptions.RemoteException;
import com.budgetview.client.exceptions.UserAlreadyExists;
import com.budgetview.desktop.components.dialogs.MessageDialog;
import com.budgetview.desktop.components.dialogs.MessageType;
import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.startup.components.Passwords;
import com.budgetview.model.User;
import com.budgetview.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetPasswordDialog {
  private GlobRepository repository;
  private Directory directory;

  public SetPasswordDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show() {
    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource(getClass(), "/layout/general/setPasswordDialog.splits");
    final Glob user = repository.get(User.KEY);

    final JPasswordField currentPasswordField =
      builder.add("currentPassword", new JPasswordField()).getComponent();

    if (user.isTrue(User.AUTO_LOGIN)) {
      currentPasswordField.setVisible(false);
      builder.add("title", new JLabel(Lang.get("setPassword.title.protect")));
    }
    else {
      builder.add("title", new JLabel(Lang.get("setPassword.title.change")));
    }

    final JTextField newName = builder.add("newName", new JTextField()).getComponent();
    final JPasswordField newPasswordField =
      builder.add("newPassword", new JPasswordField())
        .getComponent();

    final JPasswordField confirmedPasswordField =
      builder.add("confirmedPassword", new JPasswordField())
        .getComponent();

    final JEditorPane messageLabel = new JEditorPane();
    builder.add("message", messageLabel);
    GuiUtils.initHtmlComponent(messageLabel);

    final Passwords passwords = new Passwords(newPasswordField, confirmedPasswordField) {
      public void displayErrorMessage(String key) {
        setErrorText(key, messageLabel);
      }

      public void clearMessage() {
        messageLabel.setText("");
      }
    };

    final PicsouDialog dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);

    final AbstractAction validate = new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        if (passwords.passwordAccepted()) {
          try {
            char[] passwd;
            if (user.isTrue(User.AUTO_LOGIN)) {
              passwd = user.get(User.NAME).toCharArray();
            }
            else {
              passwd = currentPasswordField.getPassword();
              if (passwd.length == 0) {
                setErrorText("setPassword.missing.password", messageLabel);
              }
            }
            boolean writeOk = directory.get(BackupService.class).rename(newName.getText(),
                                                                        newPasswordField.getPassword(),
                                                                        passwd);
            if (!writeOk) {
              setErrorText("setPassword.write.error", messageLabel);
            }
            else {
              dialog.setVisible(false);
              String messagePrefix = user.isTrue(User.AUTO_LOGIN) ?
                                     "setPassword.write.protect.success." :
                                     "setPassword.write.setPassword.success.";
              MessageDialog.show(messagePrefix + "title", MessageType.SUCCESS, directory,
                                 messagePrefix + "message");
            }
          }
          catch (UserAlreadyExists ex) {
            setErrorText("setPassword.user.error", messageLabel);
          }
          catch (RemoteException ex) {
            setErrorText("setPassword.authentication.error", messageLabel);
          }
        }
      }
    };

    AbstractAction cancel = new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };

    dialog.addPanelWithButtons(builder.<JPanel>load(), validate, cancel);
    dialog.pack();
    if (user.isTrue(User.AUTO_LOGIN)) {
      newName.requestFocusInWindow();
    }
    else {
      currentPasswordField.requestFocusInWindow();
    }
    dialog.showCentered();
    builder.dispose();
  }

  private void setErrorText(String key, JEditorPane messageLabel) {
    messageLabel.setText("<html><font color=red>" + Lang.get(key) + "</font></html>");
  }

}