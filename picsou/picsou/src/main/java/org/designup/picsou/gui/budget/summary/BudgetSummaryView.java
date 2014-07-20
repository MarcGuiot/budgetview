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

import static org.designup.picsou.gui.utils.Matchers.userCreatedMainAccounts;
import static org.designup.picsou.model.Account.activeUserCreatedMainAccounts;

public class BudgetSummaryView
  extends View
  implements GlobSelectionListener, ChangeSetListener {

  private JLabel multiSelectionLabel = new JLabel();
  private MainDailyPositionsChartView chartView;
  private Map<Integer, SplitsNode> statusNodes = new HashMap<Integer, SplitsNode>();
  private Map<Integer, SplitsNode> buttonNodes = new HashMap<Integer, SplitsNode>();
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
    updateStatusNodes();
  }

  public void selectionUpdated(GlobSelection selection) {
    updateMonthMessage();
    updateAccountSelection();
    updateStatusNodes();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    boolean changed = false;
    if (changeSet.containsChanges(Month.TYPE)) {
      updateMonthMessage();
      changed = true;
    }
    if (changed || changeSet.containsChanges(PeriodAccountStat.TYPE)) {
      updateAccountSelection();
      updateStatusNodes();
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
      updateStatusNodes();
    }
  }

  private class AccountSelectorRepeat implements RepeatComponentFactory<Glob> {
    public void registerComponents(PanelBuilder cellBuilder, final Glob accountStat) {
      final Integer accountId = accountStat.get(PeriodAccountStat.ACCOUNT);
      Key accountKey = Key.create(Account.TYPE, accountId);
      final SplitsNode<JButton> statusNode = cellBuilder.add("accountStatus", new JButton(new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          selectAccount(accountId);
        }
      }));
      statusNodes.put(accountId, statusNode);
      updateStatusNode(currentAccountId, accountId, statusNode);

      Glob account = repository.get(accountKey);
      final AccountPopupFactory popupFactory = new AccountPopupFactory(account, repository, directory);
      popupFactory.setShowGraphToggle(false);
      popups.put(accountId, popupFactory);
      GlobButtonView accountButton = AccountViewPanel.createEditAccountButton(account, popupFactory, repository, directory);
      SplitsNode<JButton> accountButtonNode = cellBuilder.add("accountButton", accountButton.getComponent());
      cellBuilder.addDisposable(accountButton);
      buttonNodes.put(accountId, accountButtonNode);
      updateButtonNode(currentAccountId, accountId, accountButtonNode);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .setUpdateMatcher(ChangeSetMatchers.changesForKey(UserPreferences.KEY))
        .forceSelection(accountKey);
      cellBuilder.add("accountPosition", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);

      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          buttonNodes.remove(accountId);
          statusNodes.remove(accountId);
          popups.remove(accountId);
        }
      });
    }
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

  private void selectAccount(Integer newAccountId) {
    currentAccountId = newAccountId;
    if (newAccountId == null) {
      chartView.clearAccount();
      selectionService.clear(Account.TYPE);
    }
    else {
      Key newAccountKey = Key.create(Account.TYPE, newAccountId);
      if (repository.contains(newAccountKey)) {
        chartView.setAccount(newAccountKey);
        selectionService.select(repository.find(newAccountKey));
      }
      else {
        chartView.setAccount(userCreatedMainAccounts());
        selectionService.clear(Account.TYPE);
      }
    }
    updateStatusNodes();
  }

  private void updateStatusNodes() {
    SortedSet<Integer> monthIds = selectionService.getSelection(Month.TYPE).getSortedSet(Month.ID);
    GlobList accounts = repository.getAll(Account.TYPE, activeUserCreatedMainAccounts(monthIds));
    boolean showMultiAccounts = accounts.size() > 1;

    Integer newAccountId = currentAccountId;

    for (Map.Entry<Integer, SplitsNode> entry : buttonNodes.entrySet()) {
      Integer accountId = entry.getKey();
      SplitsNode node = entry.getValue();
      updateButtonNode(newAccountId, accountId, node);
    }
    for (Map.Entry<Integer, SplitsNode> entry : statusNodes.entrySet()) {
      Integer accountId = entry.getKey();
      SplitsNode node = entry.getValue();
      updateStatusNode(newAccountId, accountId, node);
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

  private void updateButtonNode(Integer currentSelectedId, Integer accountId, SplitsNode node) {
    if (Utils.equal(accountId, currentSelectedId)) {
      node.applyStyle("selectedAccount");
    }
    else {
      node.applyStyle("unselectedAccount");
    }
  }

  private void updateStatusNode(Integer selectedAccountId, Integer accountId, SplitsNode node) {
    JComponent component = (JComponent)node.getComponent();
    Glob stat = repository.find(Key.create(PeriodAccountStat.TYPE, accountId));
    boolean isOk = stat != null && stat.isTrue(PeriodAccountStat.OK);
    if (isOk) {
      if (Utils.equal(accountId, selectedAccountId)) {
        node.applyStyle("account:OK:selected");
        component.setToolTipText(Lang.get("budgetSummaryView.account.status.selected.tooltip"));
      }
      else {
        node.applyStyle("account:OK:unselected");
        component.setToolTipText(Lang.get("budgetSummaryView.account.status.unselected.tooltip"));
      }
    }
    else {
      if (Utils.equal(accountId, selectedAccountId)) {
        node.applyStyle("account:NOK:selected");
        component.setToolTipText(Lang.get("budgetSummaryView.account.status.selected.tooltip"));
      }
      else {
        node.applyStyle("account:NOK:unselected");
        component.setToolTipText(Lang.get("budgetSummaryView.account.status.unselected.tooltip"));
      }
    }
  }
}

