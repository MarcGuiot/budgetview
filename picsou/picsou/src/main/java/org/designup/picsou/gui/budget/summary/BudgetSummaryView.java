package org.designup.picsou.gui.budget.summary;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.AccountViewPanel;
import org.designup.picsou.gui.accounts.actions.AccountPopupFactory;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.utils.AccountPositionStringifier;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.model.PeriodAccountStat;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.*;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import static org.designup.picsou.model.Account.activeUserCreatedMainAccounts;

public class BudgetSummaryView
  extends View
  implements GlobSelectionListener, ChangeSetListener {

  private JLabel multiSelectionLabel = new JLabel();
  private MainDailyPositionsChartView chartView;
  private Map<Integer, SplitsNode> selectors = new HashMap<Integer, SplitsNode>();
  private Map<Integer, SplitsNode> buttons = new HashMap<Integer, SplitsNode>();
  private Map<Integer, AccountPopupFactory> popups = new HashMap<Integer, AccountPopupFactory>();
  private Integer currentAccountId;

  public BudgetSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/budget/budgetSummaryView.splits",
                                                      repository, directory);

    builder.addRepeat("accountSelectors",
                      PeriodAccountStat.TYPE,
                      GlobMatchers.ALL,
                      GlobComparators.ascending(PeriodAccountStat.SEQUENCE),
                      new AccountSelectorRepeat());

    builder.add("multiSelectionLabel", multiSelectionLabel);

    chartView = new MainDailyPositionsChartView(new ScrollableHistoChartRange(0, 1, true, repository),
                                                new HistoChartConfig(true, false, true, true, true, true, false, true, false, true),
                                                "chart", repository, directory, "daily.budgetSummary");
    chartView.installHighlighting();
    chartView.setShowFullMonthLabels(true);
    chartView.registerComponents(builder);

    parentBuilder.add("budgetSummaryView", builder);

    repository.addChangeListener(this);
    selectionService.addListener(this, Month.TYPE);

    updateMonthMessage();
    updateAccountSelection();
    updateAccountSelectors();
  }

  public void selectionUpdated(GlobSelection selection) {
    updateMonthMessage();
    updateAccountSelection();
    updateAccountSelectors();
  }

  public void updateMonthMessage() {
    SortedSet<Integer> selectedMonthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    if (selectedMonthIds.size() > 1) {
      multiSelectionLabel.setText(Lang.get("budgetSummaryView.multimonth", selectedMonthIds.size()));
      multiSelectionLabel.setVisible(true);
    }
    else {
      multiSelectionLabel.setVisible(false);
    }
  }

  public void updateAccountSelection() {
    SortedSet<Integer> monthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    GlobMatcher matcher = activeUserCreatedMainAccounts(monthIds);
    if ((currentAccountId == null)
        || !repository.contains(Key.create(Account.TYPE, currentAccountId))
        || !matcher.matches(repository.get(Key.create(Account.TYPE, currentAccountId)), repository)) {
      GlobList accounts = repository.getAll(Account.TYPE, matcher).sort(new AccountComparator());
      selectAccount(accounts.isEmpty() ? null : accounts.getFirst().get(Account.ID));
    }
  }

  private void updateAccountSelectors() {
    SortedSet<Integer> monthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    GlobList accounts = repository.getAll(Account.TYPE, activeUserCreatedMainAccounts(monthIds));
    boolean showMultiAccounts = accounts.size() > 1;
    for (SplitsNode node : selectors.values()) {
      node.getComponent().setVisible(showMultiAccounts);
    }
    for (final Map.Entry<Integer, AccountPopupFactory> entry : popups.entrySet()) {
      Action selectAccountAction = null;
      if (showMultiAccounts) {
        selectAccountAction = new AbstractAction(Lang.get("budgetSummaryView.showGraphForAccount")) {
          public void actionPerformed(ActionEvent e) {
            selectAccount(entry.getKey());
          }
        };
      }
      entry.getValue().setFirstAction(selectAccountAction);
    }
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    boolean changed = false;
    if (changeSet.containsChanges(Month.TYPE)) {
      updateMonthMessage();
      changed = true;
    }
    if (changeSet.containsChanges(Account.TYPE)) {
      updateAccountSelection();
      changed = true;
    }
    if (changed) {
      updateAccountSelectors();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    boolean changed = false;
    if (changedTypes.contains(Month.TYPE)) {
      updateMonthMessage();
      changed = true;
    }
    if (changedTypes.contains(Account.TYPE)) {
      currentAccountId = null;
      updateAccountSelection();
      changed = true;
    }
    if (changed) {
      updateAccountSelectors();
    }
  }

  private void selectAccount(Integer newAccountId) {
    currentAccountId = newAccountId;
    if (newAccountId == null) {
      chartView.clearAccount();
    }
    else {
      Key newAccountKey = Key.create(Account.TYPE, newAccountId);
      if (repository.contains(newAccountKey)) {
        chartView.setAccount(repository.get(newAccountKey));
      }
      else {
        chartView.setAccount(repository.get(Account.MAIN_SUMMARY_KEY));
      }
    }
    for (Map.Entry<Integer, SplitsNode> entry : buttons.entrySet()) {
      Integer accountId = entry.getKey();
      SplitsNode node = entry.getValue();
      updateButtonNode(newAccountId, accountId, node);
    }
    for (Map.Entry<Integer, SplitsNode> entry : selectors.entrySet()) {
      Integer accountId = entry.getKey();
      SplitsNode node = entry.getValue();
      updateSelectorNode(newAccountId, accountId, node);
    }
  }

  private class AccountSelectorRepeat implements RepeatComponentFactory<Glob> {
    public void registerComponents(PanelBuilder cellBuilder, final Glob accountStat) {
      final Integer accountId = accountStat.get(PeriodAccountStat.ACCOUNT);
      Key accountKey = Key.create(Account.TYPE, accountId);
      final SplitsNode<JLabel> statusNode = cellBuilder.add("accountStatus", new JLabel());
      final Key accountStatKey = accountStat.getKey();
      final KeyChangeListener statusUpdater = new KeyChangeListener(accountStatKey) {
        public void update() {
          Glob stat = repository.find(accountStatKey);
          boolean isOk = (stat == null) || stat.get(PeriodAccountStat.OK, true);
          statusNode.applyStyle(isOk ? "accountOK" : "accountNOK");
          statusNode.getComponent().setToolTipText(Lang.get(isOk ? "budgetSummaryView.status.ok.tooltip" : "budgetSummaryView.status.nok.tooltip"));
        }
      };
      repository.addChangeListener(statusUpdater);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          repository.removeChangeListener(statusUpdater);
        }
      });
      statusUpdater.update();

      Glob account = repository.get(accountKey);
      final AccountPopupFactory popupFactory = new AccountPopupFactory(account, repository, directory);
      popups.put(accountId, popupFactory);
      GlobButtonView accountButton = AccountViewPanel.createEditAccountButton(account, popupFactory, repository, directory);
      SplitsNode<JButton> accountButtonNode = cellBuilder.add("accountButton", accountButton.getComponent());
      cellBuilder.addDisposable(accountButton);
      buttons.put(accountId, accountButtonNode);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          buttons.remove(accountId);
        }
      });
      updateButtonNode(currentAccountId, accountId, accountButtonNode);

      JButton selector = new JButton(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          selectAccount(accountId);
        }
      });
      SplitsNode<JButton> selectorNode = cellBuilder.add("accountSelector", selector);
      selectors.put(accountId, selectorNode);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          selectors.remove(accountId);
        }
      });
      updateSelectorNode(currentAccountId, accountId, selectorNode);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .setUpdateMatcher(ChangeSetMatchers.changesForKey(UserPreferences.KEY))
        .forceSelection(accountKey);
      cellBuilder.add("accountPosition", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);

      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          buttons.remove(accountId);
          selectors.remove(accountId);
          popups.remove(accountId);
        }
      });
    }
  }

  private void updateButtonNode(Integer currentSelectedId, Integer accountId, SplitsNode node) {
    if (Utils.equal(accountId, currentSelectedId)) {
      node.applyStyle("selectedAccount");
    }
    else {
      node.applyStyle("unselectedAccount");
    }
  }

  private void updateSelectorNode(Integer currentSelectedId, Integer accountId, SplitsNode node) {
    JComponent component = (JComponent)node.getComponent();
    if (Utils.equal(accountId, currentSelectedId)) {
      node.applyStyle("checkedSelector");
      component.setToolTipText(Lang.get("budgetSummaryView.selector.selected.tooltip"));
    }
    else {
      node.applyStyle("uncheckedSelector");
      component.setToolTipText(Lang.get("budgetSummaryView.selector.unselected.tooltip"));
    }
  }
}

