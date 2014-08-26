package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.accounts.AccountViewPanel;
import org.designup.picsou.gui.accounts.actions.AccountPopupFactory;
import org.designup.picsou.gui.accounts.chart.AccountPositionsChartView;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.utils.AccountPositionStringifier;
import org.designup.picsou.gui.components.JPopupButton;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.utils.BooleanListener;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import static org.designup.picsou.gui.utils.Matchers.*;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class AccountChartsPanel {
  private final AccountType accountType;
  private final HistoChartRange shortRange;
  private final HistoChartRange longRange;
  private final GlobRepository repository;
  private final Directory directory;
  private final ProjectChartView projects;
  private JPanel panel;

  public AccountChartsPanel(AccountType accountType, HistoChartRange shortRange, HistoChartRange longRange, GlobRepository repository, Directory directory, ProjectChartView projects) {
    this.accountType = accountType;
    this.shortRange = shortRange;
    this.longRange = longRange;
    this.repository = repository;
    this.directory = directory;
    this.projects = projects;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/components/accountChartsPanel.splits",
                                                      repository, directory);

    final BooleanField showListField =
      AccountType.MAIN.equals(accountType) ? UserPreferences.SHOW_MAIN_ACCOUNT_LIST_IN_SUMMARY : UserPreferences.SHOW_SAVINGS_ACCOUNT_LIST_IN_SUMMARY;
    Action toggleListAction = new ToggleBooleanAction(UserPreferences.KEY,
                                                      showListField,
                                                      Lang.get("summaryView.showAccountSummary"),
                                                      Lang.get("summaryView.showAccountList"),
                                                      repository);
    JPopupMenu titleMenu = new JPopupMenu();
    titleMenu.add(toggleListAction);
    String sectionTitleKey = AccountType.MAIN.equals(accountType) ? "account.summary.main" : "account.summary.savings";
    builder.add("sectionTitleButton", new JPopupButton(Lang.get(sectionTitleKey), titleMenu));

    final GlobRepeat repeat = builder.addRepeat("accountCharts", Account.TYPE,
                                                GlobMatchers.NONE,
                                                new AccountComparator(),
                                                new AccountRepeatFactory());
    final Integer summaryId =
      AccountType.MAIN.equals(accountType) ? Account.MAIN_SUMMARY_ACCOUNT_ID : Account.SAVINGS_SUMMARY_ACCOUNT_ID;
    KeyChangeListener updater = new KeyChangeListener(UserPreferences.KEY) {
      public void update() {
        Glob prefs = repository.find(UserPreferences.KEY);
        if (prefs != null) {
          boolean showList = prefs.isTrue(showListField);
          repeat.setFilter(showList ? userCreatedAccounts(accountType) : fieldEquals(Account.ID, summaryId));
        }
      }
    };
    repository.addChangeListener(updater);
    updater.update();

    panel = builder.load();
    panel.setName(getName(accountType));
  }

  public static String getName(AccountType type) {
    return "accountChartsPanel:" + type.getName();
  }

  private class AccountRepeatFactory implements RepeatComponentFactory<Glob> {

    public void registerComponents(PanelBuilder cellBuilder, Glob account) {

      if (Account.isUserCreatedAccount(account)) {
        registerUserCreatedAccountComponents(cellBuilder, account);
      }
      else {
        registerSummaryAccountComponents(cellBuilder, account);
      }
    }

    public void registerUserCreatedAccountComponents(PanelBuilder cellBuilder, Glob account) {
      final AccountPopupFactory popupFactory = new AccountPopupFactory(account, repository, directory);
      GlobButtonView accountButton = AccountViewPanel.createEditAccountButton(account, popupFactory, repository, directory);
      cellBuilder.add("accountChartButton", accountButton.getComponent());
      cellBuilder.addDisposable(accountButton);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .forceSelection(account.getKey());
      cellBuilder.add("accountPositionLabel", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);

      final AccountPositionsChartView accountChart =
        AccountPositionsChartView.full(account.get(Account.ID), "accountHistoChart", shortRange, repository, directory);
      final SplitsNode<HistoChart> chartNode = accountChart.registerComponents(cellBuilder);

      BooleanFieldListener showHide =
        BooleanFieldListener.installNodeStyle(account.getKey(), Account.SHOW_GRAPH,
                                              chartNode, "accountChartShown", "accountChartHidden", repository);
      cellBuilder.addDisposable(showHide);
    }

    public void registerSummaryAccountComponents(PanelBuilder cellBuilder, Glob account) {

      JButton hidden = new JButton(Lang.get(Account.isMain(account) ? "account.summary.main" : "account.summary.savings"));
      hidden.setVisible(false);
      hidden.setEnabled(false);
      cellBuilder.add("accountChartButton", hidden);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .forceSelection(account.getKey());
      cellBuilder.add("accountPositionLabel", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);

      final MainDailyPositionsChartView chartView =
        new MainDailyPositionsChartView(shortRange,
                                        AccountPositionsChartView.FULL_CONFIG,
                                        "accountHistoChart", repository, directory, "daily.budgetSummary");
      chartView.setAccount(Account.isMain(account) ? userCreatedMainAccounts() : userCreatedSavingsAccounts());
      SplitsNode<HistoChart> node = chartView.registerComponents(cellBuilder);
      node.applyStyle("accountChartShown");
      cellBuilder.addDisposable(chartView);
    }
  }
}
