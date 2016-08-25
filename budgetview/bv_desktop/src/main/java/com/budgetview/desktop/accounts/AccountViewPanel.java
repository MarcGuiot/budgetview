package com.budgetview.desktop.accounts;

import com.budgetview.desktop.accounts.actions.AccountPopupFactory;
import com.budgetview.desktop.accounts.chart.MainDailyPositionsChartView;
import com.budgetview.desktop.accounts.components.AccountWeatherButton;
import com.budgetview.desktop.accounts.position.AccountPositionLabels;
import com.budgetview.desktop.addons.AddOnListener;
import com.budgetview.desktop.analysis.histobuilders.range.ScrollableHistoChartRange;
import com.budgetview.desktop.card.NavigationService;
import com.budgetview.desktop.components.PopupGlobFunctor;
import com.budgetview.desktop.components.charts.histo.HistoSelection;
import com.budgetview.desktop.description.Formatting;
import com.budgetview.desktop.description.stringifiers.AccountComparator;
import com.budgetview.desktop.model.SavingsBudgetStat;
import com.budgetview.desktop.transactions.utils.TransactionMatchers;
import com.budgetview.model.*;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.model.AccountType;
import com.budgetview.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.gui.components.GlobSelectablePanel;
import org.globsframework.gui.components.GlobSelectionToggle;
import org.globsframework.gui.components.GlobUnselectPanel;
import org.globsframework.gui.editors.GlobToggleEditor;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobBooleanVisibilityUpdater;
import org.globsframework.gui.views.AbstractGlobTextView;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
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
  protected JPanel header;
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
                           new TransactionMatchers.AccountDateMatcher(months));
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
      selectablePanel.setMultiSelectionEnabled(false);
      cellBuilder.addDisposable(selectablePanel);

      AccountWeatherButton.create(account.getKey(), cellBuilder, "accountWeather", repository, directory);

      final AccountPopupFactory popupFactory = new AccountPopupFactory(account, repository, directory, false);
      popupFactory.setShowSelectionToggle(true);
      popupFactory.setShowGraphToggle(true);
      add("editAccount",
          createEditAccountButton(account, popupFactory, repository, directory), account, cellBuilder);

      add("showAccount",
          createShowAccountButton(repository, directory), account, cellBuilder);

      cellBuilder.add("toggleGraph", GlobToggleEditor.init(Account.SHOW_CHART, repository, directory)
        .forceSelection(account.getKey())
        .getComponent());

      final MainDailyPositionsChartView chartView =
        new MainDailyPositionsChartView(createRange(false),
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
      cellBuilder.addDisposable(AddOnListener.install(repository, AddOns.EXTRA_RANGE, new AddOnListener() {
        public void processAddOn(boolean enabled) {
          chartView.setRange(createRange(enabled));
        }
      }));

      GlobBooleanVisibilityUpdater chartUpdater =
        GlobBooleanVisibilityUpdater.init(account.getKey(), Account.SHOW_CHART, chartView.getChart(), repository);
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

  public ScrollableHistoChartRange createRange(boolean extraRangeEnabled) {
    return new ScrollableHistoChartRange(0, extraRangeEnabled ? 3 :  1, true, repository);
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