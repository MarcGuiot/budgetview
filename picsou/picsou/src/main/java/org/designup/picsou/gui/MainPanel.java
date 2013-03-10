package org.designup.picsou.gui;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.about.AboutAction;
import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.accounts.CreateAccountAction;
import org.designup.picsou.gui.actions.*;
import org.designup.picsou.gui.backup.BackupAction;
import org.designup.picsou.gui.backup.RestoreFileAction;
import org.designup.picsou.gui.backup.RestoreSnapshotMenuAction;
import org.designup.picsou.gui.budget.BudgetToggle;
import org.designup.picsou.gui.budget.BudgetView;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.dialogs.SendImportedFileAction;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.feedback.FeedbackService;
import org.designup.picsou.gui.feedback.FeedbackView;
import org.designup.picsou.gui.feedback.actions.OpenFeedbackDialogAction;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.help.actions.GotoSupportAction;
import org.designup.picsou.gui.help.actions.GotoWebsiteAction;
import org.designup.picsou.gui.help.actions.SendLogsAction;
import org.designup.picsou.gui.license.LicenseExpirationAction;
import org.designup.picsou.gui.license.LicenseInfoView;
import org.designup.picsou.gui.license.RegisterLicenseAction;
import org.designup.picsou.gui.mobile.CreateMobileAccountAction;
import org.designup.picsou.gui.mobile.DumpMobileXmlAction;
import org.designup.picsou.gui.mobile.SendMobileDataAction;
import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.notes.ShowNotesAction;
import org.designup.picsou.gui.notifications.NotificationsFlagView;
import org.designup.picsou.gui.preferences.PreferencesAction;
import org.designup.picsou.gui.preferences.dev.DevOptionsAction;
import org.designup.picsou.gui.printing.actions.PrintAction;
import org.designup.picsou.gui.savings.SavingsView;
import org.designup.picsou.gui.series.PeriodBudgetAreaTrigger;
import org.designup.picsou.gui.series.PeriodSeriesStatUpdater;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.series.analysis.SeriesAnalysisView;
import org.designup.picsou.gui.signpost.SignpostView;
import org.designup.picsou.gui.startup.components.DemoMessageView;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.summary.SummaryView;
import org.designup.picsou.gui.summary.version.NewVersionView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.time.actions.SelectCurrentMonthAction;
import org.designup.picsou.gui.time.actions.SelectCurrentYearAction;
import org.designup.picsou.gui.time.actions.SelectLast12MonthsAction;
import org.designup.picsou.gui.time.actions.SelectSinceLastJanuaryAction;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.transactions.creation.CreateTransactionMenuAction;
import org.designup.picsou.gui.transactions.reconciliation.annotations.ShowReconciliationAction;
import org.designup.picsou.gui.undo.RedoAction;
import org.designup.picsou.gui.undo.UndoAction;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.utils.DataCheckerAction;
import org.designup.picsou.gui.utils.DumpDataAction;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.dev.*;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.ReplicationGlobRepository;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static org.globsframework.model.utils.GlobMatchers.isFalse;

public class MainPanel {
  private PicsouFrame parent;
  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private OpenFeedbackDialogAction openFeedbackAction;
  private SetPasswordAction setPasswordAction;
  private BackupAction backupAction;
  private RestoreFileAction restoreActionFileAction;
  private RestoreSnapshotMenuAction restoreSnapshotMenuAction;
  private SendImportedFileAction sendImportedFileAction;
  private PreferencesAction preferencesAction;
  private ExitAction exitAction;
  private static ExitAction exitActionWhitoutUserEvaluation;
  private LogoutAction logoutAction;
  private DeleteUserAction deleteUserAction;
  private GlobsPanelBuilder builder;
  private GlobRepository repository;
  private Directory directory;
  private WindowManager windowManager;
  private RegisterLicenseAction registerAction;
  private JMenuBar menuBar;
  private JPanel panel;
  private TimeView timeView;
  private CardView cardView;
  private TransactionView transactionView;
  private SeriesAnalysisView seriesAnalysisView;
  private CategorizationView categorizationView;
  private SignpostView signpostView;
  private Action threadsAction;
  private CreateMobileAccountAction createMobileAccountAction;

  public static MainPanel init(GlobRepository repository, Directory directory, WindowManager mainWindow) {
    MainPanel panel = new MainPanel(repository, directory, mainWindow);
    mainWindow.getFrame().setRepository(repository);
    return panel;
  }

  private MainPanel(final GlobRepository repository, final Directory directory, WindowManager windowManager) {
    this.repository = repository;
    this.directory = directory;
    this.windowManager = windowManager;
    this.parent = windowManager.getFrame();
    directory.add(JFrame.class, parent);
    directory.add(new UndoRedoService(repository));
    directory.add(new HelpService(repository, directory));
    directory.add(new FeedbackService(repository, directory));
    LogoutService logoutService = new MainPanelLogoutService();
    directory.add(LogoutService.class, logoutService);

    directory.addFactory(SeriesEditor.class, new Directory.Factory<SeriesEditor>() {
      public SeriesEditor create() {
        Directory directorySelection = new DefaultDirectory(directory);
        directorySelection.add(SelectionService.class, new SelectionService());
        return new SeriesEditor(repository, directory);
      }
    });

    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/picsou.splits", repository, directory);

    TitleView titleView = new TitleView(repository, directory);

    transactionView = new TransactionView(repository, directory);
    categorizationView = new CategorizationView(repository, directory);
    timeView = new TimeView(repository, directory);

    BudgetToggle budgetToggle = new BudgetToggle(repository);
    budgetToggle.registerComponents(builder);

    directory.add(new NavigationService(transactionView, categorizationView, budgetToggle, repository, directory));

    importFileAction = ImportFileAction.initForMenu(Lang.get("import"), repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    openFeedbackAction = new OpenFeedbackDialogAction(Lang.get("feedback"), repository, directory);
    backupAction = new BackupAction(repository, directory);
    restoreActionFileAction = new RestoreFileAction(repository, directory);
    restoreSnapshotMenuAction = new RestoreSnapshotMenuAction(directory, repository);
    sendImportedFileAction = new SendImportedFileAction(directory, repository);
    preferencesAction = new PreferencesAction(repository, directory);
    registerAction = new RegisterLicenseAction(repository, directory);
    exitAction = new ExitAction(windowManager, repository, directory);
    logoutAction = new LogoutAction(logoutService);
    setPasswordAction = new SetPasswordAction(repository, directory);
    deleteUserAction = new DeleteUserAction(this, repository, directory);
    createMobileAccountAction = new CreateMobileAccountAction(directory, repository);
    threadsAction = new SendStackTracesAction(repository, directory);

    LicenseInfoView licenseInfoView = new LicenseInfoView(repository, directory);

    ReplicationGlobRepository replicationGlobRepository =
      new ReplicationGlobRepository(repository, PeriodSeriesStat.TYPE, PeriodBudgetAreaStat.TYPE);

    PeriodSeriesStatUpdater.init(replicationGlobRepository, directory);
    PeriodBudgetAreaTrigger.init(replicationGlobRepository);

    cardView = new CardView(repository, directory, categorizationView.getGotoBudgetSignpost());
    seriesAnalysisView = new SeriesAnalysisView(repository, directory);
    signpostView = new SignpostView(replicationGlobRepository, directory);
    createPanel(
      titleView,
      transactionView,
      timeView,
      new NewVersionView(repository, directory),
      new DemoMessageView(repository, directory),
      new AccountView(repository, directory),
      categorizationView,
      cardView,
      new BudgetView(replicationGlobRepository, directory),
      seriesAnalysisView,
      new SavingsView(replicationGlobRepository, directory),
      new SummaryView(repository, directory),
      new FeedbackView(repository, directory),
      signpostView,
      licenseInfoView,
      new NotificationsFlagView(repository, directory));

    createMenuBar(parent, replicationGlobRepository, directory);

    builder.load();
  }

  private void selectLastMonthWithATransaction(GlobRepository repository, Directory directory) {
    GlobList transactions = repository.getAll(Transaction.TYPE, isFalse(Transaction.PLANNED));
    if (transactions.isEmpty()) {
      return;
    }
    Integer lastMonth = transactions.getSortedSet(Transaction.MONTH).last();
    directory.get(SelectionService.class).select(repository.get(Key.create(Month.TYPE, lastMonth)));
  }

  private void createPanel(View... views) {
    for (View view : views) {
      view.registerComponents(builder);
    }
    builder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        panel = (JPanel)component;
      }
    });
  }

  public void show() {
    ImportFileAction.registerToOpenRequestManager(Lang.get("import"), repository, directory);

    parent.setJMenuBar(menuBar);
    cardView.showInitialCard();
    transactionView.reset();
    categorizationView.reset();
    directory.get(NavigationService.class).reset();
    directory.get(UndoRedoService.class).reset();
    directory.get(HelpService.class).reset();
    signpostView.reset();

    windowManager.setPanel(panel);
    seriesAnalysisView.reset();
    timeView.selectCurrentMonth();
    selectLastMonthWithATransaction(repository, directory);
    timeView.centerToSelected();

    SplitsEditor.show(builder, parent);

    showInitialMessageIfNeeded();
  }

  private void showInitialMessageIfNeeded() {
    if (!repository.contains(Transaction.TYPE)) {
      SignpostStatus.init(repository);
    }
  }

  public void createMenuBar(final PicsouFrame frame, ReplicationGlobRepository replicationGlobRepository, Directory directory) {
    menuBar = new JMenuBar();

    menuBar.add(createFileMenu(replicationGlobRepository));
    menuBar.add(createEditMenu(frame, directory));
    menuBar.add(createViewMenu(directory));

    Utils.beginRemove();
    menuBar.add(createDevMenu(directory));
    Utils.endRemove();

    menuBar.add(createHelpMenu(directory));
  }

  private JMenu createFileMenu(ReplicationGlobRepository replicationGlobRepository) {
    JMenu menu = new JMenu(Lang.get("menuBar.file"));
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
    menu.add(new PrintAction(replicationGlobRepository, directory));

    menu.addSeparator();
    menu.add(createMobileAccountAction);
    menu.add(new SendMobileDataAction(repository, directory));


    if (Gui.useMacOSMenu()) {
      if (exitActionWhitoutUserEvaluation != null) {
        MRJAdapter.removeQuitApplicationListener(exitActionWhitoutUserEvaluation);
        exitActionWhitoutUserEvaluation = null;
      }
      MRJAdapter.addQuitApplicationListener(exitAction);
    }
    else {
      menu.addSeparator();
      menu.add(exitAction);
    }
    return menu;
  }

  private JMenu createEditMenu(PicsouFrame frame, Directory directory) {
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
    editMenu.add(new CreateTransactionMenuAction(directory));

    return editMenu;
  }

  private JMenu createDevMenu(final Directory directory) {
    JMenu devMenu = new JMenu("[Dev]");
    devMenu.add(new DevOptionsAction(repository, directory));
    devMenu.add(new DumpDataAction(repository));
    devMenu.add(new DataCheckerAction(repository, directory));
    devMenu.add(new ThrowExceptionAction());
    devMenu.add(new ThrowInRepoExceptionAction(repository));
    devMenu.add(new GotoNextMonthAction(repository));
    devMenu.add(new AddSixDayAction(repository));
    devMenu.add(new ClearAllSignpostsAction(repository));
    devMenu.add(new ShowUserEvaluationDialogAction(repository, directory));
    devMenu.add(new LicenseExpirationAction(repository, directory));
    devMenu.add(new DumpRepositoryAction(repository));
    devMenu.add(new ChangeDateAction(repository));
    devMenu.add(new AddAccountErrorAction(repository));
    devMenu.add(new DumpMobileXmlAction(repository, directory));
    devMenu.add(threadsAction);
    return devMenu;
  }

  private JMenu createViewMenu(Directory directory) {
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

  private JMenu createHelpMenu(final Directory directory) {
    JMenu menu = new JMenu(Lang.get("menuBar.help"));
    menu.add(new AbstractAction(Lang.get("help.index")) {
      public void actionPerformed(ActionEvent e) {
        directory.get(HelpService.class).show("index", parent);
      }
    });
    menu.add(cardView.getHelpAction());

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

  public void logout() {
    prepareForLogout();
    windowManager.logout();
  }

  public void gotoDemoAccount() {
    prepareForLogout();
    windowManager.logOutAndOpenDemo();
  }

  public void gotoAutoLogin() {
    prepareForLogout();
    windowManager.logOutAndAutoLogin();
  }

  public void deleteUser(String userName, char[] chars) {
    prepareForLogout();
    windowManager.logOutAndDeleteUser(userName, chars);
  }

  private void prepareForLogout() {
    if (Gui.useMacOSMenu()) {
      MRJAdapter.removeQuitApplicationListener(exitAction);
      if (exitActionWhitoutUserEvaluation == null) {
        exitActionWhitoutUserEvaluation = new ExitAction(windowManager, repository, directory, false);
        MRJAdapter.addQuitApplicationListener(exitActionWhitoutUserEvaluation);
      }
    }
    directory.get(OpenRequestManager.class).popCallback();
  }

  public void end() {
    SendMobileDataAction.sendToMobile(repository, directory.get(ConfigService.class));
  }

  private class MainPanelLogoutService implements LogoutService {
    public void logout() {
      MainPanel.this.logout();
    }

    public void gotoDemoAccount() {
      MainPanel.this.gotoDemoAccount();
    }

    public void gotoAutologin() {
      MainPanel.this.gotoAutoLogin();
    }
  }
}
