package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.accounts.utils.GotoAccountWebsiteAction;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.description.stringifiers.AccountComparator;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SavingsBudgetStat;
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
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.utils.GlobSelectablePanel;
import org.globsframework.gui.utils.GlobSelectionToggle;
import org.globsframework.gui.utils.GlobUnselectPanel;
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
          createEditAccountButton(repository, directory), account, cellBuilder);

      add("showAccount",
          createShowAccountButton(repository, directory), account, cellBuilder);

      GlobSelectionToggle selectionToggle = new GlobSelectionToggle(account.getKey(), repository, directory);
      cellBuilder.add("selectAccount", selectionToggle.getComponent());
      cellBuilder.addDisposeListener(selectionToggle);

      cellBuilder.add("gotoWebsite", new GotoAccountWebsiteAction(account, repository, directory));

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
                                Double position = list.get(0).get(Account.POSITION);
                                if (position == null) {
                                  return Lang.get("accountView.missing.position");
                                }
                                return Formatting.DECIMAL_FORMAT.format(position);
                              }
                            },
                            new GlobListFunctor() {
                              public void run(GlobList list, GlobRepository repository) {
                                AccountPositionEditionDialog accountPositionEditor =
                                  new AccountPositionEditionDialog(account, false, repository, directory, directory.get(JFrame.class));
                                accountPositionEditor.show();
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

  public static GlobButtonView createEditAccountButton(final GlobRepository repository,
                                                       final Directory directory) {
    return GlobButtonView.init(Account.NAME, repository, directory,
                               new GlobListFunctor() {
                                 public void run(GlobList accounts, GlobRepository repository) {
                                   AccountEditionDialog dialog = new AccountEditionDialog(repository, directory);
                                   dialog.show(accounts.get(0));
                                 }
                               });
  }
}