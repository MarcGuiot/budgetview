package org.designup.picsou.gui;

import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ExportFileAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.components.JWavePanel;
import org.designup.picsou.gui.graphics.CategoriesChart;
import org.designup.picsou.gui.graphics.HistoricalChart;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.InformationView;
import org.designup.picsou.gui.transactions.TransactionDetailsView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.Category;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
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
    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/picsou.splits", repository, directory);

    TransactionSelection transactionSelection = new TransactionSelection(repository, directory);

    TransactionView transactionView = new TransactionView(repository, directory, transactionSelection);
    TransactionDetailsView transactionDetailsView = new TransactionDetailsView(repository, directory);
    CategoryView categoryView = new CategoryView(repository, directory);
    TimeView timeView = new TimeView(repository, directory);

    importFileAction = new ImportFileAction(repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    exitAction = new ExitAction(directory);

    createPanel(directory,
                new TitleView(repository, directory),
                new InformationView(repository, directory, transactionSelection),
                transactionView,
                transactionDetailsView,
                timeView,
                categoryView,
                new CardView(repository, directory, transactionSelection),
                new HistoricalChart(repository, directory),
                new CategoriesChart(repository, directory, transactionSelection));

    timeView.selectLastMonth();
    categoryView.select(Category.ALL);
    initMonthSelection(directory);

    createMenuBar(parent);
  }

  private void initMonthSelection(Directory directory) {
    final SelectionService selectionService = directory.get(SelectionService.class);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        selectionService.clear(Transaction.TYPE);
      }
    }, Month.TYPE);
  }

  private void createPanel(Directory directory, View... views) {
    for (View view : views) {
      view.registerComponents(builder);
    }
    builder.add("chartAreaPanel", new JWavePanel(directory.get(ColorService.class)));
    builder.add("verticalSplit", createSplitPane());
    builder.add("horizontalSplit", createSplitPane());
    builder.addLoader(new SplitsLoader() {
      public void load(Component component) {
        JPanel panel = (JPanel)component;
        mainWindow.setPanel(panel);
      }
    });

    builder.load();
    SplitsEditor.show(builder, parent);
  }

  public void createMenuBar(JFrame frame) {
    JMenu fileMenu = new JMenu(Lang.get("file"));
    fileMenu.add(importFileAction);
    fileMenu.add(exportFileAction);
    fileMenu.addSeparator();
    fileMenu.add(exitAction);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);

    frame.setJMenuBar(menuBar);
  }

  private JSplitPane createSplitPane() {
    JSplitPane splitPane = new JSplitPane();
    splitPane.setUI(new BasicSplitPaneUI() {
      public BasicSplitPaneDivider getDivider() {
        return new BasicSplitPaneDivider(this) {
          public void paint(Graphics g) {
          }
        };
      }
    });
    return splitPane;
  }

  public void openInFront() {
    final JDialog jDialog = new JDialog(parent);
    jDialog.setVisible(true);
    jDialog.setVisible(false);
  }
}
