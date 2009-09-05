package org.designup.picsou.gui.backup;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AskPasswordDialog {
  private String title;
  private String label;
  private String message;
  private Directory directory;
  private String[] argsForMessage;
  private JPasswordField passwordField;
  private PicsouDialog dialog;

  public AskPasswordDialog(String title, String label, String message, Directory directory, String ...argsForMessage) {
    this.title = title;
    this.label = label;
    this.message = message;
    this.directory = directory;
    this.argsForMessage = argsForMessage;
  }

  public char[] show() {
    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource(getClass(), "/layout/askPasswordDialog.splits");
    builder.add("title", new JLabel(Lang.get(title)));
    builder.add("passwordLabel", new JLabel(Lang.get(label)));
    builder.add("message", Gui.createHtmlEditor(Lang.get(message, argsForMessage)));
    final JPasswordField passwordField = builder.add("password", new JPasswordField()).getComponent();
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
    passwordField.requestFocusInWindow();
    dialog.showCentered();

    return passwordField.getPassword();
  }

  private class CancelAction extends AbstractAction {
    private final PicsouDialog dialog;

    public CancelAction(PicsouDialog dialog) {
      super(Lang.get("cancel"));
      this.dialog = dialog;
    }

    public void actionPerformed(ActionEvent e) {
      passwordField.setText(null);
      dialog.setVisible(false);
    }
  }

  private class OkAction extends AbstractAction {
    public OkAction() {
      super(Lang.get("ok"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }
}
