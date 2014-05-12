package org.designup.picsou.gui.budget.summary;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.View;
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
  private Integer currentAccountId;

  public BudgetSummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    repository.addChangeListener(this);
    selectionService.addListener(this, Month.TYPE);
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

    updateMonthMessage();
    updateAccountSelection();
  }

  public void selectionUpdated(GlobSelection selection) {
    updateMonthMessage();
    updateAccountSelection();
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

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(Month.TYPE)) {
      updateMonthMessage();
    }
    if (changeSet.containsChanges(Account.TYPE)) {
      updateAccountSelection();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Month.TYPE)) {
      updateMonthMessage();
    }
    if (changedTypes.contains(Account.TYPE)) {
      currentAccountId = null;
      updateAccountSelection();
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
    for (Map.Entry<Integer, SplitsNode> entry : selectors.entrySet()) {
      Integer accountId = entry.getKey();
      SplitsNode node = entry.getValue();
      updateNode(newAccountId, accountId, node);
    }
  }

  private void updateNode(Integer currentSelectedId, Integer accountId, SplitsNode node) {
    if (Utils.equal(accountId, currentSelectedId)) {
      node.applyStyle("selectedAccount");
    }
    else {
      node.applyStyle("unselectedAccount");
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
        }
      };
      repository.addChangeListener(statusUpdater);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          repository.removeChangeListener(statusUpdater);
        }
      });
      statusUpdater.update();

      GlobButtonView buttonView = GlobButtonView.init(Account.TYPE, repository, directory, new GlobListFunctor() {
        public void run(GlobList list, GlobRepository repository) {
          selectAccount(accountId);
        }
      })
        .forceSelection(accountKey);
      JButton selector = buttonView.getComponent();
      SplitsNode<JButton> selectorNode = cellBuilder.add("accountSelector", selector);
      cellBuilder.addDisposable(buttonView);
      selectors.put(accountId, selectorNode);
      cellBuilder.addDisposable(new Disposable() {
        public void dispose() {
          selectors.remove(accountId);
        }
      });
      updateNode(currentAccountId, accountId, selectorNode);

      GlobLabelView accountPositionLabel = GlobLabelView.init(Account.TYPE, repository, directory, new AccountPositionStringifier())
        .setUpdateMatcher(ChangeSetMatchers.changesForKey(UserPreferences.KEY))
        .forceSelection(accountKey);
      cellBuilder.add("accountPosition", accountPositionLabel.getComponent());
      cellBuilder.addDisposable(accountPositionLabel);
    }
  }
}

