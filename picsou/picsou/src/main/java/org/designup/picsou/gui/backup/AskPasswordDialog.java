package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AskPasswordDialog {
  private String title;
  private String label;
  private Directory directory;
  private String[] args;

  public AskPasswordDialog(String title, String label, Directory directory, String ...args) {
    this.title = title;
    this.label = label;
    this.directory = directory;
    this.args = args;
  }

  public char[] show() {
    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource(getClass(), "/layout/askPasswordDialog.splits");
    builder.add("title", new JLabel(Lang.get(title, args)));
    builder.add("passwordLabel", new JLabel(Lang.get(label)));
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
