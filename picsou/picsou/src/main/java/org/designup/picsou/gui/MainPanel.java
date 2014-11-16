package org.designup.picsou.gui;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.DeleteUserAction;
import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.analysis.AnalysisSelector;
import org.designup.picsou.gui.analysis.AnalysisView;
import org.designup.picsou.gui.budget.BudgetView;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.categorization.CategorizationSelectionView;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.highlighting.HighlightingService;
import org.designup.picsou.gui.components.layoutconfig.LayoutConfigService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.dashboard.DashboardStatUpdater;
import org.designup.picsou.gui.dashboard.DashboardView;
import org.designup.picsou.gui.feedback.FeedbackService;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.license.activation.LicenseInfoView;
import org.designup.picsou.gui.mobile.SendMobileDataAction;
import org.designup.picsou.gui.model.PeriodAccountStat;
import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.notifications.NotificationsFlagView;
import org.designup.picsou.gui.projects.ProjectView;
import org.designup.picsou.gui.series.PeriodAccountStatUpdater;
import org.designup.picsou.gui.series.PeriodBudgetAreaStatUpdater;
import org.designup.picsou.gui.series.PeriodSeriesStatUpdater;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.signpost.WelcomeView;
import org.designup.picsou.gui.signpost.SignpostService;
import org.designup.picsou.gui.signpost.SignpostView;
import org.designup.picsou.gui.signpost.guides.ImportSignpost;
import org.designup.picsou.gui.startup.components.DemoMessageView;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.summary.ProjectSelector;
import org.designup.picsou.gui.summary.version.NewVersionView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.PeriodView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.MainPanelContainer;
import org.designup.picsou.gui.utils.MenuBarBuilder;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.repository.ReplicationGlobRepository;
import org.globsframework.utils.Ref;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

import static org.globsframework.model.utils.GlobMatchers.isFalse;

public class MainPanel {
  private final AnalysisSelector analysisSelector;
  private PicsouFrame frame;
  private static ExitAction exitActionWhitoutUserEvaluation;
  private GlobsPanelBuilder builder;
  private GlobRepository repository;
  private Directory directory;
  private WindowManager windowManager;
  private JPanel panel;
  private TimeView timeView;
  private CardView cardView;
  private TransactionView transactionView;
  private AnalysisView analysisView;
  private CategorizationView categorizationView;
  private ProjectView projectView;
  private SignpostView signpostView;
  private MenuBarBuilder menuBar;
  private CategorizationSelectionView categorizationSelectionView;
  private DashboardView dashboardView;
  private final BudgetView budgetView;

  public static MainPanel init(GlobRepository repository, Directory directory, WindowManager mainWindow) {
    MainPanel panel = new MainPanel(repository, directory, mainWindow);
    mainWindow.getFrame().setRepository(repository);
    return panel;
  }

  private MainPanel(final GlobRepository repository, final Directory directory, WindowManager windowManager) {
    this.repository = repository;
    this.directory = directory;
    this.windowManager = windowManager;
    this.frame = windowManager.getFrame();
    directory.add(new UndoRedoService(repository));
    directory.add(new HelpService(repository, directory));
    directory.add(new FeedbackService(repository, directory));
    directory.add(new LayoutConfigService(repository, directory));
    LogoutService logoutService = new MainPanelLogoutService();
    directory.add(LogoutService.class, logoutService);
    directory.add(new SignpostService(directory));

    directory.addFactory(SeriesEditor.class, new Directory.Factory<SeriesEditor>() {
      public SeriesEditor create() {
        Directory directorySelection = new DefaultDirectory(directory);
        directorySelection.add(SelectionService.class, new SelectionService());
        return new SeriesEditor(repository, directory);
      }
    });

    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/mainPanel.splits", repository, directory);

    builder.add("mainPanel", new MainPanelContainer(directory));

    PeriodView periodView = new PeriodView(repository, directory);
    timeView = new TimeView(repository, directory);

    transactionView = new TransactionView(repository, directory);
    categorizationSelectionView = new CategorizationSelectionView(repository, directory);
    categorizationView = new CategorizationView(categorizationSelectionView, repository);
    cardView = new CardView(repository, directory, categorizationSelectionView.getGotoBudgetSignpost());

    ReplicationGlobRepository replicationGlobRepository =
      new ReplicationGlobRepository(repository, PeriodSeriesStat.TYPE, PeriodBudgetAreaStat.TYPE, PeriodAccountStat.TYPE);
    projectView = new ProjectView(repository, directory);

    budgetView = new BudgetView(replicationGlobRepository, directory);

    directory.add(new NavigationService(transactionView, categorizationSelectionView, projectView, repository, directory));

    menuBar = new MenuBarBuilder(repository, replicationGlobRepository,
                                 windowManager, logoutService,
                                 cardView.getHelpAction(),
                                 directory,
                                 new DeleteUserAction(this, repository, directory));

    JButton importFile = new JButton(menuBar.getImportFileAction());
    builder.add("importFile", importFile);
    final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
    importSignpost.attach(importFile);

    PeriodSeriesStatUpdater.init(replicationGlobRepository, directory);
    PeriodBudgetAreaStatUpdater.init(replicationGlobRepository);
    PeriodAccountStatUpdater.init(replicationGlobRepository, directory);
    DashboardStatUpdater.init(repository, directory);

    directory.add(new HighlightingService());

    analysisView = new AnalysisView(repository, directory);
    analysisSelector = new AnalysisSelector(repository, directory);
    signpostView = new SignpostView(replicationGlobRepository, directory);
    dashboardView = new DashboardView(repository, directory);
    createPanel(
      periodView,
      new AccountView(replicationGlobRepository, directory),
      dashboardView,
      new LicenseInfoView(repository, directory),
      transactionView,
      timeView,
      new NewVersionView(repository, directory),
      new DemoMessageView(repository, directory),
      categorizationSelectionView,
      categorizationView,
      cardView,
      budgetView,
      analysisSelector,
      analysisView,
      new ProjectSelector(repository, directory),
      projectView,
      new WelcomeView(replicationGlobRepository, directory),
      signpostView,
      new NotificationsFlagView(repository, directory));

    if (Gui.useMacOSMenu()) {
      if (exitActionWhitoutUserEvaluation != null) {
        MRJAdapter.removeQuitApplicationListener(exitActionWhitoutUserEvaluation);
        exitActionWhitoutUserEvaluation = null;
      }
      MRJAdapter.addQuitApplicationListener(menuBar.getExitAction());
    }

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

  public void prepareForDisplay() {
    ImportFileAction.registerToOpenRequestManager(Lang.get("import"), repository, directory);

    menuBar.createMenuBar(frame);
    cardView.showInitialCard();
    budgetView.reset();
    dashboardView.reset();
    transactionView.reset();
    categorizationSelectionView.reset();
    directory.get(NavigationService.class).reset();
    directory.get(UndoRedoService.class).reset();
    directory.get(HelpService.class).reset();
    directory.get(SelectionService.class).clearAll();
    signpostView.reset();
    projectView.reset();

    windowManager.setPanel(panel);
    analysisView.reset();
    analysisSelector.reset();
    timeView.selectCurrentMonth();
    selectLastMonthWithATransaction(repository, directory);
    timeView.centerToSelected();

    SplitsEditor.show(builder, frame);

    showInitialMessageIfNeeded();
  }

  private void showInitialMessageIfNeeded() {
    if (!repository.contains(Transaction.TYPE)) {
      SignpostStatus.init(repository);
    }
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
      MRJAdapter.removeQuitApplicationListener(menuBar.getExitAction());
      if (exitActionWhitoutUserEvaluation == null) {
        exitActionWhitoutUserEvaluation = new ExitAction(windowManager, repository, directory, false);
        MRJAdapter.addQuitApplicationListener(exitActionWhitoutUserEvaluation);
      }
    }
    directory.get(OpenRequestManager.class).popCallback();
  }

  public void updateMobile() {
    SendMobileDataAction.sendToMobile(repository, directory.get(ConfigService.class), new Ref<String>(), false);
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
