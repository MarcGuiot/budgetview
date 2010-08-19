package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.accounts.position.SavingsAccountPositionLabels;
import org.designup.picsou.gui.budget.SeriesEditionButtons;
import org.designup.picsou.gui.projects.NextProjectsView;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

public class SavingsView extends View implements GlobSelectionListener {
  private Matchers.AccountDateMatcher accountDateMatcher;
  private GlobRepeat repeat;

  public SavingsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    selectionService.addListener(this, Month.TYPE);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/savings/savingsView.splits",
                                                      repository, directory);

    createNextProjectsBlock(builder);
    createSavingsBlock(builder);

    parentBuilder.add("savingsView", builder);
  }

  private void createNextProjectsBlock(GlobsPanelBuilder builder) {

    NextProjectsView nextProjectsView = new NextProjectsView(repository, directory);
    nextProjectsView.registerComponents(builder);

    SavingsChartView savingsChartView = new SavingsChartView(repository, directory);
    savingsChartView.registerComponents(builder);

    SeriesEditionButtons seriesButtons = new SeriesEditionButtons(BudgetArea.EXTRAS,
                                                                  repository, directory,
                                                                  directory.get(SeriesEditionDialog.class));
    seriesButtons.setNames("createProject", "editAllProjects");
    seriesButtons.registerButtons(builder);
  }

  private void createSavingsBlock(GlobsPanelBuilder builder) {

    SeriesEditionButtons seriesButtons = new SeriesEditionButtons(BudgetArea.SAVINGS, repository, directory,
                                                                  directory.get(SeriesEditionDialog.class));
    seriesButtons.setNames("createSavingsSeries", "editAllSavingsSeries");
    seriesButtons.registerButtons(builder);

    Key accountKey = Key.create(Account.TYPE, Account.SAVINGS_SUMMARY_ACCOUNT_ID);
    AccountPositionLabels positionLabels = new SavingsAccountPositionLabels(accountKey, repository, directory);
    builder.add("totalSavingsPositionAmount",
                positionLabels.getEstimatedAccountPositionLabel(true));
    builder.add("totalSavingsPositionDate",
                positionLabels.getEstimatedAccountPositionDateLabel());

    repeat = builder.addRepeat("savingsAccounts", Account.TYPE,
                      getFilter(),
                      new SavingsAccountsComponentFactory(seriesButtons));
  }

  private GlobMatcher getFilter() {
    return GlobMatchers.and(new AccountMatcher(),
                            accountDateMatcher,
                            GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID,
                                                                      Account.MAIN_SUMMARY_ACCOUNT_ID)));
  }

  public void selectionUpdated(GlobSelection selection) {
    GlobList selectedMonth = selection.getAll(Month.TYPE);
    accountDateMatcher = new Matchers.AccountDateMatcher(selectedMonth);
    repeat.setFilter(getFilter());
  }

  private class AccountMatcher implements GlobMatcher {
    public boolean matches(Glob account, GlobRepository repository) {
      return Account.isUserCreatedSavingsAccount(account);
    }
  }

  private class SavingsAccountsComponentFactory implements RepeatComponentFactory<Glob> {
    private SeriesEditionButtons seriesButtons;

    public SavingsAccountsComponentFactory(SeriesEditionButtons seriesButtons) {
      this.seriesButtons = seriesButtons;
    }

    public void registerComponents(RepeatCellBuilder cellBuilder, Glob account) {

      cellBuilder.add("accountName",
                      GlobLabelView.init(Account.NAME, repository, directory)
                        .forceSelection(account.getKey()).getComponent());

      Key accountKey = account.getKey();
      AccountPositionLabels positionLabels = new SavingsAccountPositionLabels(accountKey, repository, directory);
      cellBuilder.add("estimatedAccountPosition",
                      positionLabels.getEstimatedAccountPositionLabel(false));
      cellBuilder.add("estimatedAccountPositionDate",
                      positionLabels.getEstimatedAccountPositionDateLabel());

      final SavingsSeriesView seriesView = new SavingsSeriesView(account, repository, directory,
                                                                 directory.get(SeriesAmountEditionDialog.class),
                                                                 seriesButtons);
      cellBuilder.add("savingsSeries", seriesView.getPanel());
      cellBuilder.addDisposeListener(seriesView);
    }

  }
}
