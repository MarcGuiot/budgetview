package org.designup.picsou.bank.importer.webcomponents.utils;

import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
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

    connectButton = new JButton("Connect");
    builder.add("connectButton", connectButton);
    connectButton.addActionListener(connectAction);

    panel = builder.load();
  }

  public void setEnabled() {
    if (connectButton == null) {
      createPanel();
    }
    connectButton.setEnabled(true);
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
