package org.designup.picsou.gui.utils;

import org.designup.picsou.gui.WindowManager;
import org.designup.picsou.gui.about.AboutAction;
import org.designup.picsou.gui.accounts.actions.CreateAccountAction;
import org.designup.picsou.gui.actions.*;
import org.designup.picsou.gui.addons.dev.ToggleAllAddOnsAction;
import org.designup.picsou.gui.backup.BackupAction;
import org.designup.picsou.gui.backup.RestoreFileAction;
import org.designup.picsou.gui.backup.RestoreSnapshotMenuAction;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.dialogs.SendImportedFileAction;
import org.designup.picsou.gui.feedback.actions.SendFeedbackAction;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.help.actions.GotoSupportAction;
import org.designup.picsou.gui.help.actions.GotoWebsiteAction;
import org.designup.picsou.gui.help.actions.SendLogsAction;
import org.designup.picsou.gui.license.dev.ShowLicenseExpirationAction;
import org.designup.picsou.gui.license.activation.RegisterLicenseAction;
import org.designup.picsou.gui.license.dev.SimulateLicenseRegistrationAction;
import org.designup.picsou.gui.mobile.DumpMobileXmlAction;
import org.designup.picsou.gui.mobile.EditMobileAccountAction;
import org.designup.picsou.gui.mobile.SendMobileDataAction;
import org.designup.picsou.gui.notes.ShowNotesAction;
import org.designup.picsou.gui.preferences.PreferencesAction;
import org.designup.picsou.gui.preferences.dev.DevOptionsAction;
import org.designup.picsou.gui.printing.actions.PrintBudgetAction;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.gui.time.actions.SelectCurrentMonthAction;
import org.designup.picsou.gui.time.actions.SelectCurrentYearAction;
import org.designup.picsou.gui.time.actions.SelectLast12MonthsAction;
import org.designup.picsou.gui.time.actions.SelectSinceLastJanuaryAction;
import org.designup.picsou.gui.transactions.creation.ShowCreateTransactionAction;
import org.designup.picsou.gui.transactions.reconciliation.annotations.ShowReconciliationAction;
import org.designup.picsou.gui.undo.RedoAction;
import org.designup.picsou.gui.undo.UndoAction;
import org.designup.picsou.gui.utils.dev.*;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.ReplicationGlobRepository;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class MenuBarBuilder {

  private final GlobRepository repository;
  private final Directory directory;

  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private SendFeedbackAction openFeedbackAction;
  private SetPasswordAction setPasswordAction;
  private BackupAction backupAction;
  private RestoreFileAction restoreActionFileAction;
  private RestoreSnapshotMenuAction restoreSnapshotMenuAction;
  private SendImportedFileAction sendImportedFileAction;
  private PreferencesAction preferencesAction;
  private DeleteUserAction deleteUserAction;
  private RegisterLicenseAction registerAction;
  private Action threadsAction;
  private EditMobileAccountAction editMobileAccountAction;
  private PrintBudgetAction printBudgetAction;
  private Action viewHelpAction;
  private ExitAction exitAction;
  private LogoutAction logoutAction;

  public MenuBarBuilder(GlobRepository repository, ReplicationGlobRepository replicationGlobRepository, WindowManager windowManager, LogoutService logoutService, Action viewHelpAction, Directory directory, DeleteUserAction deleteUserAction) {
    this.repository = repository;
    this.directory = directory;

    this.viewHelpAction = viewHelpAction;
    this.importFileAction = ImportFileAction.initForMenu(Lang.get("import"), repository, directory);
    this.exportFileAction = new ExportFileAction(repository, directory);
    this.openFeedbackAction = new SendFeedbackAction(Lang.get("feedback"), repository, directory);
    this.backupAction = new BackupAction(repository, directory);
    this.restoreActionFileAction = new RestoreFileAction(repository, directory);
    this.restoreSnapshotMenuAction = new RestoreSnapshotMenuAction(directory, repository);
    this.sendImportedFileAction = new SendImportedFileAction(directory, repository);
    this.preferencesAction = new PreferencesAction(repository, directory);
    this.registerAction = new RegisterLicenseAction(repository, directory);
    this.setPasswordAction = new SetPasswordAction(repository, directory);
    this.deleteUserAction = deleteUserAction;
    this.printBudgetAction = new PrintBudgetAction(replicationGlobRepository, directory);
    this.editMobileAccountAction = new EditMobileAccountAction(repository, directory);
    this.threadsAction = new SendStackTracesAction(repository, directory);
    this.exitAction = new ExitAction(windowManager, repository, directory);
    this.logoutAction = new LogoutAction(logoutService);
  }

  public void createMenuBar(final PicsouFrame frame) {
    JMenuBar menuBar = new JMenuBar();

    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu(frame));
    menuBar.add(createViewMenu());

    Utils.beginRemove();
    menuBar.add(createDevMenu());
    Utils.endRemove();

    menuBar.add(createHelpMenu(frame));

    frame.setJMenuBar(menuBar);
  }

  private JMenu createFileMenu() {

    final JMenu menu = new JMenu(Lang.get("menuBar.file"));
    menu.add(importFileAction);
    menu.add(exportFileAction);
    menu.addSeparator();
    menu.add(backupAction);
    menu.add(restoreActionFileAction);
    menu.add(restoreSnapshotMenuAction);
    // A Restaurer - ne fonctionne plus sur Mac
    // MRJAdapter.setPreferencesEnabled(true);
    // MRJAdapter.addPreferencesListener(preferencesAction);

    menu.addSeparator();
    menu.add(preferencesAction);

    menu.addSeparator();
    menu.add(registerAction);

    menu.add(setPasswordAction);
    menu.add(logoutAction);
    menu.add(deleteUserAction);

    menu.addSeparator();
    menu.add(printBudgetAction);

    menu.addSeparator();
    menu.add(editMobileAccountAction);
    menu.add(new SendMobileDataAction(repository, directory));

    if (!Gui.useMacOSMenu()) {
      menu.addSeparator();
      menu.add(exitAction);
    }
    return menu;
  }

  private JMenu createEditMenu(PicsouFrame frame) {
    final UndoAction undoAction = new UndoAction(directory);
    final RedoAction redoAction = new RedoAction(directory);

    JMenu editMenu = new JMenu(Lang.get("menuBar.edit"));
    editMenu.add(undoAction);
    editMenu.add(redoAction);

    JRootPane rootPane = frame.getRootPane();
    GuiUtils.addShortcut(rootPane, "UNDO", undoAction, GuiUtils.ctrl(KeyEvent.VK_Z));
    GuiUtils.addShortcut(rootPane, "REDO", redoAction, GuiUtils.ctrl(KeyEvent.VK_Y));
    GuiUtils.addShortcut(rootPane, "Data check", new DataCheckerAction(repository, directory),
                         GuiUtils.ctrl(KeyEvent.VK_D));

    editMenu.addSeparator();
    CreateAccountAction createAccount =
      new CreateAccountAction("account.create.menu", AccountType.MAIN, repository, directory, frame);
    createAccount.setGotoAccountViewEnabled(true);
    editMenu.add(createAccount);
    editMenu.add(new ShowCreateTransactionAction(directory));

    return editMenu;
  }

  private JMenu createDevMenu() {
    JMenu devMenu = new JMenu("[Dev]");
    devMenu.add(new DevOptionsAction(repository, directory));
    devMenu.addSeparator();
    devMenu.add(new DumpRepositoryAction(repository));
    devMenu.add(new DumpDataAction(repository));
    devMenu.add(new DataCheckerAction(repository, directory));
    devMenu.add(new ThrowExceptionAction());
    devMenu.add(new ThrowInRepoExceptionAction(repository));
    devMenu.add(new AddAccountErrorAction(repository));
    devMenu.addSeparator();
    devMenu.add(new Goto10OfNextMonthAction(repository));
    devMenu.add(new AddSixDaysAction(repository));
    devMenu.add(new ChangeDateAction(repository));
    devMenu.addSeparator();
    devMenu.add(new ClearAllSignpostsAction(repository));
    devMenu.add(new ShowUserEvaluationDialogAction(repository, directory));
    devMenu.add(new DumpUserProgressMessageAction(repository, directory));
    devMenu.addSeparator();
    devMenu.add(new SimulateLicenseRegistrationAction(repository));
    devMenu.add(new ShowLicenseExpirationAction(repository, directory));
    devMenu.addSeparator();
    devMenu.add(ToggleAllAddOnsAction.enableAll(repository));
    devMenu.add(ToggleAllAddOnsAction.disableAll(repository));
    devMenu.addSeparator();
    devMenu.add(new DumpMobileXmlAction(repository));
    devMenu.add(threadsAction);
    return devMenu;
  }

  private JMenu createViewMenu() {
    JMenu showMenu = new JMenu(Lang.get("menuBar.view"));
    showMenu.add(new SelectCurrentMonthAction(repository, directory));
    showMenu.add(new SelectCurrentYearAction(repository, directory));
    showMenu.add(new SelectLast12MonthsAction(repository, directory));
    showMenu.add(new SelectSinceLastJanuaryAction(repository, directory));
    showMenu.addSeparator();
    showMenu.add(new ShowNotesAction(repository, directory));
    showMenu.addSeparator();
    showMenu.add(new ShowReconciliationAction(repository, directory));
    return showMenu;
  }

  private JMenu createHelpMenu(final Window frame) {
    JMenu menu = new JMenu(Lang.get("menuBar.help"));
    menu.add(new AbstractAction(Lang.get("help.index")) {
      public void actionPerformed(ActionEvent e) {
        directory.get(HelpService.class).show("index", frame);
      }
    });
    menu.add(viewHelpAction);

    menu.addSeparator();
    menu.add(new GotoWebsiteAction(directory));
    menu.add(new GotoSupportAction(directory));

    menu.addSeparator();
    menu.add(openFeedbackAction);
    menu.add(new SendLogsAction(directory));
    menu.add(sendImportedFileAction);

    if (!Gui.useMacOSMenu()) {
      menu.addSeparator();
      menu.add(new AboutAction(directory));
    }

    return menu;
  }

  public ExitAction getExitAction() {
    return exitAction;
  }

  public ImportFileAction getImportFileAction() {
    return importFileAction;
  }

  public PrintBudgetAction getPrintBudgetAction() {
    return printBudgetAction;
  }
}
