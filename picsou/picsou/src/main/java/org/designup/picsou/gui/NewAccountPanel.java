package org.designup.picsou.gui;

import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.Strings;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.SplitsBuilder;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.components.JStyledPanel;
import org.crossbowlabs.splits.utils.ToggleVisibilityAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class NewAccountPanel {
  private JLabel messageLabel = new JLabel();
  private JStyledPanel filePanel = new JStyledPanel();
  private JLabel fileLabel = new JLabel();
  private JTextField fileField = new JTextField();
  private JButton fileButton = new JButton();
  private JPanel panel;
  private JButton loginButton = new JButton(new LoginAction());
  private GlobRepository repository;
  private Directory directory;
  private MainWindow mainWindow;
  private File[] files;

  public static void show(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    NewAccountPanel panel = new NewAccountPanel(repository, directory, mainWindow);
    mainWindow.fadeTo(panel.panel);
  }

  private NewAccountPanel(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    this.repository = repository;
    this.directory = directory;
    this.mainWindow = mainWindow;
    fileButton.setAction(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        files = ImportFileAction.queryFile(panel);
        if (files != null) {
          StringBuffer buffer = new StringBuffer();
          for (int i = 0; i < files.length; i++) {
            buffer.append(files[i].getPath());
            if (i + 1 < files.length) {
              buffer.append(";");
            }
          }
          fileField.setText(buffer.toString());
        }
      }
    });

    ColorService colorService = PicsouColors.createColorService();
    SplitsBuilder builder = new SplitsBuilder(colorService, Gui.ICON_LOCATOR, Lang.TEXT_LOCATOR);
    builder.add("message", messageLabel);
    builder.add("filePanel", filePanel);
    builder.add("fileLabel", fileLabel);
    builder.add("fileField", fileField);
    builder.add("fileButton", fileButton);
    JStyledPanel helpPanel = new JStyledPanel();
    builder.add("toggleHelp", new ToggleVisibilityAction(helpPanel));
    builder.add("helpPanel", helpPanel);
    builder.add("login", loginButton);
    panel = (JPanel) builder.parse(getClass(), "/layout/newAccountPanel.splits");

  }

  private File[] getInitialFile() {
    String path = fileField.getText();
    String[] strings = path.split(";");
    File[] files = new File[strings.length];
    for (int i = 0; i < strings.length; i++) {
      String string = strings[i];
      files[i] = new File(string);
    }
    if (Strings.isNullOrEmpty(path)) {
      return null;
    }
    return files;
  }

  private boolean initialFileAccepted() {
    String path = fileField.getText();
    if (Strings.isNullOrEmpty(path)) {
      displayErrorMessage("login.data.file.required");
      return false;
    }

    String[] strings = path.split(";");
    for (String string : strings) {
      File file = new File(string);
      if (!file.exists()) {
        displayErrorMessage("login.data.file.not.found");
        return false;
      }
    }
    return true;
  }

  private void login() {
    if (!initialFileAccepted()) {
      return;
    }
    try {
      MainPanel.show(repository, directory, mainWindow, getInitialFile());
    }
    catch (IOException e) {
      displayErrorMessage("");
    }
  }

  private void displayErrorMessage(String key) {
    messageLabel.setText("<html><font color=red>" + Lang.get(key) + "</font></html>");
  }

  private class LoginAction extends AbstractAction {
    public LoginAction() {
      super("Login");
    }

    public void actionPerformed(ActionEvent event) {
      login();
    }
  }
}
