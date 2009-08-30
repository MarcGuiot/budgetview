package org.designup.picsou.gui.startup;

import com.jidesoft.swing.InfiniteProgressPanel;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.components.CustomFocusTraversalPolicy;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.client.ServerAccess;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

public class LoginPanel {
  private JTextField userField = new JTextField(15);
  private JPasswordField passwordField = new JPasswordField(15);
  private JPasswordField confirmPasswordField = new JPasswordField(15);
  protected JLabel confirmPasswordLabel = new JLabel();
  private JButton loginButton = new JButton(new LoginAction());
  private JButton autoLoginButton = new JButton(new AutoLogAction());
  private JCheckBox creationCheckBox = new JCheckBox();
  private JEditorPane messageLabel = new JEditorPane();
  private InfiniteProgressPanel progressPanel = new InfiniteProgressPanel();

  private JComponent[] creationComponents = {confirmPasswordLabel, confirmPasswordField};
  private MainWindow mainWindow;
  private Directory directory;
  private JPanel panel;
  private boolean useDemoAccount;
  private java.util.List<ServerAccess.UserInfo> users;
  private String autoLoginUser;

  public LoginPanel(MainWindow mainWindow, Directory directory) {
    this.mainWindow = mainWindow;
    this.directory = directory;

    this.loginButton.setOpaque(false);
    initPanel();
  }

  public void initFocus() {
    if (Strings.isNullOrEmpty(userField.getText())) {
      userField.requestFocusInWindow();
    }
    else if (passwordField.getPassword().length == 0) {
      passwordField.requestFocusInWindow();
    }
    else if (confirmPasswordField.isVisible() && confirmPasswordField.getPassword().length == 0) {
      confirmPasswordField.requestFocusInWindow();
    }
  }

  private void initPanel() {
    initAutoClear(userField, passwordField, confirmPasswordField);

    passwordField.addActionListener(new LoginAction());
    confirmPasswordField.addActionListener(new LoginAction());
    creationCheckBox.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        clearMessage();
        setVisible(creationComponents, creationCheckBox.isSelected());
        initFocus();
      }
    });

    setVisible(creationComponents, false);

    SplitsBuilder builder = SplitsBuilder.init(directory).setSource(getClass(), "/layout/loginPanel.splits");
    builder.add("name", userField);
    builder.add("password", passwordField);
    builder.add("confirmPassword", confirmPasswordField);
    builder.add("confirmLabel", confirmPasswordLabel);
    builder.add("createAccountCheckBox", creationCheckBox);
    builder.add("message", messageLabel);
    builder.add("autologin", autoLoginButton);
    builder.add("progressPanel", progressPanel);
    GuiUtils.initHtmlComponent(messageLabel);
    builder.add("demoMode", new DemoModeAction());
    builder.add("userlogin", loginButton);
    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        panel = (JPanel)component;
      }
    })
      .load();
  }

  private void initAutoClear(JTextField... textFields) {
    for (JTextField textField : textFields) {
      textField.getDocument().addDocumentListener(new AbstractDocumentListener() {
        protected void documentChanged(DocumentEvent e) {
          clearMessage();
        }
      });
    }
  }

  private void login() {
    boolean createUser = false;
    if (creationCheckBox.isSelected()) {
      if (!userIdAccepted() || !passwordAccepted()) {
        return;
      }
      createUser = true;
    }
    logUser(userField.getText(), passwordField.getPassword(), createUser, false);
  }

  private void logUser(String user, char[] password, boolean createUser, boolean autoLog) {
    if (createUser && !SlaValidationDialog.termsAccepted(mainWindow.getFrame(), directory)) {
      return;
    }

    setComponentsEnabled(false);
    progressPanel.start();

    mainWindow.login(user, password, createUser, useDemoAccount, autoLog);
    useDemoAccount = false;
    autoLoginUser = null;
  }

  private void autoLogin(){
    boolean createUser = autoLoginUser == null;
    logUser("autologUser", "autologUser".toCharArray(), createUser, true);
  }

  public JPanel preparePanelForShow(java.util.List<ServerAccess.UserInfo> users) {
    setComponentsEnabled(true);
    this.users = users;
    autoLoginUser = null;
    for (ServerAccess.UserInfo user : users) {
      if (user.autologgin){
        if (autoLoginUser != null){
          Log.write("Multiple autologgin user " + autoLoginUser +  " " + user.name);
        }
        autoLoginUser = user.name;
        autoLoginButton.setText(Lang.get("login.auto.login"));
      }
    }
    userField.setText(null);
    passwordField.setText(null);
    confirmPasswordField.setText(null);
    setVisible(creationComponents, false);
    creationCheckBox.setSelected(false);
    mainWindow.getFrame().setFocusTraversalPolicy(
      new CustomFocusTraversalPolicy(userField, passwordField, confirmPasswordField,
                                     loginButton, creationCheckBox));
    return panel;
  }

  public void setComponentsEnabled(boolean enabled) {
    this.userField.setEnabled(enabled);
    this.passwordField.setEnabled(enabled);
    this.creationCheckBox.setEnabled(enabled);
    this.confirmPasswordField.setEnabled(enabled);
    this.loginButton.setEnabled(enabled);
  }

  private boolean userIdAccepted() {
    String id = userField.getText();
    if (Strings.isNullOrEmpty(id)) {
      displayErrorMessage("login.user.required");
      return false;
    }
    if (id.length() < 4) {
      displayErrorMessage("login.user.too.short");
      return false;
    }
    return true;
  }

  private boolean passwordAccepted() {
    char[] pwd = passwordField.getPassword();
    if (pwd.length == 0) {
      displayErrorMessage("login.password.required");
      return false;
    }
    if (pwd.length < 4) {
      displayErrorMessage("login.password.too.short");
      return false;
    }
    char[] confirm = confirmPasswordField.getPassword();
    if (confirm.length == 0) {
      displayErrorMessage("login.confirm.required");
      return false;
    }
    if (!Arrays.equals(pwd, confirm)) {
      displayErrorMessage("login.confirm.error");
      return false;
    }
    clearMessage();
    return true;
  }

  private void clearMessage() {
    messageLabel.setText("");
  }

  public void displayErrorText(String message) {
    messageLabel.setText("<html><font color=red>" + message + "</font></html>");
  }

  public void displayErrorMessage(String key) {
    displayErrorText(Lang.get(key));
  }

  public void displayBadPasswordMessage(String key, String complement) {
    messageLabel.setText("<html><font color=red>" + Lang.get(key) + (complement == null ? "" : complement) + "</font></html>");
  }

  public void stopProgressBar() {
    progressPanel.stop();
  }

  private class LoginAction extends AbstractAction {
    public LoginAction() {
      super(Lang.get("login.enter"));
    }

    public void actionPerformed(ActionEvent event) {
      login();
    }
  }

  private class AutoLogAction extends AbstractAction {
    public AutoLogAction() {
      super(Lang.get("login.auto.create.login"));
    }

    public void actionPerformed(ActionEvent event) {
      autoLogin();
    }
  }

  private void setVisible(Component[] components, boolean visible) {
    for (Component component : components) {
      component.setVisible(visible);
    }
  }

  private class DemoModeAction extends AbstractAction {

    private DemoModeAction() {
      super(Lang.get("login.demo"));
    }

    public void actionPerformed(ActionEvent e) {
      useDemoAccount = true;
      userField.setText("");
      passwordField.setText("");
      if (creationCheckBox.isSelected()) {
        creationCheckBox.doClick();
      }
      loginButton.getAction().actionPerformed(null);
    }
  }
}
