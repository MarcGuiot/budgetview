package org.designup.picsou.bank.connectors.webcomponents.utils;

import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class UserAndPasswordPanel {

  private final ActionListener connectAction;
  private final Directory directory;

  private JTextField userCodeField;
  private JButton connectButton;
  private JPasswordField passwordField;
  private JPanel panel;

  public UserAndPasswordPanel(ActionListener connectAction, Directory directory) {
    this.connectAction = connectAction;
    this.directory = directory;
  }

  public void createPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/userAndPasswordPanel.splits");

    userCodeField = new JTextField();
    builder.add("userCode", userCodeField);

    passwordField = new JPasswordField();
    builder.add("password", passwordField);
    passwordField.addActionListener(connectAction);

    connectButton = new JButton(Lang.get("synchro.userAndPassword.connect"));
    builder.add("connectButton", connectButton);
    connectButton.addActionListener(connectAction);

    setEnabled(false);

    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        userCodeField.requestFocus();
      }
    });

    panel = builder.load();
  }

  public void setEnabled(boolean enabled) {
    if (connectButton == null) {
      createPanel();
    }
    connectButton.setEnabled(enabled);
  }

  public void setFieldsEnabled(boolean enabled) {
    userCodeField.setEnabled(enabled);
    passwordField.setEnabled(enabled);
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  public String getUser() {
    return userCodeField.getText();
  }

  public String getPassword() {
    return new String(passwordField.getPassword())  ;
  }
}
