package org.designup.picsou.gui.accounts;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.accounts.actions.AccountPopupFactory;
import org.designup.picsou.gui.accounts.chart.MainDailyPositionsChartView;
import org.designup.picsou.gui.accounts.components.AccountStatusButton;
import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.budget.summary.UncategorizedButton;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.PopupGlobFunctor;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.UserPreferences;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobSelectablePanel;
import org.globsframework.gui.components.GlobSelectionToggle;
import org.globsframework.gui.components.GlobUnselectPanel;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.gui.utils.GlobBooleanVisibilityUpdater;
import org.globsframework.gui.views.AbstractGlobTextView;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.util.Set;

public abstract class AccountViewPanel {
  protected GlobRepository repository;
  protected Directory directory;
  private AccountType accountType;
  private GlobMatcher accountTypeMatcher;
  private GlobMatcher filterMatcherWithDates;
  private Integer summaryId;
  private JPanel panel;
  private JPanel header;
  private GlobRepeat accountRepeat;

  public AccountViewPanel(final GlobRepository repository, final Directory directory,
                          GlobMatcher accountMatcher, AccountType accountType, Integer summaryId) {
    this.repository = repository;
    this.directory = directory;
    this.accountTypeMatcher = accountMatcher;
    this.accountType = accountType;
    this.summaryId = summaryId;

    directory.get(SelectionService.class).addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        GlobList months = selection.getAll(Month.TYPE);
        filterMatcherWithDates =
          GlobMatchers.and(accountTypeMatcher,
                           new Matchers.AccountDateMatcher(months));
        accountRepeat.setFilter(filterMatcherWithDates);
      }
    }, Month.TYPE);

    repository.addChangeListener(new TypeChangeSetListener(SavingsBudgetStat.TYPE) {
      public void update(GlobRepository repository) {
        updateEstimatedPosition();
      }
    });
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/accounts/accountViewPanel.splits", repository, directory);

    header = builder.add("header", new JPanel()).getComponent();

    AccountPositionLabels.registerReferencePositionLabels(builder, summaryId,
                                                          "referencePosition",
                                                          "referencePositionDate",
                                                          "accountView.total.date");

    JLabel labelTypeName = new JLabel(Lang.get(accountType == AccountType.MAIN ? "accountView.title.main" : "accountView.title.savings"));
    builder.add("accountListTitle", labelTypeName);

    builder.add("unselect", GlobUnselectPanel.init(Account.TYPE, directory).getComponent());

    accountRepeat = builder.addRepeat("accountRepeat", Account.TYPE, accountTypeMatcher,
                                      new AccountComparator(),
                                      new AccountRepeatFactory());

    panel = builder.load();
  }

  protected abstract AccountType getAccountType();

  public JPanel getPanel() {
    if (panel == null) {
      createPanel();
    }
    return panel;
  }

  protected void updateEstimatedPosition() {
    boolean hasAccounts = !repository.getAll(Account.TYPE, accountTypeMatcher).isEmpty();
    header.setVisible(hasAccounts);
  }

  private class AccountRepeatFactory implements RepeatComponentFactory<Glob> {
    public void registerComponents(PanelBuilder cellBuilder, final Glob account) {

      SplitsNode<JPanel> accountPanel = cellBuilder.add("accountPanel", new JPanel());
      final GlobSelectablePanel selectablePanel =
        new GlobSelectablePanel(accountPanel,
                                "selectedPanel", "unselectedPanel",
                                "selectedRolloverPanel", "unselectedRolloverPanel",
                                repository, directory, account.getKey());
      cellBuilder.addDisposable(selectablePanel);

      AccountStatusButton.create(account.getKey(), cellBuilder, "accountStatus", repository, directory);

      UncategorizedButton.create(account.getKey(), cellBuilder, "uncategorized", repository, directory);

      final AccountPopupFactory popupFactory = new AccountPopupFactory(account, repository, directory);
      popupFactory.setShowGraphToggle(true);
      add("editAccount",
          createEditAccountButton(account, popupFactory, repository, directory), account, cellBuilder);

      add("showAccount",
          createShowAccountButton(repository, directory), account, cellBuilder);

      MainDailyPositionsChartView chartView =
        new MainDailyPositionsChartView(new ScrollableHistoChartRange(0, 1, true, repository),
                                        new HistoChartConfig(true, false, true, false, false, true, false, true, false, true),
                                        "accountPositionsChart", repository, directory, "daily.budgetSummary") {
          protected void processClick(HistoSelection selection, Set<Key> objectKeys, NavigationService navigationService) {
            selectionService.select(account);
          }
        };
      chartView.setAccount(account.getKey());
      chartView.installHighlighting();
      chartView.setShowFullMonthLabels(true);
      chartView.registerComponents(cellBuilder);
      cellBuilder.addDisposable(chartView);

      GlobBooleanVisibilityUpdater chartUpdater =
        GlobBooleanVisibilityUpdater.init(account.getKey(), Account.SHOW_GRAPH, chartView.getChart(), repository);
      cellBuilder.addDisposable(chartUpdater);

      GlobSelectionToggle selectionToggle = new GlobSelectionToggle(account.getKey(), repository, directory);
      cellBuilder.add("selectAccount", selectionToggle.getComponent());
      cellBuilder.addDisposable(selectionToggle);

      add("accountUpdateDate",
          GlobLabelView.init(Account.POSITION_DATE, repository, directory)
            .setUpdateMatcher(ChangeSetMatchers.changesForKey(UserPreferences.KEY)),
          account, cellBuilder);

      final GlobButtonView balance =
        GlobButtonView.init(Account.TYPE, repository, directory,
                            new GlobListStringifier() {
                              public String toString(GlobList list, GlobRepository repository) {
                                if (list.isEmpty()) {
                                  return "";
                                }
                                Double position = list.get(0).get(Account.POSITION_WITH_PENDING);
                                if (position == null) {
                                  return Lang.get("accountView.missing.position");
                                }
                                return Formatting.DECIMAL_FORMAT.format(position);
                              }
                            },
                            new GlobListFunctor() {
                              public void run(GlobList list, GlobRepository repository) {
                                popupFactory.editPosition();
                              }
                            }
        ).forceSelection(account.getKey());
      cellBuilder.add("accountPosition", balance.getComponent());

      cellBuilder.addDisposable(balance);
    }

    private void add(String name, final AbstractGlobTextView labelView, Glob account, PanelBuilder cellBuilder) {
      labelView.forceSelection(account.getKey());
      cellBuilder.add(name, labelView.getComponent());
      cellBuilder.addDisposable(labelView);
    }
  }

  public static GlobButtonView createShowAccountButton(final GlobRepository repository,
                                                       final Directory directory) {
    return GlobButtonView.init(Account.NAME, repository, directory,
                               new GlobListFunctor() {
                                 public void run(GlobList accounts, GlobRepository repository) {
                                   if (accounts.isEmpty()) {
                                     return;
                                   }
                                   Key accountKey = accounts.getFirst().getKey();
                                   NavigationService navigationService = directory.get(NavigationService.class);
                                   navigationService.gotoDataForAccount(accountKey);
                                 }
                               });
  }

  public static GlobButtonView createEditAccountButton(Glob account, AccountPopupFactory popupFactory,
                                                       final GlobRepository repository, final Directory directory) {
    PopupGlobFunctor functor = new PopupGlobFunctor(popupFactory);
    GlobButtonView buttonView = GlobButtonView.init(Account.NAME, repository, directory, functor);
    buttonView.forceSelection(account.getKey());
    functor.setComponent(buttonView.getComponent());
    return buttonView;
  }
}