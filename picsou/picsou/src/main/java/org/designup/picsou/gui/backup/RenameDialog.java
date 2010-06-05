package org.designup.picsou.gui.backup;

import org.designup.picsou.client.exceptions.RemoteException;
import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.startup.Passwords;
import org.designup.picsou.model.User;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class RenameDialog {
  private GlobRepository repository;
  private Directory directory;

  public RenameDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
  }

  public void show() {
    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource(getClass(), "/layout/general/renameUserDialog.splits");
    final Glob user = repository.get(User.KEY);

    final JPasswordField currentPasswordField =
      builder.add("currentPassword", new JPasswordField())
        .getComponent();

    if (user.isTrue(User.AUTO_LOGIN)) {
      currentPasswordField.setVisible(false);
      builder.add("title", new JLabel(Lang.get("rename.title.protect")));
    }
    else {
      builder.add("title", new JLabel(Lang.get("rename.title.change")));
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

    final PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);

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
                setErrorText("rename.missing.password", messageLabel);
              }
            }
            boolean writeOk = directory.get(BackupService.class).rename(newName.getText(),
                                                                        newPasswordField.getPassword(),
                                                                        passwd);
            if (!writeOk) {
              setErrorText("rename.write.error", messageLabel);
            }
            else {
              dialog.setVisible(false);
            }
          }
          catch (RemoteException found) {
            setErrorText("rename.authentication.error", messageLabel);
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
      currentPasswordField.requestFocusInWindow();
    }
    else {
      newName.requestFocusInWindow();
    }
    dialog.showCentered();
    builder.dispose();
  }

  private void setErrorText(String key, JEditorPane messageLabel) {
    messageLabel.setText("<html><font color=red>" + Lang.get(key) + "</font></html>");
  }

}