package org.designup.picsou.bank.connectors.webcomponents.utils;

import org.designup.picsou.bank.connectors.AbstractBankConnector;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserAndPasswordPanel {

  private final Action connectAction;
  private final Directory directory;

  private JTextField userCodeField;
  private JButton connectButton;
  private JPasswordField passwordField;
  private JPanel panel;

  public UserAndPasswordPanel(Action connectAction, Directory directory) {
    this.connectAction = connectAction;
    this.directory = directory;
  }

  public JPanel createPanel(AbstractBankConnector connector) {
    final SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/userAndPasswordPanel.splits");

    userCodeField = new JTextField();
    builder.add("userCode", userCodeField);

    passwordField = new JPasswordField();
    builder.add("password", passwordField);
    passwordField.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        if (connectAction.isEnabled()) {
          connectAction.actionPerformed(e);
        }
      }
    });

    connectAction.putValue(Action.NAME, Lang.get("synchro.userAndPassword.connect"));
    connectButton = new JButton(connectAction);
    builder.add("connectButton", connectButton);

    setEnabled(false);

    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        userCodeField.requestFocus();
      }
    });

    panel = builder.load();
    connector.addToBeDisposed(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });
    return panel;
  }

  public void setEnabled(boolean enabled) {
    connectAction.setEnabled(enabled);
  }

  public void setFieldsEnabled(boolean enabled) {
    userCodeField.setEnabled(enabled);
    passwordField.setEnabled(enabled);
  }

  public JPanel getPanel() {
    return panel;
  }

  public String getUser() {
    return userCodeField.getText();
  }

  public String getPassword() {
    return new String(passwordField.getPassword());
  }

  public void requestFocus() {
    if (userCodeField.hasFocus() || passwordField.hasFocus()) {
      return;
    }
    if (Strings.isNotEmpty(getUser()) && Strings.isNullOrEmpty(getPassword())) {
      passwordField.requestFocus();
    }
    else {
      userCodeField.requestFocus();
    }
  }

  public void reset() {
    userCodeField.setText("");
    passwordField.setText("");
  }

  public void setUserCode(String code) {
    userCodeField.setText(code);
  }
}
