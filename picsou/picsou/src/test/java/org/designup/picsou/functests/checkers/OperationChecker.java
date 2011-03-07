package org.designup.picsou.functests.checkers;

import junit.framework.TestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Ref;
import org.globsframework.utils.TestUtils;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OperationChecker {
  private MenuItem importMenu;
  private MenuItem exportMenu;
  private MenuItem preferencesMenu;
  private MenuItem undoMenu;
  private MenuItem redoMenu;
  private MenuItem throwExceptionMenu;
  private MenuItem throwExceptionInRepositoryMenu;
  public static final String DEFAULT_ACCOUNT_NUMBER = "11111";
  private Window window;
  private MenuItem checkMenu;
  private MenuItem fileMenu;

  public static OperationChecker init(Window window) {
    return new OperationChecker(window);
  }

  public OperationChecker(Window window) {
    UISpecAssert.waitUntil(window.containsSwingComponent(JMenu.class), 10000);
    this.window = window;
    fileMenu = window.getMenuBar().getMenu("File");
    importMenu = fileMenu.getSubMenu("Import");
    exportMenu = fileMenu.getSubMenu("Export");
    preferencesMenu = fileMenu.getSubMenu("Preferences");

    MenuItem editMenu = window.getMenuBar().getMenu("Edit");
    undoMenu = editMenu.getSubMenu("Undo");
    redoMenu = editMenu.getSubMenu("Redo");
    checkMenu = editMenu.getSubMenu("[Check data (see logs)]");
    throwExceptionMenu = editMenu.getSubMenu("[Throw exception]");
    throwExceptionInRepositoryMenu = editMenu.getSubMenu("Throw exception in repository");
  }

  public ImportDialogChecker openImportDialog() {
    return ImportDialogChecker.open(importMenu.triggerClick());
  }

  public void importOfxFile(String name) {
    importFile(new String[]{name}, null, null, null);
  }

  public void importOfxFile(String name, double initialAmount) {
    importFile(new String[]{name}, null, initialAmount, null);
  }

  public void importOfxWithDeferred(String fileName, String cardAccountName) {
    ImportDialogChecker importDialog = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();
    importDialog
      .openCardTypeChooser()
      .selectDeferredCard(cardAccountName)
      .validate();
    importDialog
      .setMainAccount()
      .doImport()
      .completeLastStep();
  }

  public void importQifFileWithDeferred(String fileName, String bank, double position) {
    ImportDialogChecker importDialog = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();

    importDialog
      .addNewAccount()
      .selectBank(bank)
      .setAccountNumber("1111")
      .setAccountName("card 1111")
      .setAsDeferredCard()
      .validate();

    importDialog.doImportWithBalance()
      .setAmountAndEnter(position);
    importDialog.completeLastStep();
  }

  public void importOfxFile(String name, String bank) {
    importFile(new String[]{name}, bank, null, null);
  }

  public void importOfxFile(String name, Double amount) {
    importFile(new String[]{name}, null, amount, null);
  }

  public void importOfxOnAccount(String fileName, String newAccount, String existingAccount) {
    ImportDialogChecker importDialog = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();
    importDialog.openChooseAccount()
      .associate(newAccount, existingAccount)
      .validate();

    importDialog.completeImport();
  }

  public void importQifFile(String file, String bank) {
    importFile(new String[]{file}, bank, null, null);
  }

  public void importQifFile(String file, String bank, Double amount) {
    importFile(new String[]{file}, bank, amount, null);
  }

  public void importQifFile(String file, String bank, String targetAccount) {
    importFile(new String[]{file}, bank, null, targetAccount);
  }

  public void importQifFiles(String bank, String... files) {
    importFile(files, bank, 0., null);
  }

  private void importFile(final String[] fileNames, final String bank, final Double amount, final String targetAccount) {
    Window dialog = WindowInterceptor.getModalDialog(importMenu.triggerClick());
    TextBox fileField = dialog.getInputTextBox("fileField");
    String txt = "";
    for (String name : fileNames) {
      txt += name + ";";
    }
    fileField.setText(txt);

    dialog.getButton("Import").click();

    ImportDialogChecker importDialog = ImportDialogChecker.create(dialog);

    JButton createFirstAccount = dialog.findSwingComponent(JButton.class, "Create an account");
    if (createFirstAccount != null) {
      importDialog
        .defineAccount(bank, "Main account", DEFAULT_ACCOUNT_NUMBER);
    }
    else if (bank != null && dialog.findSwingComponent(JButton.class, "Select the bank") != null) { // OFX
      importDialog
        .openEntityEditor()
        .selectBank(bank)
        .validate();
    }
    if (targetAccount != null) {
      dialog.getComboBox("accountCombo").select(targetAccount);
    }
    if (importDialog.hasAccountType()) {
      importDialog.setMainAccountForAll();
    }

    final Button step2Button = dialog.getButton(Lang.get("import.step2.ok"));
    for (int i = 0; i < fileNames.length - 2; i++) {
      step2Button.click();
    }
    if (amount != null) {
      Window window = WindowInterceptor.getModalDialog(step2Button.triggerClick());
      AccountPositionEditionChecker accountPosition = new AccountPositionEditionChecker(window);
      accountPosition.setAmount(amount);
      accountPosition.validate();
    }
    else {
      step2Button.click();
    }
    importDialog.checkLastStep();
    importDialog.completeLastStep();
  }

  public void exportOfxFile(String name) {
    WindowInterceptor
      .init(exportMenu.triggerClick())
      .processWithButtonClick("OK")
      .process(FileChooserHandler.init().select(name))
      .run();
  }

  public Trigger getBackupTrigger() {
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    return fileMenu.getSubMenu("Backup").triggerClick();
  }

  public String backup(TestCase test) {
    return backup(TestUtils.getFileName(test));
  }

  public String backup(TestCase test, boolean trial) {
    return backup(TestUtils.getFileName(test), trial);
  }

  public String backup(String filePath) {
    return backup(filePath, false);
  }

  public String backup(String filePath, boolean warnTrial) {
    final Ref<String> selectedFile = new Ref<String>();
    WindowInterceptor interceptor = WindowInterceptor
      .init(getBackupTrigger());
    if (warnTrial) {
      interceptor.process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageDialogChecker checker = new MessageDialogChecker(window);
          checker.checkMessageContains("Backup is possible during the trial period but restore is not possible.");
          return checker.triggerClose();
        }
      });
    }
    interceptor
      .process(FileChooserHandler.init().select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
          dialog.checkMessageContains("Backup done in file");
          selectedFile.set(dialog.getFilePath());
          return dialog.getOkTrigger();
        }
      })
      .run();
    return selectedFile.get();
  }

  public void restore(String filePath) {
    WindowInterceptor
      .init(getRestoreTrigger())
      .process(FileChooserHandler.init().select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
          dialog.checkMessageContains("Restore done");
          return dialog.getOkTrigger();
        }
      })
      .run();
  }

  public void restoreNotAvailable() {
    WindowInterceptor
      .init(getRestoreTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageDialogChecker dialogChecker = new MessageDialogChecker(window);
          dialogChecker.checkMessageContains("Restore is not possible during trial periode.");
          return dialogChecker.triggerClose();
        }
      })
      .run();

  }

  public void restoreWithNewPassword(String filePath, final String password) {
    WindowInterceptor.init(getRestoreTrigger())
      .process(FileChooserHandler.init().select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          PasswordDialogChecker dialog = new PasswordDialogChecker(window);
          dialog.checkTitle("Secure backup");
          dialog.setPassword(password);
          return dialog.getOkTrigger();
        }
      })
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
          dialog.checkMessageContains("Restore done");
          return dialog.getOkTrigger();
        }
      })
      .run();
  }

  public Trigger getRestoreTrigger() {
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    return fileMenu.getSubMenu("Restore").triggerClick();
  }

  public Trigger getRestoreSnapshotTrigger() {
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    return fileMenu.getSubMenu("previous version").triggerClick();
  }

  public Trigger getImportTrigger() {
    return importMenu.triggerClick();
  }

  public Trigger getExportTrigger() {
    return exportMenu.triggerClick();
  }

  public PreferencesChecker openPreferences() {
    return new PreferencesChecker(WindowInterceptor.getModalDialog(preferencesMenu.triggerClick()));
  }

  public void undo() {
    undoMenu.click();
  }

  public boolean isUndoAvailable() {
    return undoMenu.getAwtComponent().isEnabled();
  }

  public void checkUndoAvailable() {
    UISpecAssert.assertTrue(undoMenu.isEnabled());
  }

  public void checkUndoNotAvailable() {
    UISpecAssert.assertFalse(undoMenu.isEnabled());
  }

  public void redo() {
    redoMenu.click();
  }

  public void checkRedoAvailable() {
    UISpecAssert.assertTrue(redoMenu.isEnabled());
  }

  public void checkRedoNotAvailable() {
    UISpecAssert.assertFalse(redoMenu.isEnabled());
  }

  public void logout() {
    window.getMenuBar().getMenu("File").getSubMenu("Logout").click();
  }

  public void exit() {
    window.getMenuBar().getMenu("File").getSubMenu("Exit").click();
  }

  public void deleteUser(String password) {
    MenuItem subMenu = window.getMenuBar().getMenu("File").getSubMenu("Delete");
    PasswordDialogChecker dialogChecker =
      new PasswordDialogChecker(WindowInterceptor.getModalDialog(subMenu.triggerClick()));
    if (password != null) {
      dialogChecker.setPassword(password);
    }
    dialogChecker.validate();
    UISpecAssert.waitUntil(window.containsSwingComponent(JPasswordField.class, "password"), 2000);
  }

  public void deleteAutoLoginUser() {
    MenuItem subMenu = window.getMenuBar().getMenu("File").getSubMenu("Delete");
    ConfirmationDialogChecker confirmationDialogChecker =
      new ConfirmationDialogChecker(WindowInterceptor.getModalDialog(subMenu.triggerClick()));
    confirmationDialogChecker.checkMessageContains("Do you realy want do delete your data?");
    confirmationDialogChecker.validate();
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(getHelpMenu().getSubMenu("Index").triggerClick());
  }

  public HelpChecker openHelp(String title) {
    return HelpChecker.open(getHelpMenu().getSubMenu(title).triggerClick());
  }

  public AboutChecker openAbout() {
    return AboutChecker.open(getHelpMenu().getSubMenu("About").triggerClick());
  }

  public FeedbackDialogChecker openFeedback() {
    MenuItem feedbackMenu = getHelpMenu().getSubMenu("Feedback");
    return FeedbackDialogChecker.init(feedbackMenu.triggerClick());
  }

  private MenuItem getHelpMenu() {
    return window.getMenuBar().getMenu("Help");
  }

  public void backupAndLaunchApplication(String user, String password, Date currentDate) throws Exception {
    File file = File.createTempFile("budgetview", ".snapshot");
    String backupFile = file.getAbsoluteFile().getAbsolutePath();
    file.delete();

    backup(backupFile);
    String javaHome = System.getProperty("java.home");
    String classPath = System.getProperty("java.class.path");
    List<String> args = new ArrayList<String>();
    args.add(javaHome + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java");
    args.add("-Xdebug");
    args.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005");
    args.add("-cp");
    args.add(classPath);
    args.add("-Dsplits.editor.enabled=false");
    args.add("-Dsplits.debug.enabled=false");
    args.add("-D" + PicsouApplication.IS_DATA_IN_MEMORY + "=true");
    args.add("-D" + PicsouApplication.APPNAME + ".log.sout=true");
    if (currentDate != null) {
      args.add("-D" + PicsouApplication.APPNAME + ".today=" + Dates.toString(currentDate));
    }
    args.add("org.designup.picsou.gui.MainWindowLauncher");
    if (user != null) {
      args.add("-u");
      args.add(user);
    }

    if (password != null) {
      args.add("-p");
      args.add(password);
    }
    args.add("-l");
    args.add("fr");
    args.add("-s");
    args.add(backupFile);
    Process process = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
    BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    while (true) {
      Thread.sleep(10);
      try {
        while (inputReader.ready()) {
          String line = inputReader.readLine();
          System.out.println(line);
        }
      }
      catch (IOException e) {
      }
      while (errorReader.ready()) {
        String line = errorReader.readLine();
        System.err.println(line);
      }
      try {
        process.exitValue();
        return;
      }
      catch (IllegalThreadStateException e) {
      }
    }
  }

  public MessageAndDetailsDialogChecker throwExceptionInApplication() {
    return MessageAndDetailsDialogChecker.init(throwExceptionMenu.triggerClick());
  }

  public MessageAndDetailsDialogChecker throwExceptionInRepository() {
    return MessageAndDetailsDialogChecker.init(throwExceptionInRepositoryMenu.triggerClick());
  }

  public void checkDataIsOk() {
    MessageDialogChecker.init(checkMenu.triggerClick())
      .checkMessageContains("No error was found").close();
  }

  public void protect(String userName, String password) {
    MenuItem protectMenu = fileMenu.getSubMenu("Change identifier");
    RenameChecker checker =
      new RenameChecker(WindowInterceptor.getModalDialog(protectMenu.triggerClick()));
    checker.set("password", userName, password);
  }

  public void protectFromAnonymous(String userName, String password) {
    MenuItem protectMenu = fileMenu.getSubMenu("protect");
    RenameChecker checker =
      new RenameChecker(WindowInterceptor.getModalDialog(protectMenu.triggerClick()));
    checker.set(userName, password);
  }

  public void checkProtect() {
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    UISpecAssert.assertThat(fileMenu.contain("protect"));
  }

  public void checkChangeUserName() {
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    UISpecAssert.assertThat(fileMenu.contain("Change identifiers..."));
  }

  public void nextMonth() {
    MenuItem fileMenu = window.getMenuBar().getMenu("Edit");
    fileMenu.getSubMenu("goto to 10 of next month").click();
  }

  public void nextSixDays() {
    MenuItem fileMenu = window.getMenuBar().getMenu("Edit");
    fileMenu.getSubMenu("Add 6 days").click();
  }

  public void hideSignposts() {
    MenuItem fileMenu = window.getMenuBar().getMenu("Edit");
    fileMenu.getSubMenu("[Hide signposts]").click();
  }

  public void dumpData() {
    MenuItem fileMenu = window.getMenuBar().getMenu("Edit");
    fileMenu.getSubMenu("[Dump data]").click();
  }

  public RestoreSnapshotChecker restoreSnapshot() {
    return new RestoreSnapshotChecker(WindowInterceptor.getModalDialog(getRestoreSnapshotTrigger()));
  }
}
