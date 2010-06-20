package org.designup.picsou.gui;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.about.AboutAction;
import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.*;
import org.designup.picsou.gui.backup.BackupAction;
import org.designup.picsou.gui.backup.RestoreAction;
import org.designup.picsou.gui.budget.BudgetView;
import org.designup.picsou.gui.card.ActionView;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.card.NavigationView;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.filtering.TextFilterPanel;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.license.LicenseInfoView;
import org.designup.picsou.gui.license.RegisterLicenseAction;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.monthsummary.VersionInfoView;
import org.designup.picsou.gui.notes.InitializationView;
import org.designup.picsou.gui.notes.NotesView;
import org.designup.picsou.gui.preferences.PreferencesAction;
import org.designup.picsou.gui.savings.SavingsView;
import org.designup.picsou.gui.series.PeriodSeriesStatUpdater;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.evolution.SeriesEvolutionView;
import org.designup.picsou.gui.series.view.SeriesView;
import org.designup.picsou.gui.startup.LogoutService;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.undo.RedoAction;
import org.designup.picsou.gui.undo.UndoAction;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.utils.*;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
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
import org.globsframework.model.Glob;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.utils.GlobMatcher;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.ReplicationGlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class MainPanel {
  private PicsouFrame parent;
  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private ProtectAction protectAction;
  private BackupAction backupAction;
  private RestoreAction restoreAction;
  private PreferencesAction preferencesAction;
  private ExitAction exitAction;
  private LogoutAction logoutAction;
  private DeleteUserAction deleteUserAction;
  private GlobsPanelBuilder builder;
  private GlobRepository repository;
  private Directory directory;
  private WindowManager windowManager;
  private RegisterLicenseAction registerAction;
  private SeriesView seriesView;
  private JMenuBar menuBar;
  private JPanel panel;
  private TimeView timeView;
  private CardView cardView;
  private TransactionView transactionView;
  private TextFilterPanel search;
  private SeriesEvolutionView seriesEvolutionView;
  private CategorizationView categorizationView;

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
    LogoutService logoutService = new LogoutService() {
      public void logout() {
        MainPanel.this.logout();
      }
    };
    directory.add(LogoutService.class, logoutService);

    directory.add(SeriesEditionDialog.class, new SeriesEditionDialog(repository, directory));

    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/picsou.splits", repository, directory);

    TitleView titleView = new TitleView(repository, directory);

    TransactionSelection transactionSelection = new TransactionSelection(repository, directory);

    transactionView = new TransactionView(repository, directory, transactionSelection);
    categorizationView = new CategorizationView(repository, directory);
    seriesView = new SeriesView(repository, directory);
    timeView = new TimeView(repository, directory);

    directory.add(new NavigationService(categorizationView, seriesView, repository, directory));

    importFileAction = ImportFileAction.initForMenu(Lang.get("import"), repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    backupAction = new BackupAction(repository, directory);
    restoreAction = new RestoreAction(repository, directory);
    preferencesAction = new PreferencesAction(repository, directory);
    registerAction = new RegisterLicenseAction(repository, directory);
    exitAction = new ExitAction(windowManager, directory);
    logoutAction = new LogoutAction(logoutService);
    protectAction = new ProtectAction(repository, directory);
    deleteUserAction = new DeleteUserAction(this, repository, directory);
    search = new TextFilterPanel(transactionView.getFilterSet(), repository, directory) {
      protected GlobMatcher createMatcher(final String searchFilter) {
        return or(fieldContainsIgnoreCase(Transaction.LABEL, searchFilter),
                  fieldContainsIgnoreCase(Transaction.NOTE, searchFilter),
                  new GlobMatcher() {
                    final GlobStringifier amountStringifier =
                      directory.get(DescriptionService.class).getStringifier(Transaction.AMOUNT);
                    public boolean matches(Glob item, GlobRepository repository) {
                      return amountStringifier.toString(item, repository).contains(searchFilter);
                    }
                  });
      }
    };
    builder.add("transactionSearch", search.getPanel());
    builder.addLabel("sum", Transaction.TYPE,
                     GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT, false, Transaction.AMOUNT))
      .setAutoHideIfEmpty(true);

    LicenseInfoView licenseInfoView = new LicenseInfoView(repository, directory);

    ReplicationGlobRepository replicationGlobRepository =
      new ReplicationGlobRepository(repository, PeriodSeriesStat.TYPE);

    PeriodSeriesStatUpdater.init(replicationGlobRepository, directory);

    cardView = new CardView(repository, directory, categorizationView.getCompletionSignpost());
    NotesView notesView = new NotesView(repository, directory);
    InitializationView initializationView = new InitializationView(importFileAction, repository, directory);
    seriesEvolutionView = new SeriesEvolutionView(repository, directory);
    createPanel(
      titleView,
      transactionView,
      timeView,
      new VersionInfoView(repository, directory),
      new AccountView(repository, directory),
      categorizationView,
      cardView,
      new ActionView(repository, directory),
      new NavigationView(repository, directory),
      new BudgetView(replicationGlobRepository, directory),
      seriesView,
      seriesEvolutionView,
      new SavingsView(replicationGlobRepository, directory),
      licenseInfoView,
      notesView);

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
    search.reset();
    transactionView.reset();
    categorizationView.reset();
    directory.get(NavigationService.class).reset();
    directory.get(UndoRedoService.class).reset();
    directory.get(HelpService.class).reset();
    windowManager.setPanel(panel);
    seriesEvolutionView.reset();
    timeView.selectCurrentMonth();
    selectLastMonthWithATransaction(repository, directory);
    timeView.centerToSelected();
    seriesView.selectAll();

    SplitsEditor.show(builder, parent);

    showInitialMessageIfNeeded();
  }

  private void showInitialMessageIfNeeded() {
    if (!repository.contains(Transaction.TYPE)) {
      SignpostStatus.setCompleted(SignpostStatus.WELCOME_SHOWN, repository);
    }
  }

  public void createMenuBar(final PicsouFrame frame, Directory directory) {
    menuBar = new JMenuBar();
    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu(frame, directory));
    menuBar.add(createHelpMenu(directory));
  }

  private JMenu createFileMenu() {
    JMenu menu = new JMenu(Lang.get("file"));
    menu.add(importFileAction);
    menu.add(exportFileAction);
    menu.addSeparator();
    menu.add(backupAction);
    menu.add(restoreAction);
    menu.add(protectAction);

    if (Gui.useMacOSMenu()) {
      MRJAdapter.setPreferencesEnabled(true);
      MRJAdapter.addPreferencesListener(preferencesAction);
    }
    else {
      menu.addSeparator();
      menu.add(preferencesAction);
    }

    menu.addSeparator();
    menu.add(registerAction);
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

    JMenu editMenu = new JMenu(Lang.get("edit"));
    editMenu.add(undoAction);
    editMenu.add(redoAction);

//    Utils.beginRemove();
    editMenu.addSeparator();
    editMenu.add(new DumpDataAction(repository));
    editMenu.add(new DataCheckerAction(repository, directory));
    editMenu.add(new ThrowExceptionAction());
    editMenu.add(new ThrowInRepoExceptionAction(repository));
//    Utils.endRemove();

    JRootPane rootPane = frame.getRootPane();
    GuiUtils.addShortcut(rootPane, "UNDO", undoAction, GuiUtils.ctrl(KeyEvent.VK_Z));
    GuiUtils.addShortcut(rootPane, "REDO", redoAction, GuiUtils.ctrl(KeyEvent.VK_Y));

    return editMenu;
  }

  private JMenu createHelpMenu(final Directory directory) {
    JMenu menu = new JMenu(Lang.get("help"));
    menu.add(new AbstractAction(Lang.get("help.index")) {
      public void actionPerformed(ActionEvent e) {
        directory.get(HelpService.class).show("index", parent);
      }
    });
    menu.add(cardView.getHelpAction());

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
}
