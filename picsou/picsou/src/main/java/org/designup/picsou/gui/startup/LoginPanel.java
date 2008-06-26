package org.designup.picsou.gui.startup;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.exceptions.BadPassword;
import org.designup.picsou.client.exceptions.UserAlreadyExists;
import org.designup.picsou.client.exceptions.UserNotRegistered;
import org.designup.picsou.client.http.*;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.gui.MainPanel;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.PicsouInit;
import org.designup.picsou.gui.components.JWavePanel;
import org.designup.picsou.server.ServerDirectory;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

public class LoginPanel {

  private ServerAccess serverAccess;

  private JPanel panel;
  private JTextField userField = new JTextField(15);
  private JPasswordField passwordField = new JPasswordField(15);
  private JPasswordField confirmPasswordField = new JPasswordField(15);
  protected JLabel confirmPasswordLabel = new JLabel();
  private JButton loginButton = new JButton(new LoginAction());
  private JCheckBox creationCheckBox = new JCheckBox();
  private JLabel messageLabel = new JLabel();

  private JComponent[] creationComponents = {
    confirmPasswordLabel, confirmPasswordField
  };
  private MainWindow mainWindow;
  private Directory directory;
  private ServerDirectory serverDirectory;

  public LoginPanel(String remoteAdress, String prevaylerPath, boolean dataInMemory,
                    MainWindow mainWindow, Directory directory) {
    this.mainWindow = mainWindow;
    this.directory = directory;
    initServerAccess(remoteAdress, prevaylerPath, dataInMemory);
    mainWindow.getFrame().addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        serverAccess.takeSnapshot();
        serverAccess.disconnect();
        serverDirectory.close();
      }
    });
    this.loginButton.setOpaque(false);
    initPanel();
  }

  public void initFocus() {
    userField.requestFocusInWindow();
  }

  private void initPanel() {
    initFocusChain(userField, passwordField, confirmPasswordField, loginButton, creationCheckBox);

    passwordField.addActionListener(new LoginAction());
    confirmPasswordField.addActionListener(new LoginAction());
    creationCheckBox.addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        if (creationCheckBox.isSelected()) {
          clearMessage();
        }
        setVisible(creationComponents, creationCheckBox.isSelected());
        initFocus();
      }
    });

    setVisible(creationComponents, false);

    ColorService colorService = directory.get(ColorService.class);
    SplitsBuilder.init(directory)
      .setSource(getClass(), "/layout/loginPanel.splits")
      .add("wave", new JWavePanel(colorService))
      .add("name", userField)
      .add("password", passwordField)
      .add("confirmPassword", confirmPasswordField)
      .add("confirmLabel", confirmPasswordLabel)
      .add("createAccountCheckBox", creationCheckBox)
      .add("message", messageLabel)
      .add("login", loginButton)
      .addLoader(new SplitsLoader() {
        public void load(Component component) {
          panel = (JPanel)component;
          mainWindow.setPanel(panel);
        }
      })
      .load();
  }

  private void login() {
    panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try {
      String user = userField.getText();
      char[] password = passwordField.getPassword();
      if (creationCheckBox.isSelected()) {
        if (!userIdAccepted() || !passwordAccepted()) {
          return;
        }
        serverAccess.createUser(user, password);
        PicsouInit init = PicsouInit.init(serverAccess, user, creationCheckBox.isSelected(), directory);
        NewUserPanel.show(init.getRepository(), init.getDirectory(), mainWindow);
      }
      else {
        serverAccess.initConnection(user, password, false);
        PicsouInit init = PicsouInit.init(serverAccess, user, creationCheckBox.isSelected(), directory);
        MainPanel.show(init.getRepository(), init.getDirectory(), mainWindow);
      }
    }
    catch (UserAlreadyExists e) {
      displayErrorMessageFromKey("login.user.exists");
    }
    catch (BadPassword e) {
      displayErrorMessageFromKey("login.invalid.credentials");
    }
    catch (UserNotRegistered e) {
      displayErrorMessageFromKey("login.invalid.credentials");
    }
    catch (PasswordBasedEncryptor.EncryptFail e) {
      displayBadPasswordMessage("login.password.error", e.getMessage());
    }
    catch (InvalidData e) {
      displayMessage(e.getMessage());
      e.printStackTrace();
    }
    catch (Exception e) {
      displayErrorMessageFromKey("login.server.connection.failure");
      StringWriter stringWriter = new StringWriter();
      PrintWriter writer = new PrintWriter(stringWriter);
      e.printStackTrace(writer);
      e.printStackTrace();
      JTextArea textArea = new JTextArea(stringWriter.toString());
      GuiUtils.show(textArea);
    }
    finally {
      panel.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
  }

  private boolean userIdAccepted() {
    String id = userField.getText();
    if (Strings.isNullOrEmpty(id)) {
      displayErrorMessageFromKey("login.user.required");
      return false;
    }
    if (id.length() < 4) {
      displayErrorMessageFromKey("login.user.too.short");
      return false;
    }
    return true;
  }

  private boolean passwordAccepted() {
    char[] pwd = passwordField.getPassword();
    if (pwd.length == 0) {
      displayErrorMessageFromKey("login.password.required");
      return false;
    }
    if (pwd.length < 6) {
      displayErrorMessageFromKey("login.password.too.short");
      return false;
    }
    if (!containsSpecialChar(pwd)) {
      displayErrorMessageFromKey("login.password.special.chars");
      return false;
    }
    char[] confirm = confirmPasswordField.getPassword();
    if (confirm.length == 0) {
      displayErrorMessageFromKey("login.confirm.required");
      return false;
    }
    if (!Arrays.equals(pwd, confirm)) {
      displayErrorMessageFromKey("login.confirm.error");
      return false;
    }
    clearMessage();
    return true;
  }

  private void clearMessage() {
    messageLabel.setText("");
  }

  private boolean containsSpecialChar(char[] pwd) {
    for (char c : pwd) {
      if (!Character.isLetter(c)) {
        return true;
      }
    }
    return false;
  }

  private void displayMessage(String mesage) {
    messageLabel.setText("<html><font color=red>" + mesage + "</font></html>");
  }

  private void displayErrorMessageFromKey(String key) {
    messageLabel.setText("<html><font color=red>" + Lang.get(key) + "</font></html>");
  }

  private void displayBadPasswordMessage(String key, String complement) {
    messageLabel.setText("<html><font color=red>" + Lang.get(key) + (complement == null ? "" : complement) + "</font></html>");
  }

  private void initFocusChain(JComponent... components) {
    for (int i = 0; i < components.length - 1; i++) {
      components[i].setNextFocusableComponent(components[i + 1]);
    }
    components[components.length - 1].setNextFocusableComponent(components[0]);
  }

  private void initServerAccess(String remoteAdress, String prevaylerPath, boolean dataInMemory) {
    serverDirectory = new ServerDirectory(prevaylerPath, dataInMemory);
    EncrypterToTransportServerAccess localServerAccess =
      new EncrypterToTransportServerAccess(new LocalClientTransport(serverDirectory.getServiceDirectory()));
    ServerAccess remoteAccess = null;
    if (remoteAdress.startsWith("http")) {
      remoteAccess = new ConnectionRetryServerAccess(
        new EncrypterToTransportServerAccess(new HttpsClientTransport(remoteAdress)));
    }
    serverAccess = new DispatcherServerAccess(localServerAccess, remoteAccess);
  }

  public JPanel getJPanel() {
    return panel;
  }

  private class LoginAction extends AbstractAction {
    public LoginAction() {
      super("Login");
    }

    public void actionPerformed(ActionEvent event) {
      login();
    }
  }

  private void setVisible(Component[] components, boolean visible) {
    for (Component component : components) {
      component.setVisible(visible);
    }
  }

  public ServerDirectory getServerDirectory() {
    return serverDirectory;
  }
}
