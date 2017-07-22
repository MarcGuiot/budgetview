package com.budgetview.desktop;

import com.budgetview.desktop.accounts.AccountView;
import com.budgetview.desktop.actions.DeleteUserAction;
import com.budgetview.desktop.actions.ExitAction;
import com.budgetview.desktop.actions.ImportFileAction;
import com.budgetview.desktop.addons.AddOnsSelector;
import com.budgetview.desktop.addons.AddOnsView;
import com.budgetview.desktop.analysis.AnalysisSelector;
import com.budgetview.desktop.analysis.AnalysisView;
import com.budgetview.desktop.backup.BackupService;
import com.budgetview.desktop.budget.BudgetView;
import com.budgetview.desktop.card.CardView;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.categorization.CategorizationSelector;
import com.budgetview.desktop.categorization.CategorizationView;
import com.budgetview.desktop.components.PicsouFrame;
import com.budgetview.desktop.components.highlighting.HighlightingService;
import com.budgetview.desktop.components.layoutconfig.LayoutConfigService;
import com.budgetview.desktop.dashboard.DashboardStatUpdater;
import com.budgetview.desktop.dashboard.DashboardView;
import com.budgetview.desktop.feedback.FeedbackService;
import com.budgetview.desktop.help.HelpService;
import com.budgetview.desktop.license.activation.LicenseInfoView;
import com.budgetview.desktop.mobile.SendMobileDataAction;
import com.budgetview.desktop.model.PeriodAccountStat;
import com.budgetview.desktop.model.PeriodBudgetAreaStat;
import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.desktop.notifications.NotificationsFlagView;
import com.budgetview.desktop.projects.ProjectSelector;
import com.budgetview.desktop.projects.ProjectView;
import com.budgetview.desktop.series.PeriodAccountStatUpdater;
import com.budgetview.desktop.series.PeriodBudgetAreaStatUpdater;
import com.budgetview.desktop.series.PeriodSeriesStatUpdater;
import com.budgetview.desktop.series.SeriesEditor;
import com.budgetview.desktop.signpost.SignpostService;
import com.budgetview.desktop.signpost.SignpostView;
import com.budgetview.desktop.signpost.WelcomeView;
import com.budgetview.desktop.signpost.components.OnboardingCompletionUpdater;
import com.budgetview.desktop.signpost.guides.ImportSignpost;
import com.budgetview.desktop.startup.components.DemoMessageView;
import com.budgetview.desktop.startup.components.LogoutService;
import com.budgetview.desktop.startup.components.OpenRequestManager;
import com.budgetview.desktop.summary.version.NewVersionView;
import com.budgetview.desktop.time.TimeView;
import com.budgetview.desktop.title.PeriodView;
import com.budgetview.desktop.transactions.TransactionView;
import com.budgetview.desktop.undo.UndoRedoService;
import com.budgetview.desktop.utils.FrameSize;
import com.budgetview.desktop.utils.MacOSXHooks;
import com.budgetview.desktop.utils.MainPanelContainer;
import com.budgetview.desktop.utils.MenuBarBuilder;
import com.budgetview.model.LayoutConfig;
import com.budgetview.model.Month;
import com.budgetview.model.SignpostStatus;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
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
  private CategorizationSelector categorizationSelector;
  private DashboardView dashboardView;
  private final BudgetView budgetView;

  public static MainPanel init(GlobRepository repository, Directory directory, WindowManager mainWindow) {
    LayoutConfig.init(repository, directory);
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

    PeriodView periodView = new PeriodView(repository, directory);
    timeView = new TimeView(repository, directory);

    transactionView = new TransactionView(repository, directory);
    categorizationSelector = new CategorizationSelector(repository, directory);
    categorizationView = new CategorizationView(categorizationSelector, repository);
    cardView = new CardView(repository, directory, categorizationSelector.getGotoBudgetSignpost());

    ReplicationGlobRepository replicationGlobRepository =
      new ReplicationGlobRepository(repository, PeriodSeriesStat.TYPE, PeriodBudgetAreaStat.TYPE, PeriodAccountStat.TYPE);

    MainPanelContainer mainPanelContainer = new MainPanelContainer(replicationGlobRepository, directory);
    builder.add("mainPanel", mainPanelContainer);

    projectView = new ProjectView(repository, directory);

    budgetView = new BudgetView(replicationGlobRepository, directory);

    NavigationService navigationService = new NavigationService(mainPanelContainer, transactionView, categorizationSelector, projectView, repository, directory);
    directory.add(navigationService);
    directory.get(BackupService.class).addPostRestoreTrigger(new BackupService.Trigger() {
      public void process(GlobRepository repository) {
        boolean onboardingCompleted = SignpostStatus.isOnboardingCompleted(repository);
        directory.get(NavigationService.class).gotoHomeAfterRestore(onboardingCompleted);
      }
    });

    menuBar = new MenuBarBuilder(repository, replicationGlobRepository,
                                 windowManager, logoutService,
                                 directory,
                                 new DeleteUserAction(this, repository, directory));

    MacOSXHooks.install(menuBar.getAboutAction(), menuBar.getPreferencesAction(), menuBar.getExitAction(), directory);

    JButton importFile = new JButton(menuBar.getImportFileAction());
    builder.add("importFile", importFile);
    final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
    importSignpost.attach(importFile);

    PeriodSeriesStatUpdater.init(replicationGlobRepository, directory);
    PeriodBudgetAreaStatUpdater.init(replicationGlobRepository);
    PeriodAccountStatUpdater.init(replicationGlobRepository, directory);
    OnboardingCompletionUpdater.init(replicationGlobRepository);
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
      categorizationSelector,
      categorizationView,
      cardView,
      budgetView,
      analysisSelector,
      analysisView,
      new ProjectSelector(repository, directory),
      projectView,
      new WelcomeView(replicationGlobRepository, directory),
      new AddOnsSelector(repository, directory),
      new AddOnsView(repository, directory),
      signpostView,
      new NotificationsFlagView(repository, directory)
    );

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
        panel = (JPanel) component;
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
    categorizationSelector.reset();
    directory.get(NavigationService.class).reset();
    directory.get(UndoRedoService.class).reset();
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
    directory.get(OpenRequestManager.class).popCallback();
  }

  public void updateMobile() {
    SendMobileDataAction.sendToMobile(repository, directory, new Ref<String>(), false);
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
