package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.accounts.actions.CreateAccountAction;
import org.designup.picsou.gui.accounts.actions.DeleteAccountAction;
import org.designup.picsou.gui.accounts.chart.AccountPositionsChartView;
import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.accounts.utils.GotoAccountWebsiteAction;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.PopupMouseAdapter;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.series.analysis.histobuilders.range.SelectionHistoChartRange;
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
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.*;
import org.globsframework.gui.views.AbstractGlobTextView;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.model.utils.ChangeSetMatchers;
import org.globsframework.model.utils.GlobListFunctor;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Set;

public abstract class AccountViewPanel {
  protected GlobRepository repository;
  protected Directory directory;
  private GlobMatcher accountTypeMatcher;
  private GlobMatcher filterMatcherWithDates;
  private Integer summaryId;
  private JPanel panel;
  private JPanel header;
  private GlobRepeat accountRepeat;

  public AccountViewPanel(final GlobRepository repository, final Directory directory,
                          GlobMatcher accountMatcher, Integer summaryId) {
    this.repository = repository;
    this.directory = directory;
    this.accountTypeMatcher = accountMatcher;
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

    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        if (changeSet.containsChanges(SavingsBudgetStat.TYPE)) {
          updateEstimatedPosition();
        }
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        if (changedTypes.contains(SavingsBudgetStat.TYPE)) {
          updateEstimatedPosition();
        }
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

    JLabel labelTypeName = new JLabel();
    builder.add("labelTypeName", labelTypeName);

    builder.add("unselect", GlobUnselectPanel.init(Account.TYPE, directory).getComponent());

    accountRepeat = builder.addRepeat("accountRepeat", Account.TYPE, accountTypeMatcher,
                                      new AccountComparator(),
                                      new AccountRepeatFactory());

    builder.add("createAccount", new CreateAccountAction(getAccountType(), repository, directory));

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
    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob account) {

      SplitsNode<JPanel> accountPanel = cellBuilder.add("accountPanel", new JPanel());
      GlobSelectablePanel toggle =
        new GlobSelectablePanel(accountPanel,
                                "selectedPanel", "unselectedPanel",
                                "selectedRolloverPanel", "unselectedRolloverPanel",
                                repository, directory, account.getKey());
      cellBuilder.addDisposeListener(toggle);

      add("editAccount",
          createEditAccountButton(account, repository, directory), account, cellBuilder);

      add("showAccount",
          createShowAccountButton(repository, directory), account, cellBuilder);

      AccountPositionsChartView positionsChart =
        new AccountPositionsChartView(account.get(Account.ID),
                                      "accountPositionsChart",
                                      new SelectionHistoChartRange(repository, directory), repository, directory);
      positionsChart.registerComponents(cellBuilder);
      cellBuilder.addDisposeListener(positionsChart);

      GlobSelectionToggle selectionToggle = new GlobSelectionToggle(account.getKey(), repository, directory);
      cellBuilder.add("selectAccount", selectionToggle.getComponent());
      cellBuilder.addDisposeListener(selectionToggle);

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
                                editPosition(account, repository);
                              }
                            }
        ).forceSelection(account.getKey());
      cellBuilder.add("accountPosition", balance.getComponent());

      cellBuilder.addDisposeListener(balance);
    }

    private void add(String name, final AbstractGlobTextView labelView, Glob account, RepeatCellBuilder cellBuilder) {
      labelView.forceSelection(account.getKey());
      cellBuilder.add(name, labelView.getComponent());
      cellBuilder.addDisposeListener(labelView);
    }
  }

  private void editPosition(Glob account, GlobRepository repository) {
    AccountPositionEditionDialog accountPositionEditor =
      new AccountPositionEditionDialog(account, repository, directory, directory.get(JFrame.class));
    accountPositionEditor.show();
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

  private GlobButtonView createEditAccountButton(Glob account, final GlobRepository repository,
                                                 final Directory directory) {
    GlobButtonView buttonView = GlobButtonView.init(Account.NAME, repository, directory, GlobListFunctor.NO_OP);
    buttonView.forceSelection(account.getKey());
    buttonView.getComponent().addMouseListener(new PopupMouseAdapter(new AccountPopupFactory(account)));
    return buttonView;
  }

  private class AccountPopupFactory implements PopupMenuFactory {
    private final Glob account;

    public AccountPopupFactory(Glob account) {
      this.account = account;
    }

    public JPopupMenu createPopup() {
      JPopupMenu menu = new JPopupMenu();
      menu.add(new AbstractAction(Lang.get("accountView.edit")) {
        public void actionPerformed(ActionEvent e) {
          AccountEditionDialog dialog = new AccountEditionDialog(repository, directory, false);
          dialog.show(account.getKey());
        }
      });
      menu.add(new AbstractAction(Lang.get("accountView.editPosition")) {
        public void actionPerformed(ActionEvent e) {
          editPosition(account, repository);
        }
      });
      menu.addSeparator();
      menu.add(new GotoAccountWebsiteAction(account, repository, directory));
      menu.addSeparator();
      menu.add(new DeleteAccountAction(account, repository, directory));
      return menu;
    }
  }
}