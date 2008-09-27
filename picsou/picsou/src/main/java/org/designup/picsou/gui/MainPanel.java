package org.designup.picsou.gui;

import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ExportFileAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.actions.PreferencesAction;
import org.designup.picsou.gui.budget.BudgetView;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.categories.actions.EditCategoriesAction;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.filtering.TextFilterPanel;
import org.designup.picsou.gui.graphics.CategoriesChart;
import org.designup.picsou.gui.graphics.HistoricalChart;
import org.designup.picsou.gui.license.LicenseDialog;
import org.designup.picsou.gui.monthsummary.BalanceSummaryView;
import org.designup.picsou.gui.monthsummary.MonthSummaryView;
import org.designup.picsou.gui.series.view.SeriesView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.undo.RedoAction;
import org.designup.picsou.gui.undo.UndoAction;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.triggers.GlobStateChecker;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.fieldContainsIgnoreCase;
import static org.globsframework.model.utils.GlobMatchers.or;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.StringWriter;

public class MainPanel {
  private PicsouFrame parent;
  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private PreferencesAction preferencesFileAction;
  private ExitAction exitAction;
  protected GlobsPanelBuilder builder;
  private MainWindow mainWindow;
  private RegisterLicenseAction registerAction;
  private CheckRepositoryAction check;

  public static MainPanel show(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    MainPanel panel = new MainPanel(repository, directory, mainWindow);
    mainWindow.getFrame().setRepository(repository);
    return panel;
  }

  private MainPanel(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    this.mainWindow = mainWindow;
    this.parent = mainWindow.getFrame();
    directory.add(JFrame.class, parent);
    directory.add(new UndoRedoService(repository, directory));

    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/picsou.splits", repository, directory);

    TitleView titleView = new TitleView(repository, directory);

    TransactionSelection transactionSelection = new TransactionSelection(repository, directory);

    TransactionView transactionView = new TransactionView(repository, directory, transactionSelection);
    CategorizationView categorizationView = new CategorizationView(repository, directory);
    CategoryView categoryView = new CategoryView(repository, directory);
    SeriesView seriesView = new SeriesView(repository, directory);
    TimeView timeView = new TimeView(repository, directory);

    directory.add(new NavigationService(categorizationView, categoryView, seriesView,
                                        repository, directory));

    importFileAction = ImportFileAction.initAndRegisterInOpenRequestManager(Lang.get("import"), repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    preferencesFileAction = new PreferencesAction(repository, directory);
    registerAction = new RegisterLicenseAction(parent, repository, directory);
    check = new CheckRepositoryAction(repository);
    exitAction = new ExitAction(directory);

    builder.add("editCategories", new EditCategoriesAction(repository, directory));

    TextFilterPanel search = new TextFilterPanel(transactionView.getFilterSet(), repository, directory) {
      protected GlobMatcher createMatcher(String searchFilter) {
        return or(fieldContainsIgnoreCase(Transaction.LABEL, searchFilter),
                  fieldContainsIgnoreCase(Transaction.NOTE, searchFilter));
      }
    };
    builder.add("transactionSearch", search.getPanel());

    MonthSummaryView monthSummary = new MonthSummaryView(importFileAction, repository, directory);

    createPanel(
      titleView,
      transactionView,
      timeView,
      categoryView,
      new AccountView(repository, directory),
      monthSummary,
      new BalanceSummaryView(repository, directory),
      categorizationView,
      new CardView(repository, directory),
      new BudgetView(repository, directory),
      seriesView,
      new HistoricalChart(repository, directory),
      new CategoriesChart(repository, directory, transactionSelection));

    monthSummary.init();

    selectLastMonthWithATransaction(repository, directory);
    categoryView.selectAll();
    seriesView.selectAll();

    createMenuBar(parent, directory);
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

    builder.load();
    SplitsEditor.show(builder, parent);
  }

  public void createMenuBar(final PicsouFrame frame, Directory directory) {
    JMenu fileMenu = new JMenu(Lang.get("file"));
    fileMenu.add(importFileAction);
    fileMenu.add(exportFileAction);
    fileMenu.addSeparator();
    fileMenu.add(preferencesFileAction);
    fileMenu.addSeparator();
    fileMenu.add(registerAction);
    fileMenu.addSeparator();
    fileMenu.add(exitAction);

    JMenu editMenu = new JMenu(Lang.get("edit"));
    final UndoAction undoAction = new UndoAction(directory);
    editMenu.add(undoAction);
    final RedoAction redoAction = new RedoAction(directory);
    editMenu.add(redoAction);

    Utils.beginRemove();
    editMenu.add(check);
    Utils.endRemove();

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(editMenu);
    frame.setJMenuBar(menuBar);

    JRootPane rootPane = frame.getRootPane();
    GuiUtils.addShortcut(rootPane, "UNDO", undoAction,
                         KeyStroke.getKeyStroke(KeyEvent.VK_Z, Gui.getCtrlModifier()));
    GuiUtils.addShortcut(rootPane, "REDO", redoAction,
                         KeyStroke.getKeyStroke(KeyEvent.VK_Y, Gui.getCtrlModifier()));
  }

  private static class RegisterLicenseAction extends AbstractAction {
    private Frame parent;
    private GlobRepository repository;
    private Directory directory;

    public RegisterLicenseAction(Frame parent, GlobRepository repository, Directory directory) {
      super(Lang.get("license.register"));
      this.parent = parent;
      this.repository = repository;
      this.directory = directory;
    }

    public void actionPerformed(ActionEvent e) {
      LicenseDialog dialog = new LicenseDialog(parent, repository, directory);
      dialog.show();
    }
  }

  private static class CheckRepositoryAction extends AbstractAction {
    private GlobRepository repository;

    public CheckRepositoryAction(GlobRepository repository) {
      super("check");
      this.repository = repository;
    }

    public void actionPerformed(ActionEvent e) {
      GlobStateChecker globStateChecker = new GlobStateChecker(repository);
      if (!globStateChecker.check()) {
        java.util.List<GlobStateChecker.Correcteur> correcteurs = globStateChecker.getCorrecteurs();
        StringWriter stringWriter = new StringWriter();
        for (GlobStateChecker.Correcteur correcteur : correcteurs) {
          stringWriter.append(correcteur.info()).append("\n-----------------------------------------\n");
        }
        throw new RuntimeException(stringWriter.toString());
      }
    }
  }
}
