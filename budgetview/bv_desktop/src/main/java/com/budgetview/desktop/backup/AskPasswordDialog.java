package com.budgetview.desktop.backup;

import com.budgetview.desktop.components.dialogs.PicsouDialog;
import com.budgetview.desktop.utils.Gui;
import com.budgetview.utils.Lang;
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

  public AskPasswordDialog(String title, String label, String message, Directory directory, String... argsForMessage) {
    this.title = title;
    this.label = label;
    this.message = message;
    this.directory = directory;
    this.argsForMessage = argsForMessage;
  }

  public char[] show() {
    SplitsBuilder builder = new SplitsBuilder(directory);
    builder.setSource(getClass(), "/layout/general/askPasswordDialog.splits");
    builder.add("title", new JLabel(Lang.get(title)));
    builder.add("passwordLabel", new JLabel(Lang.get(label)));
    builder.add("message", Gui.createHtmlEditor(Lang.get(message, argsForMessage)));
    final JPasswordField passwordField =
      builder.add("password", new JPasswordField())        
        .getComponent();
    final PicsouDialog dialog = PicsouDialog.create(this, directory.get(JFrame.class), directory);

    final AbstractAction validate = new AbstractAction(Lang.get("ok")) {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    };
    passwordField.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
       dialog.setVisible(false);
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
    builder.dispose();
    return passwordField.getPassword();
  }

}
