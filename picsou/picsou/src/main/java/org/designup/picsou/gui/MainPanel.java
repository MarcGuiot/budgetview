package org.designup.picsou.gui;

import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ExportFileAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.categorization.CategorizationDialog;
import org.designup.picsou.gui.graphics.CategoriesChart;
import org.designup.picsou.gui.graphics.HistoricalChart;
import org.designup.picsou.gui.monthsummary.MonthSummaryView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.transactions.UncategorizedMessageView;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class MainPanel {
  private JFrame parent;
  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private ExitAction exitAction;
  protected GlobsPanelBuilder builder;
  private MainWindow mainWindow;

  public static MainPanel show(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    MainPanel panel = new MainPanel(repository, directory, mainWindow);
    mainWindow.getFrame().setRepository(repository);
    return panel;
  }

  private MainPanel(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    this.mainWindow = mainWindow;
    this.parent = mainWindow.getFrame();
    directory.add(JFrame.class, parent);
    directory.add(new CategorizationDialog(parent, repository, directory));

    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/picsou.splits", repository, directory);

    TransactionSelection transactionSelection = new TransactionSelection(repository, directory);

    TransactionView transactionView = new TransactionView(repository, directory, transactionSelection);
    TransactionDetailsView transactionDetailsView = new TransactionDetailsView(repository, directory);
    CategoryView categoryView = new CategoryView(repository, directory);
    TimeView timeView = new TimeView(repository, directory);

    importFileAction = ImportFileAction.initAndRegisterInOpenRequestManager(repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    exitAction = new ExitAction(directory);

    createPanel(
      new TitleView(repository, directory),
      new UncategorizedMessageView(repository, directory, transactionSelection),
      transactionView,
      transactionDetailsView,
      timeView,
      categoryView,
      new AccountView(repository, directory),
      new MonthSummaryView(repository, directory),
      new CardView(repository, directory, transactionSelection),
      new HistoricalChart(repository, directory),
      new CategoriesChart(repository, directory, transactionSelection));

    timeView.selectLastMonth();
    categoryView.select(Category.ALL);

    createMenuBar(parent);
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

  public void createMenuBar(final JFrame frame) {
    JMenu fileMenu = new JMenu(Lang.get("file"));
    fileMenu.add(importFileAction);
    fileMenu.add(exportFileAction);
    fileMenu.addSeparator();
    fileMenu.add(exitAction);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);

    frame.setJMenuBar(menuBar);
  }
}
