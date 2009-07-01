package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RestoreChangePasswordDialog {
  private GlobRepository repository;
  private Directory directory;

  public RestoreChangePasswordDialog(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;

  }

  public char[] show() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/restoreChangePassword.splits", repository, directory);
    final JPasswordField passwordField = builder.add("password", new JPasswordField());
    final PicsouDialog dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    final AbstractAction validate = new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };
    passwordField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (passwordField.getPassword() == null || passwordField.getPassword().length == 0) {
          validate.setEnabled(false);
        }
        else {
          validate.setEnabled(true);
        }
      }
    });

    AbstractAction cancel = new AbstractAction(Lang.get("cancel")) {
      public void actionPerformed(ActionEvent e) {
        passwordField.setText(null);
        dialog.setVisible(false);
      }
    };

    dialog.addPanelWithButtons(builder.<JPanel>load(), validate, cancel);
    dialog.pack();
    dialog.showCentered();

    return passwordField.getPassword();
  }
}
