package org.designup.picsou.gui.accounts;

import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.accounts.utils.GotoAccountWebsiteAction;
import org.designup.picsou.gui.accounts.utils.GotoAccountOperationsAction;
import org.designup.picsou.gui.description.AccountComparator;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.SavingsBudgetStat;
import org.designup.picsou.gui.monthsummary.AccountPositionThresholdAction;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.AbstractGlobTextView;
import org.globsframework.gui.views.GlobButtonView;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.format.GlobListStringifier;
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

    registerReferencePositionLabels(builder, summaryId,
                                      "referencePosition",
                                      "referencePositionDate",
                                      "accountView.total.date");

    Key summaryAccount = Key.create(Account.TYPE, summaryId);
    AccountPositionLabels positionLabels = createPositionLabels(summaryAccount);
    builder.add("estimatedPosition",
                positionLabels.getEstimatedAccountPositionLabel(true));
    builder.add("estimatedPositionDate",
                positionLabels.getEstimatedAccountPositionDateLabel());

    JLabel labelTypeName = new JLabel();
    builder.add("labelTypeName", labelTypeName);

    accountRepeat = builder.addRepeat("accountRepeat", Account.TYPE, accountTypeMatcher,
                                      new AccountComparator(),
                                      new AccountRepeatFactory());

    builder.add("createAccount", new NewAccountAction(getAccountType(), repository, directory));

    panel = builder.load();
  }

  public static void registerReferencePositionLabels(GlobsPanelBuilder builder, 
                                                    Integer summaryId,
                                                    String positionLabelName,
                                                    String titleLabelName,
                                                    String titleLabelKey) {
    Key summaryAccount = Key.create(Account.TYPE, summaryId);
    builder.addLabel(positionLabelName, Account.POSITION)
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);
    builder.addLabel(titleLabelName, Account.TYPE, new ReferenceAmountStringifier(titleLabelKey))
      .setAutoHideIfEmpty(true)
      .forceSelection(summaryAccount);
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
      add("accountName",
          createAccountNameButton(account, repository, directory), account, cellBuilder);

      cellBuilder.add("gotoWebsite", new GotoAccountWebsiteAction(account, repository, directory));

      cellBuilder.add("gotoOperations", new GotoAccountOperationsAction(account, repository, directory));

      add("accountUpdateDate",
          GlobLabelView.init(Account.POSITION_DATE, repository, directory),
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
                            }).forceSelection(account.getKey());
      cellBuilder.add("accountPosition", balance.getComponent());

      AccountPositionLabels positionLabels = createPositionLabels(account.getKey());
      cellBuilder.add("estimatedAccountPosition", positionLabels.getEstimatedAccountPositionLabel(false));
      cellBuilder.add("estimatedAccountPositionDate", positionLabels.getEstimatedAccountPositionDateLabel());

      cellBuilder.addDisposeListener(balance);
    }

    private void add(String name, final AbstractGlobTextView labelView, Glob account, RepeatCellBuilder cellBuilder) {
      labelView.forceSelection(account.getKey());
      cellBuilder.add(name, labelView.getComponent());
      cellBuilder.addDisposeListener(labelView);
    }
  }

  protected abstract AccountPositionLabels createPositionLabels(Key accountKey);

  private static class EditAccountFunctor implements GlobListFunctor {

    private Directory directory;

    private EditAccountFunctor(Directory directory) {
      this.directory = directory;
    }

    public void run(GlobList list, GlobRepository repository) {
      AccountEditionDialog dialog = new AccountEditionDialog(repository, directory);
      dialog.show(list.get(0));
    }
  }

  private static class ReferenceAmountStringifier implements GlobListStringifier {
    private String key;

    private ReferenceAmountStringifier(String key) {
      this.key = key;
    }

    public String toString(GlobList list, GlobRepository repository) {
      if (list.isEmpty() || list.get(0).get(Account.POSITION_DATE) == null) {
        return "";
      }
      return Lang.get(key, Formatting.toString(list.get(0).get(Account.POSITION_DATE)));
    }
  }

  public static GlobButtonView createAccountNameButton(Glob account, final GlobRepository repository, final Directory directory) {
    return GlobButtonView.init(Account.NAME, repository, directory,
                               new EditAccountFunctor(directory)).forceSelection(account.getKey());
  }
}