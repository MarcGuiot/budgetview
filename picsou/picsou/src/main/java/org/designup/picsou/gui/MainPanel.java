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
import org.designup.picsou.gui.feedback.actions.SendFeedbackAction;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.help.actions.GotoSupportAction;
import org.designup.picsou.gui.license.LicenseExpirationAction;
import org.designup.picsou.gui.license.LicenseInfoView;
import org.designup.picsou.gui.license.RegisterLicenseAction;
import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.notes.ShowNotesAction;
import org.designup.picsou.gui.preferences.PreferencesAction;
import org.designup.picsou.gui.savings.SavingsView;
import org.designup.picsou.gui.series.PeriodBudgetAreaTrigger;
import org.designup.picsou.gui.series.PeriodSeriesStatUpdater;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.series.analysis.SeriesAnalysisView;
import org.designup.picsou.gui.signpost.SignpostView;
import org.designup.picsou.gui.startup.LogoutService;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.summary.SummaryView;
import org.designup.picsou.gui.summary.version.NewVersionView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.time.actions.SelectCurrentMonthAction;
import org.designup.picsou.gui.time.actions.SelectCurrentYearAction;
import org.designup.picsou.gui.time.actions.SelectLast12MonthsAction;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.transactions.reconciliation.ShowReconciliationAction;
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
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import static org.globsframework.model.utils.GlobMatchers.isFalse;
import org.globsframework.model.utils.ReplicationGlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.Map;

public class MainPanel {
  private PicsouFrame parent;
  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private SendFeedbackAction sendFeedbackAction;
  private ProtectAction protectAction;
  private BackupAction backupAction;
  private RestoreFileAction restoreActionFileAction;
  private RestoreSnapshotMenuAction restoreSnapshotMenuAction;
  private SendImportedFileAction sendImportedFileAction;
  private PreferencesAction preferencesAction;
  private ExitAction exitAction;
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
    LogoutService logoutService = new LogoutService() {
      public void logout() {
        MainPanel.this.logout();
      }
    };
    directory.add(LogoutService.class, logoutService);

    directory.addFactory(SeriesEditor.class, new Directory.Factory<SeriesEditor>() {
      public SeriesEditor create() {
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
    sendFeedbackAction = new SendFeedbackAction(repository, directory);
    backupAction = new BackupAction(repository, directory);
    restoreActionFileAction = new RestoreFileAction(repository, directory);
    restoreSnapshotMenuAction = new RestoreSnapshotMenuAction(directory, repository);
    sendImportedFileAction = new SendImportedFileAction(directory, repository);
    preferencesAction = new PreferencesAction(repository, directory);
    registerAction = new RegisterLicenseAction(repository, directory);
    exitAction = new ExitAction(windowManager, repository, directory);
    logoutAction = new LogoutAction(logoutService);
    protectAction = new ProtectAction(repository, directory);
    deleteUserAction = new DeleteUserAction(this, repository, directory);
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
      new AccountView(repository, directory),
      categorizationView,
      cardView,
      new BudgetView(replicationGlobRepository, directory),
      seriesAnalysisView,
      new SavingsView(replicationGlobRepository, directory),
      new SummaryView(repository, directory),
      new FeedbackView(repository, directory),
      signpostView,
      licenseInfoView);

    createMenuBar(parent, directory);
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

  public void createMenuBar(final PicsouFrame frame, Directory directory) {
    menuBar = new JMenuBar();

    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu(frame, directory));
    menuBar.add(createViewMenu(directory));

    Utils.beginRemove();
    menuBar.add(createDevMenu(directory));
    Utils.endRemove();

    menuBar.add(createHelpMenu(directory));
  }

  private JMenu createFileMenu() {
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
    menu.add(protectAction);
    menu.add(logoutAction);
    menu.add(deleteUserAction);
    if (Gui.useMacOSMenu()) {
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

    editMenu.addSeparator();
    CreateAccountAction createAccount =
      new CreateAccountAction("account.create.menu", AccountType.MAIN, repository, directory, frame);
    createAccount.setGotoAccountViewEnabled(true);
    editMenu.add(createAccount);

    JRootPane rootPane = frame.getRootPane();
    GuiUtils.addShortcut(rootPane, "UNDO", undoAction, GuiUtils.ctrl(KeyEvent.VK_Z));
    GuiUtils.addShortcut(rootPane, "REDO", redoAction, GuiUtils.ctrl(KeyEvent.VK_Y));

    return editMenu;
  }

  private JMenu createDevMenu(final Directory directory) {
    JMenu devMenu = new JMenu("[Dev]");
    devMenu.addSeparator();
    devMenu.add(new DumpDataAction(repository));
    devMenu.add(new DataCheckerAction(repository, directory));
    devMenu.add(new ThrowExceptionAction());
    devMenu.add(new ThrowInRepoExceptionAction(repository));
    devMenu.add(new GotoNextMonthAction(repository));
    devMenu.add(new AddSixDayAction(repository));
    devMenu.add(new ClearAllSignpostsAction(repository));
    devMenu.add(new OpenFeedbackDialogAction(repository, directory));
    devMenu.add(new ShowUserEvaluationDialogAction(repository, directory));
    devMenu.add(new LicenseExpirationAction(repository, directory));
    devMenu.add(threadsAction);
    return devMenu;
  }

  private JMenu createViewMenu(Directory directory) {
    JMenu showMenu = new JMenu(Lang.get("menuBar.view"));
    showMenu.add(new SelectCurrentMonthAction(repository, directory));
    showMenu.add(new SelectCurrentYearAction(repository, directory));
    showMenu.add(new SelectLast12MonthsAction(repository, directory));
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
    menu.add(new GotoSupportAction(directory));
    menu.add(sendFeedbackAction);
    menu.add(sendImportedFileAction);
    if (!Gui.useMacOSMenu()) {
      menu.addSeparator();
      menu.add(new AboutAction(directory));
    }
    return menu;
  }

  public void logout() {
    directory.get(OpenRequestManager.class).popCallback();
    windowManager.logout();
  }

  public void deleteUser(String userName, char[] chars) {
    directory.get(OpenRequestManager.class).popCallback();
    windowManager.logOutAndDeleteUser(userName, chars);
  }

  private static class SendStackTracesAction extends AbstractAction {
    private GlobRepository repository;
    private Directory directory;
    private Thread thread;
    private boolean stop;
    private StringBuilder buffer = new StringBuilder();

    public SendStackTracesAction(GlobRepository repository, Directory directory) {
      super(Lang.get("dumpThread"));
      this.repository = repository;
      this.directory = directory;
    }

    public void actionPerformed(ActionEvent e) {
      if (thread == null) {
        stop = false;
        dump();
        this.putValue(Action.NAME, Lang.get("sendThread"));
      }
      else {
        this.putValue(Action.NAME, Lang.get("dumpThread"));
        stop();
      }
    }

    private void stop() {
      try {
        synchronized (this) {
          stop = true;
          notifyAll();
        }
        while (thread.isAlive()) {
          thread.interrupt();
          thread.join(200);
        }
        thread = null;

        Glob glob = repository.find(User.KEY);
        String mail = "???";
        if (glob != null) {
          mail = glob.get(User.EMAIL);
        }
        String content = buffer.toString();
        Log.write(content);
        directory.get(ConfigService.class).sendMail(ConfigService.MAIL_CONTACT,
                                                    mail,
                                                    "Thread dump",
                                                    content,
                                                    new ConfigService.Listener() {
                                                      public void sent(String mail, String title, String content) {
                                                        Log.write("Mail sent from " + mail + " title : " + title + "\n" + content);
                                                      }

                                                      public void sendFail(String mail, String title, String content) {
                                                        Log.write("Fail to sent mail from " + mail + " title : " + title + "\n" + content);
                                                        //                                                      MessageDialog.show("");
                                                      }
                                                    });
      }
      catch (InterruptedException e) {
        Log.write("interupted thread in send ");
      }
    }

    private void dump() {
      thread = new Thread() {
        public void run() {
          try {
            synchronized (this) {
              wait(1000);
              while (!stop) {
                buffer.append("-------------------").append(Dates.toTimestampString(new Date())).append('\n');
                Runtime runtime = Runtime.getRuntime();
                buffer
                  .append("free mem : ").append(runtime.freeMemory())
                  .append(" max mem : ").append(runtime.maxMemory())
                  .append(" total mem : ").append(runtime.totalMemory())
                  .append('\n');
                Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
                for (Map.Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
                  if (thread != entry.getKey()) {
                    buffer.append("Thread : \n")
                      .append(entry.getKey().getName())
                      .append("\n");
                    for (StackTraceElement element : entry.getValue()) {
                      buffer
                        .append("at ")
                        .append(element.getClassName())
                        .append(".")
                        .append(element.getMethodName())
                        .append("(")
                        .append(element.getFileName())
                        .append(":")
                        .append(element.getLineNumber())
                        .append(")")
                        .append("\n");
                    }
                  }
                }
                wait(4000);
              }
            }
          }
          catch (InterruptedException e) {
          }
        }
      };
      thread.setDaemon(true);
      thread.start();

    }
  }
}
