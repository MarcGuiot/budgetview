package org.designup.picsou.functests.checkers;

import junit.framework.TestCase;
import org.designup.picsou.functests.checkers.mobile.CreateMobileAccountChecker;
import org.designup.picsou.functests.checkers.mobile.EditMobileAccountChecker;
import org.designup.picsou.functests.checkers.printing.PrintDialogChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.addons.dev.ToggleAllAddOnsAction;
import org.designup.picsou.gui.utils.DataCheckerAction;
import org.designup.picsou.gui.utils.DumpDataAction;
import org.designup.picsou.gui.utils.dev.*;
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
    while (!importDialogChecker.isLastStep() && count < 10) {
      if (importDialogChecker.isNew()) {
        importDialogChecker.setMainAccount();
      }
      count++;
      if (!importDialogChecker.isLastStep()) {
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

  public void importOfxWithDeferred(String fileName, String cardAccountName, String targetAccount) {
    openImportDialog()
      .importDeferred(cardAccountName, fileName, true, targetAccount);
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

  public void importQifFileWithDeferred(String fileName, String bank, Double position, String targetAccount) {
    importQifFileWithDeferred(fileName, bank, position, 25, 28, 0, targetAccount);
  }

  public void importQifFileWithDeferred(String fileName, String bank, Double position,
                                        final int deferredDayPeriod, final int deferredDayPrelevement, final int deferredMonthShift,
                                        String targetAccount) {
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
      .setDeferredAccount(deferredDayPeriod, deferredDayPrelevement, deferredMonthShift, targetAccount);

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
      .setDeferredAccount(25, 28, 0)
      .doImport();
    importDialog.completeLastStep();
  }

  public void importOfxFile(String name, String bank) {
    ImportDialogChecker importDialogChecker = openImportDialog()
      .selectFiles(name)
      .acceptFile();
    while (!importDialogChecker.isLastStep()) {
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

  public void importQifFile(String file) {
    openImportDialog()
      .setFilePath(file)
      .acceptFile()
      .selectAccount("Main account")
      .completeImport();
  }

  public void importQifFileAndSkipSeries(String file) {
    openImportDialog()
      .setFilePath(file)
      .acceptFile()
      .selectAccount("Main account")
      .completeImportAndSkipSeries();
  }

  public void importQifFile(String file, String bank, Double position) {
    openImportDialog()
      .setFilePath(file)
      .acceptFile()
      .createNewAccount(bank, "Main account", "", position)
      .setMainAccount()
      .completeImport();
  }

  public void importQifFileAndSkipSeries(String file, String bank, Double position) {
    openImportDialog()
      .setFilePath(file)
      .acceptFile()
      .createNewAccount(bank, "Main account", "", position)
      .setMainAccount()
      .completeImportAndSkipSeries();
  }

  public void importFile(String file, String targetAccount) {
    openImportDialog().setFilePath(file)
      .acceptFile()
      .selectAccount(targetAccount)
      .completeImport();
  }

  public void importFile(String file, String targetAccount, Double position) {
    openImportDialog().setFilePath(file)
      .acceptFile()
      .selectAccount(targetAccount)
      .completeImport(position);
  }

  public void importQifFiles(String bank, String file) {
    ImportDialogChecker importDialogChecker = openImportDialog()
      .setFilePath(file)
      .acceptFile();

    while (!importDialogChecker.isLastStep()) {
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
      importDialog.selectBank("Other");
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
      .init(getExportMenu())
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

  public String backup(String filePath) {
    final Ref<String> selectedFile = new Ref<String>();
    WindowInterceptor interceptor = WindowInterceptor
      .init(getBackupTrigger());
    interceptor
      .process(FileChooserHandler.init().select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
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

  public void restoreWithPassword(String filePath, final String password) {
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

  public ExportDialogChecker openExport() {
    return ExportDialogChecker.init(getExportMenu().triggerClick());
  }

  public PreferencesChecker openPreferences() {
    return new PreferencesChecker(WindowInterceptor.getModalDialog(getPreferencesMenu().triggerClick()));
  }

  public DevOptionsChecker openDevOptions() {
    return DevOptionsChecker.open(getDevMenu().getSubMenu("Show Dev Options Dialog").triggerClick());
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
    System.setProperty("realExit", "false");
    getFileMenu().getSubMenu(Lang.get("exit")).click();
  }

  public void checkExitWithoutDialog() {
    exit();
  }

  public UserEvaluationDialogChecker exitWithUserEvaluation() {
    System.setProperty("realExit", "false");
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

  public void checkFeedbackLink() {
    FeedbackDialogChecker.init(getHelpMenu().getSubMenu(Lang.get("feedback")).triggerClick())
      .checkComponents()
      .cancel();
  }

  public AboutChecker openAbout() {
    return AboutChecker.open(getHelpMenu().getSubMenu("About").triggerClick());
  }

  public FeedbackDialogChecker openFeedback() {
    MenuItem feedbackMenu = getHelpMenu().getSubMenu(Lang.get("feedback"));
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
      args.add("-D" + PicsouApplication.APPNAME + PicsouApplication.TODAY + "=" + Dates.toString(currentDate));
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
    args.add(Lang.getLang());
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

  public SetPasswordChecker changeAccountIdentifiers() {
    MenuItem protectMenu = getFileMenu().getSubMenu("Change user name and password");
    return new SetPasswordChecker(WindowInterceptor.getModalDialog(protectMenu.triggerClick()));
  }

  public SetPasswordChecker setPasswordForAnonymous() {
    MenuItem protectMenu = getFileMenu().getSubMenu("Protect account with a password");
    return new SetPasswordChecker(WindowInterceptor.getModalDialog(protectMenu.triggerClick()));
  }

  public void checkSetPasswordIsProtect() {
    UISpecAssert.assertThat(getFileMenu().contain("Protect account with a password"));
  }

  public void checkSetPasswordIsChange() {
    UISpecAssert.assertThat(getFileMenu().contain("Change user name and password"));
  }

  public void checkDataIsOk() {
    MessageDialogChecker.open(getDevMenu().getSubMenu(DataCheckerAction.LABEL).triggerClick())
      .checkSuccessMessageContains("No error was found").close();
  }

  public void nextMonth() {
    getDevMenu().getSubMenu(Goto10OfNextMonthAction.LABEL).click();
  }

  public void nextSixDays() {
    getDevMenu().getSubMenu(AddSixDaysAction.LABEL).click();
  }

  public void hideSignposts() {
    getDevMenu().getSubMenu(HideSignpostsAction.LABEL).click();
  }

  public void dumpRepository() {
    getDevMenu().getSubMenu(DumpRepositoryAction.LABEL).click();
  }

  public void dumpData() {
    getDevMenu().getSubMenu(DumpDataAction.LABEL).click();
  }

  private MenuItem getThrowExceptionMenu() {
    return getDevMenu().getSubMenu(ThrowExceptionAction.LABEL);
  }

  private MenuItem getThrowExceptionInRepositoryMenu() {
    return getDevMenu().getSubMenu("Throw exception in repository");
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

  public void selectAllMonthsSinceJanuary() {
    getViewMenu().getSubMenu("Select all months since january").click();
  }

  public AccountEditionChecker createAccount() {
    return AccountEditionChecker.open(getEditMenu().getSubMenu(Lang.get("account.create.menu")).triggerClick());
  }

  public void createTransactions() {
    getEditMenu().getSubMenu("Enter transactions").click();
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

  public PrintDialogChecker openPrint() {
    return PrintDialogChecker.init(getFileMenu().getSubMenu("Print...").triggerClick());
  }

  public MessageAndDetailsDialogChecker openSendLogs() {
    return MessageAndDetailsDialogChecker.init(getHelpMenu().getSubMenu(Lang.get("sendLogs.action")).triggerClick());
  }

  public void changeDate() {
    getDevMenu().getSubMenu(ChangeDateAction.LABEL).click();
  }

  public void checkMobileAccessEnabled() {
    assertThat(getMobileCreationMenu().isEnabled());
  }

  public void checkMobileAccessDisabled() {
    assertFalse(getMobileCreationMenu().isEnabled());
  }

  public CreateMobileAccountChecker openCreateMobileUser() {
    return CreateMobileAccountChecker.open(getMobileCreationMenu().triggerClick());
  }

  public MenuItem getMobileCreationMenu() {
    return getFileMenu().getSubMenu(Lang.get("mobile.user.create.action.name"));
  }

  public EditMobileAccountChecker deleteMobileAccountUser() {
    return EditMobileAccountChecker.open(getMobileCreationMenu().triggerClick());
  }

  public MessageDialogChecker sendDataToServer() {
    return MessageDialogChecker.open(getFileMenu().getSubMenu(Lang.get("mobile.menu.send.data")).triggerClick());
  }

  public void addSixDays() {
    getDevMenu().getSubMenu(AddSixDaysAction.LABEL).click();
  }

  public void goto10OfNextMonth() {
    getDevMenu().getSubMenu(Goto10OfNextMonthAction.LABEL).click();
  }

  public void gotoPastTrialExpiration() {
    getDevMenu().getSubMenu(GotoPastTrialExpirationAction.LABEL).click();
  }

  public void enableAllAddOns() {
    getDevMenu().getSubMenu(ToggleAllAddOnsAction.ENABLE_ALL).click();
  }
}
