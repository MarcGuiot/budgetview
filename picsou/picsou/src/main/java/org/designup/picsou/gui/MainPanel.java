package org.designup.picsou.gui;

import net.roydesign.mac.MRJAdapter;
import org.designup.picsou.gui.accounts.AccountView;
import org.designup.picsou.gui.actions.DeleteUserAction;
import org.designup.picsou.gui.actions.ExitAction;
import org.designup.picsou.gui.actions.ImportFileAction;
import org.designup.picsou.gui.budget.BudgetToggle;
import org.designup.picsou.gui.budget.BudgetView;
import org.designup.picsou.gui.card.CardView;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.categorization.CategorizationView;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.components.layoutconfig.LayoutConfigService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.feedback.FeedbackService;
import org.designup.picsou.gui.help.HelpService;
import org.designup.picsou.gui.license.LicenseInfoView;
import org.designup.picsou.gui.mobile.SendMobileDataAction;
import org.designup.picsou.gui.model.PeriodAccountStat;
import org.designup.picsou.gui.model.PeriodBudgetAreaStat;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.notifications.NotificationsFlagView;
import org.designup.picsou.gui.projects.ProjectView;
import org.designup.picsou.gui.savings.SavingsView;
import org.designup.picsou.gui.series.PeriodAccountStatUpdater;
import org.designup.picsou.gui.series.PeriodBudgetAreaStatUpdater;
import org.designup.picsou.gui.series.PeriodSeriesStatUpdater;
import org.designup.picsou.gui.series.SeriesEditor;
import org.designup.picsou.gui.series.analysis.SeriesAnalysisView;
import org.designup.picsou.gui.signpost.SignpostView;
import org.designup.picsou.gui.signpost.guides.ImportSignpost;
import org.designup.picsou.gui.startup.components.DemoMessageView;
import org.designup.picsou.gui.startup.components.LogoutService;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.summary.SummaryView;
import org.designup.picsou.gui.summary.version.NewVersionView;
import org.designup.picsou.gui.time.TimeView;
import org.designup.picsou.gui.title.PeriodView;
import org.designup.picsou.gui.title.TitleView;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.gui.undo.UndoRedoService;
import org.designup.picsou.gui.utils.Gui;
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
  private SeriesAnalysisView seriesAnalysisView;
  private CategorizationView categorizationView;
  private ProjectView projectView;
  private SignpostView signpostView;
  private MenuBarBuilder menuBar;

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

    directory.addFactory(SeriesEditor.class, new Directory.Factory<SeriesEditor>() {
      public SeriesEditor create() {
        Directory directorySelection = new DefaultDirectory(directory);
        directorySelection.add(SelectionService.class, new SelectionService());
        return new SeriesEditor(repository, directory);
      }
    });

    builder = new GlobsPanelBuilder(MainPanel.class, "/layout/picsou.splits", repository, directory);

    TitleView titleView = new TitleView(repository, directory);
    PeriodView periodView = new PeriodView(repository, directory);
    timeView = new TimeView(repository, directory);

    transactionView = new TransactionView(repository, directory);
    categorizationView = new CategorizationView(repository, directory);
    cardView = new CardView(repository, directory, categorizationView.getGotoBudgetSignpost());

    ReplicationGlobRepository replicationGlobRepository =
      new ReplicationGlobRepository(repository, PeriodSeriesStat.TYPE, PeriodBudgetAreaStat.TYPE, PeriodAccountStat.TYPE);
    projectView = new ProjectView(repository, directory);

    BudgetToggle budgetToggle = new BudgetToggle(repository);
    budgetToggle.registerComponents(builder);

    directory.add(new NavigationService(transactionView, categorizationView, projectView, budgetToggle, repository, directory));

    menuBar = new MenuBarBuilder(repository, replicationGlobRepository,
                                 windowManager, logoutService,
                                 cardView.getHelpAction(),
                                 directory,
                                 new DeleteUserAction(this, repository, directory));

    JButton importFile = new JButton(menuBar.getImportFileAction());
    builder.add("importFile", importFile);
    final ImportSignpost importSignpost = new ImportSignpost(repository, directory);
    importSignpost.attach(importFile);

    LicenseInfoView licenseInfoView = new LicenseInfoView(repository, directory);

    PeriodSeriesStatUpdater.init(replicationGlobRepository, directory);
    PeriodBudgetAreaStatUpdater.init(replicationGlobRepository);
    PeriodAccountStatUpdater.init(replicationGlobRepository, directory);

    seriesAnalysisView = new SeriesAnalysisView(repository, directory, menuBar.getPrintBudgetAction());
    signpostView = new SignpostView(replicationGlobRepository, directory);
    createPanel(
      titleView,
      periodView,
      transactionView,
      timeView,
      new NewVersionView(repository, directory),
      new DemoMessageView(repository, directory),
      new AccountView(repository, directory),
      categorizationView,
      cardView,
      new BudgetView(replicationGlobRepository, directory),
      seriesAnalysisView,
      new SavingsView(replicationGlobRepository, directory),
      new SummaryView(repository, directory),
      projectView,
      signpostView,
      licenseInfoView,
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
    transactionView.reset();
    categorizationView.reset();
    directory.get(NavigationService.class).reset();
    directory.get(UndoRedoService.class).reset();
    directory.get(HelpService.class).reset();
    signpostView.reset();
    projectView.reset();

    windowManager.setPanel(panel);
    seriesAnalysisView.reset();
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
