package org.designup.picsou.gui;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.about.AboutAction;
import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ExportFileAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.backup.BackupAction;
import org.designup.picsou.gui.backup.RestoreAction;
import org.designup.picsou.gui.budget.BudgetView;
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
import org.designup.picsou.gui.monthsummary.VersionInfoView;
import org.designup.picsou.gui.monthsummary.MonthSummaryView;
import org.designup.picsou.gui.preferences.PreferencesAction;
import org.designup.picsou.gui.savings.SavingsView;
import org.designup.picsou.gui.series.PeriodSeriesStatUpdater;
import org.designup.picsou.gui.series.evolution.SeriesEvolutionView;
import org.designup.picsou.gui.series.view.SeriesView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.undo.RedoAction;
import org.designup.picsou.gui.undo.UndoAction;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.utils.DumpDataAction;
import org.designup.picsou.gui.utils.DataCheckerAction;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobListStringifiers;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.ReplicationGlobRepository;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class MainPanel {
  private PicsouFrame parent;
  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private BackupAction backupAction;
  private RestoreAction restoreAction;
  private PreferencesAction preferencesAction;
  private ExitAction exitAction;
  private GlobsPanelBuilder builder;
  private GlobRepository repository;
  private Directory directory;
  private MainWindow mainWindow;
  private RegisterLicenseAction registerAction;
  private Action dumpRepository;
  private MonthSummaryView monthSummary;
  private SeriesView seriesView;
  private DataCheckerAction checkRepository;

  public static MainPanel init(GlobRepository repository, Directory directory,
                               MainWindow mainWindow) {
    MainPanel panel = new MainPanel(repository, directory, mainWindow);
    mainWindow.getFrame().setRepository(repository);
    return panel;
  }

  private MainPanel(final GlobRepository repository, Directory directory, MainWindow mainWindow) {
    this.repository = repository;
    this.directory = directory;
    this.mainWindow = mainWindow;
    this.parent = mainWindow.getFrame();
    directory.add(JFrame.class, parent);
    directory.add(new UndoRedoService(repository, directory));
    directory.add(new HelpService(repository, directory));

    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/picsou.splits", repository, directory);

    TitleView titleView = new TitleView(repository, directory);

    TransactionSelection transactionSelection = new TransactionSelection(repository, directory);

    TransactionView transactionView = new TransactionView(repository, directory, transactionSelection);
    CategorizationView categorizationView = new CategorizationView(repository, directory);
    seriesView = new SeriesView(repository, directory);
    TimeView timeView = new TimeView(repository, directory);

    directory.add(new NavigationService(categorizationView, seriesView, repository, directory));

    importFileAction = ImportFileAction.initAndRegisterToOpenRequestManager(Lang.get("import"), repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    backupAction = new BackupAction(repository, directory);
    restoreAction = new RestoreAction(repository, directory);
    preferencesAction = new PreferencesAction(repository, directory);
    registerAction = new RegisterLicenseAction(repository, directory);
    dumpRepository = new DumpDataAction(repository);
    checkRepository = new DataCheckerAction(repository);
    exitAction = new ExitAction(directory);

    TextFilterPanel search = new TextFilterPanel(transactionView.getFilterSet(), repository, directory) {
      protected GlobMatcher createMatcher(String searchFilter) {
        return or(fieldContainsIgnoreCase(Transaction.LABEL, searchFilter),
                  fieldContainsIgnoreCase(Transaction.NOTE, searchFilter));
      }
    };
    builder.add("transactionSearch", search.getPanel());
    builder.addLabel("sum", Transaction.TYPE,
                     GlobListStringifiers.sum(Formatting.DECIMAL_FORMAT, Transaction.AMOUNT))
      .setAutoHideIfEmpty(true);

    monthSummary = new MonthSummaryView(importFileAction, repository, directory);

    LicenseInfoView licenseInfoView = new LicenseInfoView(repository, directory);

    ReplicationGlobRepository replicationGlobRepository =
      new ReplicationGlobRepository(repository, PeriodSeriesStat.TYPE);

    PeriodSeriesStatUpdater.init(replicationGlobRepository, directory);

    createPanel(
      titleView,
      transactionView,
      timeView,
      new VersionInfoView(repository, directory),
      new AccountView(repository, directory),
      monthSummary,
      categorizationView,
      new CardView(repository, directory),
      new NavigationView(repository, directory),
      new BudgetView(replicationGlobRepository, directory),
      seriesView,
      new SeriesEvolutionView(repository, directory),
      new SavingsView(replicationGlobRepository, directory),
      licenseInfoView);

    createMenuBar(parent, directory);
    timeView.selectCurrentMonth();
  }

  private void selectLastMonthWithATransaction(GlobRepository repository, Directory directory) {
    GlobList transactions = repository.getAll(Transaction.TYPE,
                                              GlobMatchers.fieldEquals(Transaction.PLANNED, false));
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
      public void load(Component component) {
        JPanel panel = (JPanel)component;
        mainWindow.setPanel(panel);
      }
    });

  }

  public void show() {

    builder.load();

    monthSummary.init();

    selectLastMonthWithATransaction(repository, directory);
    seriesView.selectAll();

    SplitsEditor.show(builder, parent);
  }

  public void createMenuBar(final PicsouFrame frame, Directory directory) {
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(createFileMenu());
    menuBar.add(createEditMenu(frame, directory));
    menuBar.add(createHelpMenu(directory));
    frame.setJMenuBar(menuBar);
  }

  private JMenu createFileMenu() {
    JMenu menu = new JMenu(Lang.get("file"));
    menu.add(importFileAction);
    menu.add(exportFileAction);
    menu.addSeparator();
    menu.add(backupAction);
    menu.add(restoreAction);

    if (useMacOSMenu()) {
      MRJAdapter.setPreferencesEnabled(true);
      MRJAdapter.addPreferencesListener(preferencesAction);
    }
    else {
      menu.addSeparator();
      menu.add(preferencesAction);
    }

    menu.addSeparator();
    menu.add(registerAction);

    if (useMacOSMenu()) {
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
    editMenu.add(dumpRepository);
    editMenu.add(checkRepository);
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

    AboutAction aboutAction = new AboutAction(repository, directory);
    if (useMacOSMenu()) {
      MRJAdapter.addAboutListener(aboutAction);
    }
    else {
      menu.addSeparator();
      menu.add(aboutAction);
    }
    return menu;
  }

  private boolean useMacOSMenu() {
    boolean result = GuiUtils.isMacOSX();
    Utils.beginRemove();
    result = false;
    Utils.endRemove();
    return result;
  }

}
