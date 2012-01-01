package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.feedback.UserEvaluationDialog;
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

import static org.uispec4j.assertion.UISpecAssert.*;

public class OperationChecker {
  public static final String DEFAULT_ACCOUNT_NUMBER = "11111";
  private Window window;

  public static OperationChecker init(Window window) {
    return new OperationChecker(window);
  }

  public OperationChecker(Window window) {
    UISpecAssert.waitUntil(window.containsSwingComponent(JMenu.class), 10000);
    this.window = window;
  }

  public ImportDialogChecker openImportDialog() {
    return ImportDialogChecker.open(getImportMenu().triggerClick());
  }

  public void importOfxFile(String name) {
    ImportDialogChecker importDialogChecker = openImportDialog()
      .setFilePath(name)
      .acceptFile();

    int count = 0;
    while (!importDialogChecker.isLastStep() && count < 10){
      if (importDialogChecker.isNew()){
        importDialogChecker.setMainAccount();
      }
      count++;
      if (!importDialogChecker.isLastStep()){
        importDialogChecker.checkNoErrorMessage();
      }
      importDialogChecker.doImport();
    }

    importDialogChecker.complete();
  }

  public void importOfxFile(String name, double initialAmount) {
    openImportDialog()
      .selectFiles(name)
      .acceptFile()
      .setPosition(initialAmount)
      .setMainAccount()
      .completeImport();
  }

  public void importOfxWithDeferred(String fileName, String cardAccountName) {
    openImportDialog()
      .importDeferred(cardAccountName, fileName, true);
  }

  public void importWithNewAccount(String fileName, String accountName) {
    ImportDialogChecker importDialog = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();

    importDialog.addNewAccount()
      .setAccountNumber(accountName)
      .setMainAccount()
      .setAccountName(accountName);
    importDialog.doImport();
    importDialog.completeLastStep();
  }

  public void importQifFileWithDeferred(String fileName, String bank, Double position) {
    ImportDialogChecker importDialog = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();

    ImportDialogChecker accountEditionChecker = importDialog.addNewAccount();
    if (bank != null) {
      accountEditionChecker
        .selectBank(bank);
    }
    accountEditionChecker
      .setAccountNumber("1111")
      .setAccountName("card 1111")
      .setDeferredAccount();

    if (position != null) {
      importDialog.setPosition(position);
    }
    importDialog.doImport();
    importDialog.completeLastStep();
  }

  public void importFirstQifFileWithDeferred(String fileName, String accountName) {
    ImportDialogChecker importDialog = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();

    importDialog.addNewAccount()
      .setAccountNumber("1111")
      .setAccountName(accountName);
    importDialog
      .setDeferredAccount()
      .doImport();
    importDialog.completeLastStep();
  }

  public void importOfxFile(String name, String bank) {
    ImportDialogChecker importDialogChecker = openImportDialog()
      .selectFiles(name)
      .acceptFile();
    while  (!importDialogChecker.isLastStep()){
      importDialogChecker.selectBank(bank)
        .setMainAccount()
        .doImport();
    }
    importDialogChecker.complete();
  }

  public void importOfxFile(String name, String bank, Double amount) {
    importFile(new String[]{name}, bank, amount, null);
  }

  public void importOfxFile(String name, Double amount) {
    importFile(new String[]{name}, null, amount, null);
  }

  public void importOfxOnAccount(String fileName, String existingAccount) {
    ImportDialogChecker importDialog = openImportDialog()
      .setFilePath(fileName)
      .acceptFile();
    importDialog.selectAccount(existingAccount);

    importDialog.completeImport();
  }

  public void importQifFile(String file, String bank) {
    openImportDialog()
      .setFilePath(file)
      .acceptFile()
      .selectAccount("Main account")
      .completeImport();
  }

  public void importQifFile(String file, String bank, Double amount) {
    openImportDialog()
      .setFilePath(file)
      .acceptFile()
      .createNewAccount(bank, "Main account", "", amount)
      .setMainAccount()
      .completeImport();
  }

  public void importFile(String file, String targetAccount) {
    openImportDialog().selectFiles(file)
      .acceptFile()
      .selectAccount(targetAccount)
      .completeImport();
  }

  public void importQifFiles(String bank, String file) {
//    String txt = "";
//    for (String name : files) {
//      txt += name + ";";
//    }
    ImportDialogChecker importDialogChecker = openImportDialog()
      .setFilePath(file)
      .acceptFile();

    while (!importDialogChecker.isLastStep()){
      importDialogChecker
        .setAccountName("Main account")
        .setPosition(0)
        .selectBank(bank)
        .setMainAccount()
        .doImport();
    }

    importDialogChecker.complete();
  }

  private void importFile(final String[] fileNames, final String bank, final Double amount, final String targetAccount) {
    fail();
    Window dialog = WindowInterceptor.getModalDialog(getImportMenu().triggerClick());
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
    else if (bank != null && asSelectBank(dialog)) { // OFX
      importDialog.selectBank("Autre");
    }
    if (targetAccount != null) {
      dialog.getComboBox("accountCombo").select(targetAccount);
    }
    if (importDialog.hasAccountType()) {
      importDialog.setMainAccountForAll();
    }

    final Button step2Button = dialog.getButton(Lang.get("import.preview.ok"));
    for (int i = 0; i < fileNames.length - 2; i++) {
      step2Button.click();
    }
    if (amount != null) {
      Window window = WindowInterceptor.getModalDialog(step2Button.triggerClick());
      AccountPositionEditionChecker accountPosition = new AccountPositionEditionChecker(window);
      accountPosition.setAmount(amount);
      accountPosition.validate();
    }
    int i = 0;
    while (!importDialog.isLastStep() && i != 10) {
      step2Button.click();
      if (importDialog.hasAccountType()) {
        importDialog.setMainAccountForAll();
      }
      if (bank != null && asSelectBank(dialog)) {
        importDialog
          .selectBank(bank);
      }
      i++;
    }
    importDialog.checkLastStep();
    importDialog.completeLastStep();
  }

  private boolean asSelectBank(Window dialog) {
    return dialog.findSwingComponent(JButton.class, Lang.get("import.account.bankentity")) != null;
  }

  public void exportOfxFile(String name) {
    WindowInterceptor
      .init(getExportMenu().triggerClick())
      .processWithButtonClick(Lang.get("ok"))
      .process(FileChooserHandler.init().select(name))
      .run();
  }

  public Trigger getBackupTrigger() {
    MenuItem fileMenu = window.getMenuBar().getMenu(Lang.get("file"));
    return fileMenu.getSubMenu(Lang.get("backup")).triggerClick();
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
//          dialog.checkMessageContains(Lang.get("backup.ok.message"));
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
    return getFileMenu().getSubMenu(Lang.get("restore")).triggerClick();
  }

  public Trigger getRestoreSnapshotTrigger() {
    return getFileMenu().getSubMenu("previous version").triggerClick();
  }

  public Trigger getImportTrigger() {
    return getImportMenu().triggerClick();
  }

  public Trigger getExportTrigger() {
    return getExportMenu().triggerClick();
  }

  public PreferencesChecker openPreferences() {
    return new PreferencesChecker(WindowInterceptor.getModalDialog(getPreferencesMenu().triggerClick()));
  }

  public void undo(int count) {
    for (int i = 0; i < count; i++) {
      undo();
    }
  }

  public void undo() {
    getUndoMenu().click();
  }

  public boolean isUndoAvailable() {
    return getUndoMenu().getAwtComponent().isEnabled();
  }

  public void checkUndoAvailable() {
    UISpecAssert.assertTrue(getUndoMenu().isEnabled());
  }

  public void checkUndoNotAvailable() {
    assertFalse(getUndoMenu().isEnabled());
  }

  public void redo() {
    getRedoMenu().click();
  }

  public void checkRedoAvailable() {
    UISpecAssert.assertTrue(getRedoMenu().isEnabled());
  }

  public void checkRedoNotAvailable() {
    assertFalse(getRedoMenu().isEnabled());
  }

  public void logout() {
    window.getMenuBar().getMenu(Lang.get("file")).getSubMenu(Lang.get("gotoLogin")).click();
  }

  public void exit() {
    requestExit();
    assertFalse(window.isVisible());
  }

  public void requestExit() {
    getFileMenu().getSubMenu(Lang.get("exit")).click();
  }

  public void checkExitWithoutDialog() {
    exit();
  }

  public UserEvaluationDialogChecker exitWithUserEvaluation() {
    return UserEvaluationDialogChecker.open(getFileMenu().getSubMenu(Lang.get("exit")).triggerClick());
  }

  public void deleteUser(String password) {
    MenuItem subMenu = window.getMenuBar().getMenu(Lang.get("file")).getSubMenu(Lang.get("delete"));
    PasswordDialogChecker dialogChecker =
      new PasswordDialogChecker(WindowInterceptor.getModalDialog(subMenu.triggerClick()));
    if (password != null) {
      dialogChecker.setPassword(password);
    }
    dialogChecker.validate();
    UISpecAssert.waitUntil(window.containsSwingComponent(JPasswordField.class, "password"), 2000);
  }

  public void deleteAutoLoginUser() {
    MenuItem subMenu = window.getMenuBar().getMenu(Lang.get("file"))
      .getSubMenu(Lang.get("delete"));
    ConfirmationDialogChecker confirmationDialogChecker =
      new ConfirmationDialogChecker(WindowInterceptor.getModalDialog(subMenu.triggerClick()));
    confirmationDialogChecker.checkMessageContains("Do you really want to delete all data associated to this user?");
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
    MenuItem feedbackMenu = getDevMenu().getSubMenu("[OpenFeedback]");
    return FeedbackDialogChecker.init(feedbackMenu.triggerClick());
  }

  private MenuItem getHelpMenu() {
    return window.getMenuBar().getMenu(Lang.get("help"));
  }

  private MenuItem getDevMenu() {
    return window.getMenuBar().getMenu("[Dev]");
  }

  public void checkGotoSupport(String url) {
    BrowsingChecker.checkDisplay(getHelpMenu().getSubMenu(Lang.get("gotoSupport")), url);
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
    return MessageAndDetailsDialogChecker.init(getThrowExceptionMenu().triggerClick());
  }

  public MessageAndDetailsDialogChecker throwExceptionInRepository() {
    return MessageAndDetailsDialogChecker.init(getThrowExceptionInRepositoryMenu().triggerClick());
  }

  public void protect(String userName, String password) {
    MenuItem protectMenu = getFileMenu().getSubMenu("Change identifier");
    RenameChecker checker =
      new RenameChecker(WindowInterceptor.getModalDialog(protectMenu.triggerClick()));
    checker.set("password", userName, password);
  }

  public void protectFromAnonymous(String userName, String password) {
    MenuItem protectMenu = getFileMenu().getSubMenu("protect");
    RenameChecker checker =
      new RenameChecker(WindowInterceptor.getModalDialog(protectMenu.triggerClick()));
    checker.set(userName, password);
  }

  public void checkProtect() {
    UISpecAssert.assertThat(getFileMenu().contain("protect"));
  }

  public void checkChangeUserName() {
    UISpecAssert.assertThat(getFileMenu().contain("Change identifiers..."));
  }

  public void checkDataIsOk() {
    MessageDialogChecker.init(getCheckMenu().triggerClick())
      .checkMessageContains("No error was found").close();
  }

  public void nextMonth() {
    getDevMenu().getSubMenu("goto to 10 of next month").click();
  }

  public void nextSixDays() {
    getDevMenu().getSubMenu("Add 6 days").click();
  }

  public void hideSignposts() {
    getDevMenu().getSubMenu("[Hide signposts]").click();
  }

  public void dumpData() {
    getDevMenu().getSubMenu("[Dump data]").click();
  }

  private MenuItem getThrowExceptionMenu() {
    return getDevMenu().getSubMenu("[Throw exception]");
  }

  private MenuItem getThrowExceptionInRepositoryMenu() {
    return getDevMenu().getSubMenu("Throw exception in repository");
  }

  private MenuItem getCheckMenu() {
    return getDevMenu().getSubMenu("[Check data (see logs)]");
  }

  public RestoreSnapshotChecker restoreSnapshot() {
    return new RestoreSnapshotChecker(WindowInterceptor.getModalDialog(getRestoreSnapshotTrigger()));
  }

  public void selectCurrentMonth() {
    getViewMenu().getSubMenu("select current month").click();
  }

  public void selectCurrentYear() {
    getViewMenu().getSubMenu("select current year").click();
  }

  public void selectLast12Months() {
    getViewMenu().getSubMenu("select last 12 months").click();
  }

  public AccountEditionChecker createAccount() {
    return AccountEditionChecker.open(getEditMenu().getSubMenu(Lang.get("account.create.menu")).triggerClick());
  }

  private MenuItem getFileMenu() {
    return window.getMenuBar().getMenu(Lang.get("file"));
  }

  private MenuItem getEditMenu() {
    return window.getMenuBar().getMenu(Lang.get("edit"));
  }

  private MenuItem getViewMenu() {
    return window.getMenuBar().getMenu(Lang.get("view"));
  }

  private MenuItem getImportMenu() {
    return getFileMenu().getSubMenu(Lang.get("import"));
  }

  private MenuItem getExportMenu() {
    return getFileMenu().getSubMenu(Lang.get("export"));
  }

  private MenuItem getPreferencesMenu() {
    return getFileMenu().getSubMenu(Lang.get("preferences"));
  }

  private MenuItem getUndoMenu() {
    return getEditMenu().getSubMenu(Lang.get("undo"));
  }

  private MenuItem getRedoMenu() {
    return getEditMenu().getSubMenu(Lang.get("redo"));
  }

  public NotesDialogChecker openNotes() {
    return NotesDialogChecker.open(getViewMenu().getSubMenu(Lang.get("notesDialog.action")));
  }

  public SendImportedFileChecker openSendImportedFile() {
    return SendImportedFileChecker.open(getSendImportedFileMenuItem());
  }

  private MenuItem getSendImportedFileMenuItem() {
    return getHelpMenu().getSubMenu(Lang.get("sendImportedFile.action"));
  }

  public void checkSendImportedFileDisabled() {
    assertFalse(getSendImportedFileMenuItem().isEnabled());
  }

  public void checkGotoWebsite(String url) {
    BrowsingChecker.checkDisplay(getHelpMenu().getSubMenu(Lang.get("gotoWebsite")).triggerClick(), url);
  }
}
