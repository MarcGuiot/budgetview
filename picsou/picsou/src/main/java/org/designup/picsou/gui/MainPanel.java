package org.designup.picsou.gui;

import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ExportFileAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.budget.BudgetView;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.categories.actions.EditCategoriesAction;
import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.graphics.CategoriesChart;
import org.designup.picsou.gui.graphics.HistoricalChart;
import org.designup.picsou.gui.license.LicenseDialog;
import org.designup.picsou.gui.monthsummary.MonthSummaryView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.transactions.UncategorizedMessageView;
import org.designup.picsou.gui.undo.RedoAction;
import org.designup.picsou.gui.undo.UndoAction;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.model.Category;
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
  private ExitAction exitAction;
  protected GlobsPanelBuilder builder;
  private MainWindow mainWindow;
  private RegisterLicenseAction registerAction;
  private CheckRepoAction check;

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

    CategorizationDialog categorizationDialog = new CategorizationDialog(repository, directory);

    TransactionSelection transactionSelection = new TransactionSelection(repository, directory);

    TransactionView transactionView = new TransactionView(repository, directory, transactionSelection);
    TransactionDetailsView transactionDetailsView = new TransactionDetailsView(repository, directory, transactionView);
    CategoryView categoryView = new CategoryView(repository, directory);
    TimeView timeView = new TimeView(repository, directory);

    importFileAction = ImportFileAction.initAndRegisterInOpenRequestManager(Lang.get("import"), repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    registerAction = new RegisterLicenseAction(parent, repository, directory);
    check = new CheckRepoAction(parent, repository);
    exitAction = new ExitAction(directory);

    builder.add("editCategories", new EditCategoriesAction(repository, directory));

    createPanel(
      new TitleView(repository, directory),
      new UncategorizedMessageView("uncategorizedMessage", "categorizeRemainingTransactions",
                                   Lang.get("transaction.allocation.action"),
                                   repository, directory),
      new UncategorizedMessageView("uncategorizedMessage_details", "categorizeRemainingTransactions_details",
                                   Lang.get("transaction.allocation.action"),
                                   repository, directory),
      transactionView,
      transactionDetailsView,
      timeView,
      categoryView,
      new AccountView(repository, directory),
      new MonthSummaryView(repository, directory),
      categorizationDialog,
      new CardView(repository, directory),
      new BudgetView(repository, directory),
      new HistoricalChart(repository, directory),
      new CategoriesChart(repository, directory, transactionSelection));

    selectLastMonthWithATransaction(repository, directory);
    categoryView.select(Category.ALL);

    createMenuBar(parent, directory);
  }

  private void selectLastMonthWithATransaction(GlobRepository repository, Directory directory) {
    GlobList transactions = repository.getAll(Transaction.TYPE);
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
                         KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
    GuiUtils.addShortcut(rootPane, "REDO", redoAction,
                         KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
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

  private static class CheckRepoAction extends AbstractAction {
    private PicsouFrame parent;
    private GlobRepository repository;

    public CheckRepoAction(PicsouFrame parent, GlobRepository repository) {
      super("check");
      this.parent = parent;
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
