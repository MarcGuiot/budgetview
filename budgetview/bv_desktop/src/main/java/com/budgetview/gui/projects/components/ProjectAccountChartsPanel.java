package com.budgetview.gui.projects.components;

import com.budgetview.gui.accounts.AccountViewPanel;
import com.budgetview.gui.components.charts.histo.HistoChart;
import com.budgetview.gui.accounts.actions.AccountPopupFactory;
import com.budgetview.gui.accounts.chart.AccountPositionsChartView;
import com.budgetview.gui.accounts.chart.MainDailyPositionsChartView;
import com.budgetview.gui.accounts.utils.AccountMatchers;
import com.budgetview.gui.accounts.utils.AccountPositionStringifier;
import com.budgetview.gui.analysis.histobuilders.range.HistoChartAdjustableRange;
import com.budgetview.gui.analysis.histobuilders.range.HistoChartRange;
import com.budgetview.gui.components.JPopupButton;
import com.budgetview.gui.description.stringifiers.AccountComparator;
import com.budgetview.model.Account;
import com.budgetview.model.AccountType;
import com.budgetview.model.ProjectAccountGraph;
import com.budgetview.model.UserPreferences;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.editors.GlobToggleEditor;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

import java.util.ArrayList;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class ProjectAccountChartsPanel implements HistoChartAdjustableRange {
  private final AccountType accountType;
  private final GlobRepository repository;
  private final Directory directory;
  private HistoChartRange currentRange;
  private java.util.List<HistoChartAdjustableRange> charts = new ArrayList<HistoChartAdjustableRange>();
  private JPanel panel;

  public ProjectAccountChartsPanel(AccountType accountType, HistoChartRange range, GlobRepository repository, Directory directory) {
    this.accountType = accountType;
    this.currentRange = range;
    this.repository = repository;
    this.directory = directory;
  }

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  private void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/components/projectAccountChartsPanel.splits",
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
          repeat.setFilter(showList ? AccountMatchers.userCreatedAccounts(accountType) : fieldEquals(Account.ID, summaryId));
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

  public void setRange(HistoChartRange range) {
    this.currentRange = range;
    for (HistoChartAdjustableRange chart : charts) {
      chart.setRange(currentRange);
    }
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

      cellBuilder.add("toggleGraph", GlobToggleEditor.init(ProjectAccountGraph.SHOW, repository, directory)
        .forceSelection(Key.create(ProjectAccountGraph.TYPE, account.get(Account.ID)))
        .getComponent());

      final AccountPopupFactory popupFactory = new AccountPopupFactory(account, repository, directory, true);
      GlobButtonView accountButton = AccountViewPanel.createEditAccountButton(account, popupFactory, repository, directory);
      cellBuilder.add("accountChartButton", accountButton.getComponent());
      cellBuilder.addDisposable(accountButton);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .forceSelection(account.getKey());
      cellBuilder.add("accountPositionLabel", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);

      final AccountPositionsChartView accountChart =
        AccountPositionsChartView.full(account.get(Account.ID), "accountHistoChart", currentRange, repository, directory);
      storeChart(cellBuilder, accountChart);
      final SplitsNode<HistoChart> chartNode = accountChart.registerComponents(cellBuilder);
      accountChart.update();

      BooleanFieldListener showHide =
        BooleanFieldListener.installNodeStyle(Key.create(ProjectAccountGraph.TYPE, account.get(Account.ID)), ProjectAccountGraph.SHOW,
                                              chartNode, "accountChartShown", "accountChartHidden", repository);
      cellBuilder.addDisposable(showHide);
    }

    public void registerSummaryAccountComponents(PanelBuilder cellBuilder, Glob account) {

      JToggleButton toggle = new JToggleButton();
      toggle.setVisible(false);
      toggle.setEnabled(false);
      cellBuilder.add("toggleGraph", toggle);

      JButton hidden = new JButton(Lang.get(Account.isMain(account) ? "account.summary.main" : "account.summary.savings"));
      hidden.setVisible(false);
      hidden.setEnabled(false);
      cellBuilder.add("accountChartButton", hidden);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .forceSelection(account.getKey());
      cellBuilder.add("accountPositionLabel", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);

      final MainDailyPositionsChartView chartView =
        new MainDailyPositionsChartView(currentRange,
                                        AccountPositionsChartView.FULL_CONFIG,
                                        "accountHistoChart", repository, directory, "daily.budgetSummary");
      storeChart(cellBuilder, chartView);
      chartView.setAccount(Account.isMain(account) ? AccountMatchers.userCreatedMainAccounts() : AccountMatchers.userCreatedSavingsAccounts());
      SplitsNode<HistoChart> node = chartView.registerComponents(cellBuilder);
      node.applyStyle("accountChartShown");
      cellBuilder.addDisposable(chartView);
    }
  }

  public void storeChart(PanelBuilder cellBuilder, final HistoChartAdjustableRange accountChart) {
    charts.add(accountChart);
    cellBuilder.addDisposable(new Disposable() {
      public void dispose() {
        charts.remove(accountChart);
      }
    });
  }
}
