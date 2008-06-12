package org.designup.picsou.gui;

import net.roydesign.event.ApplicationEvent;
import net.roydesign.mac.MRJAdapter;
import org.crossbowlabs.globs.gui.GlobsPanelBuilder;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorService;
import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ExportFileAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.categories.CategoryView;
import org.designup.picsou.gui.components.JWavePanel;
import org.designup.picsou.gui.graphics.CategoriesChart;
import org.designup.picsou.gui.graphics.HistoricalChart;
import org.designup.picsou.gui.scorecard.ScorecardView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.BalanceView;
import org.designup.picsou.gui.transactions.InformationView;
import org.designup.picsou.gui.transactions.TransactionSelection;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.Category;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class MainPanel {
  private JFrame parent;
  private JPanel panel;
  private ImportFileAction importFileAction;
  private ExportFileAction exportFileAction;
  private ExitAction exitAction;

  public static MainPanel show(GlobRepository repository, Directory directory, MainWindow mainWindow) {
    MainPanel panel = new MainPanel(repository, directory, mainWindow.getFrame());
    PicsouApplication.initialFile = null;
    mainWindow.setPanel(panel.panel);
    mainWindow.getFrame().setRepository(repository);
    return panel;
  }

  private MainPanel(GlobRepository repository, Directory directory, JFrame parent) {
    this.parent = parent;
    directory.add(JFrame.class, parent);

    TransactionSelection transactionSelection = new TransactionSelection(repository, directory);

    AccountView accountView = new AccountView(repository, directory);
    TransactionView transactionView = new TransactionView(repository, directory, transactionSelection);
    BalanceView balanceView = new BalanceView(repository, directory);
    CategoryView categoryView = new CategoryView(repository, directory);
    TimeView timeView = new TimeView(repository, directory);

    importFileAction = new ImportFileAction(repository, directory);
    exportFileAction = new ExportFileAction(repository, directory);
    exitAction = new ExitAction(directory);

    panel = createPanel(repository, directory,
                        new TitleView(repository, directory),
                        new InformationView(repository, directory, transactionSelection),
                        accountView,
                        transactionView,
                        balanceView,
                        timeView,
                        categoryView,
                        new CardView(repository, directory, transactionSelection),
                        new HistoricalChart(repository, directory),
                        new CategoriesChart(repository, directory, transactionSelection),
                        new ScorecardView(repository, directory, transactionSelection));

    accountView.selectFirst();
    timeView.selectLastMonth();
    categoryView.select(Category.ALL);
    MRJAdapter.addOpenDocumentListener(new AbstractAction() {
      public void actionPerformed(ActionEvent event) {
        openFile(((ApplicationEvent)event).getFile(), false);
      }
    });

    createMenuBar(parent);
  }

  private JPanel createPanel(GlobRepository repository, Directory directory, View... views) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(repository, directory);
    for (View view : views) {
      view.registerComponents(builder);
    }
    builder.add("chartAreaPanel", new JWavePanel(directory.get(ColorService.class)));
    builder.add("verticalSplit", createSplitPane());
    builder.add("horizontalSplit", createSplitPane());
    return (JPanel)builder.parse(MainPanel.class, "/layout/picsou.splits");
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

  public JPanel getJPanel() {
    return panel;
  }

  public void openFile(File file, boolean lastFile) {
    throw new RuntimeException();
// TODO:   importFileAction.processFile(file, lastFile);
  }

  public void openInFront() {
    final JDialog jDialog = new JDialog(parent);
    jDialog.setVisible(true);
    jDialog.setVisible(false);
  }
}
