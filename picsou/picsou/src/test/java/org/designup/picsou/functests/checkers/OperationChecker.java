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

import sun.awt.AWTAutoShutdown;

public class OperationChecker {
  private MenuItem importMenu;
  private MenuItem exportMenu;
  private MenuItem preferencesMenu;
  private MenuItem undoMenu;
  private MenuItem redoMenu;
  private MenuItem dumpMenu;
  private MenuItem throwExceptionMenu;
  private MenuItem throwExceptionInRepositoryMenu;
  public static final String DEFAULT_ACCOUNT_NUMBER = "11111";
  public static final String DEFAULT_ACCOUNT_NAME = "Account n. 11111";
  private Window window;
  private MenuItem checkMenu;

  public static OperationChecker init(Window window) {
    return new OperationChecker(window);
  }

  public OperationChecker(Window window) {
    this.window = window;
    MenuItem fileMenu = window.getMenuBar().getMenu("File");
    importMenu = fileMenu.getSubMenu("Import");
    exportMenu = fileMenu.getSubMenu("Export");
    preferencesMenu = fileMenu.getSubMenu("Preferences");

    MenuItem editMenu = window.getMenuBar().getMenu("Edit");
    undoMenu = editMenu.getSubMenu("Undo");
    redoMenu = editMenu.getSubMenu("Redo");
    dumpMenu = editMenu.getSubMenu("Dump");
    checkMenu = editMenu.getSubMenu("[Check data (see logs)]");
    throwExceptionMenu = editMenu.getSubMenu("Throw exception");
    throwExceptionInRepositoryMenu = editMenu.getSubMenu("Throw exception in repo");
  }

  public ImportChecker openImportDialog() {
    return ImportChecker.open(importMenu.triggerClick());
  }

  public void importOfxFile(String name) {
    importFile(new String[]{name}, null, null, null);
  }

  public void importOfxWithDeferred(String fileName, String cardAccountName, int day) {
    ImportChecker importChecker = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();
    importChecker
      .openCardTypeChooser()
      .selectDeferredCard(cardAccountName, day)
      .validate();
    importChecker.doImport();
  }

  public void importQifFileWithDeferred(String fileName, String bank, double position, int day) {
    ImportChecker importChecker = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();
    importChecker
      .addNewAccount()
      .selectBank(bank)
      .setAccountNumber("1111")
      .setAccountName("card 1111")
      .setAsDeferredCard()
      .setFromBeginningDay(day)
      .validate();
    importChecker.doImportWithBalance()
      .setAmountAndEnterInImport(position);
  }

  public void importOfxFile(String name, String bank) {
    importFile(new String[]{name}, bank, null, null);
  }

  public void importOfxFile(String name, Double amount) {
    importFile(new String[]{name}, null, amount, null);
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
    WindowInterceptor
      .init(importMenu.triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window importDialog) throws Exception {
          TextBox fileField = importDialog.getInputTextBox("fileField");
          String txt = "";
          for (String name : fileNames) {
            txt += name + ";";
          }
          fileField.setText(txt);

          importDialog.getButton("Import").click();
          JButton createFirstAccount = importDialog.findSwingComponent(JButton.class, "Create an account");
          if (createFirstAccount != null){
            ImportChecker.create(importDialog)
              .defineAccount(bank, "Main account", DEFAULT_ACCOUNT_NUMBER);
          }
          else if (bank != null && importDialog.findSwingComponent(JButton.class, "Set the bank") != null){ // OFX
            ImportChecker.create(importDialog).openEntityEditionChecker()
              .selectBank(bank)
              .validate();
          }
          if (targetAccount != null) {
            importDialog.getComboBox("accountCombo").select(targetAccount);
          }
          final Button okButton = importDialog.getButton(Lang.get("import.ok"));
          for (int i = 0; i < fileNames.length - 2; i++) {
            okButton.click();
          }
          Trigger trigger;
          if (amount != null) {
            Window window = WindowInterceptor.getModalDialog(okButton.triggerClick());
            AccountPositionEditionChecker accountPosition = new AccountPositionEditionChecker(window);
            accountPosition.setAmount(amount);
            trigger = accountPosition.triggerValidate();
          }
          else {
            trigger = okButton.triggerClick();
          }
          ImportChecker.ImportCompleteWindowHandler importCompleteWindowHandler =
            new ImportChecker.ImportCompleteWindowHandler(-1, -1);
          return importCompleteWindowHandler.process(WindowInterceptor.getModalDialog(trigger));
        }
      })
      .run();
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
    if (warnTrial){
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

  public void restoreNotAvailable(){
    WindowInterceptor
      .init(getRestoreTrigger())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageDialogChecker dialogChecker  = new MessageDialogChecker(window);
          dialogChecker.checkMessageContains("Restore is not possible during trial periode.");
          return dialogChecker.triggerClose();
        }
      })
      .run();

  }

  public void restoreWithNewPassword(String filePath, final String password){
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

  public boolean isUndoAvailable(){
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
    dialogChecker.setPassword(password);
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
    return HelpChecker.open(window.getMenuBar().getMenu("Help").getSubMenu("Index").triggerClick());
  }

  public AboutChecker openAbout() {
    return AboutChecker.open(window.getMenuBar().getMenu("Help").getSubMenu("About").triggerClick());
  }

  public void backupAndLaunchApplication(String user, String password, Date currentDate) throws Exception {
    File file = File.createTempFile("cashpilot", ".snapshot");
    String backupFile = file.getAbsoluteFile().getAbsolutePath();
    file.delete();

    backup(backupFile);
    String javaHome = System.getProperty("java.home");
    String classPath = System.getProperty("java.class.path");
    List<String> args = new ArrayList<String>();
    args.add(javaHome + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java");
//    args.add("-Xdebug");
//    args.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005");
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
    args.add("-u");
    args.add(user);
    args.add("-p");
    args.add(password);
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

  public void dump() {
    dumpMenu.click();
  }

  public MessageAndDetailsDialogChecker throwExceptionInApplication(){
    return MessageAndDetailsDialogChecker.init(throwExceptionMenu.triggerClick());
  }

  public MessageAndDetailsDialogChecker throwExceptionInRepository(){
    return MessageAndDetailsDialogChecker.init(throwExceptionInRepositoryMenu.triggerClick());
  }

  public void checkOk() {
    MessageAndDetailsDialogChecker.init(checkMenu.triggerClick())
      .checkDetailsContain("Start checking\nEnd checking").close();
  }
}
