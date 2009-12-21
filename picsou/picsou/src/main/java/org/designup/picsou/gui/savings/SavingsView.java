package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.SavingsAccountViewPanel;
import org.designup.picsou.gui.budget.SeriesEditionButtons;
import org.designup.picsou.gui.projects.NextProjectsView;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.directory.Directory;

public class SavingsView extends View {
  private SeriesEditionDialog seriesEditionDialog;

  public SavingsView(GlobRepository repository, Directory directory) {
    super(repository, directory);
    this.seriesEditionDialog = new SeriesEditionDialog(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/savingsView.splits",
                                                      repository, directory);

    createNextProjectsBlock(builder);
    createSavingsBlock(builder);

    parentBuilder.add("savingsView", builder);
  }

  private void createNextProjectsBlock(GlobsPanelBuilder builder) {

    NextProjectsView nextProjectsView = new NextProjectsView(repository, directory);
    nextProjectsView.registerComponents(builder);

    SeriesEditionButtons seriesButtons = new SeriesEditionButtons(BudgetArea.SPECIAL,
                                                                  repository, directory, seriesEditionDialog);
    seriesButtons.setNames("createProject", "editAllProjects");
    seriesButtons.registerButtons(builder);
  }

  private void createSavingsBlock(GlobsPanelBuilder builder) {

    SeriesEditionButtons seriesButtons = new SeriesEditionButtons(BudgetArea.SAVINGS, repository, directory, seriesEditionDialog);
    seriesButtons.setNames("createSavingsSeries", "editAllSavingsSeries");
    seriesButtons.registerButtons(builder);

    Key accountKey = Key.create(Account.TYPE, Account.SAVINGS_SUMMARY_ACCOUNT_ID);
    builder.add("totalSavingsPositionAmount",
                SavingsAccountViewPanel.getEstimatedAccountPositionLabel(accountKey, repository, directory));
    builder.add("totalSavingsPositionDate",
                SavingsAccountViewPanel.getEstimatedAccountPositionDateLabel(accountKey, repository, directory));

    builder.addRepeat("savingsAccounts", Account.TYPE,
                      GlobMatchers.and(new AccountMatcher(),
                                       GlobMatchers.not(GlobMatchers.fieldEquals(Account.ID,
                                                                                 Account.MAIN_SUMMARY_ACCOUNT_ID))),
                      new SavingsAccountsComponentFactory(seriesButtons));
  }

  private GlobMatcher getNextProjectsMatcher() {
    final int currentMonthId = directory.get(TimeService.class).getCurrentMonthId();
    return and(not(isNull(Series.FIRST_MONTH)),
               fieldStrictlyGreaterThan(Series.FIRST_MONTH, currentMonthId));
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
      cellBuilder.add("estimatedAccountPosition",
                      SavingsAccountViewPanel.getEstimatedAccountPositionLabel(accountKey, repository, directory));
      cellBuilder.add("estimatedAccountPositionDate",
                      SavingsAccountViewPanel.getEstimatedAccountPositionDateLabel(accountKey, repository, directory));

      final SavingsSeriesView seriesView = new SavingsSeriesView(account, repository, directory, 
                                                                 seriesEditionDialog, seriesButtons);
      cellBuilder.add("savingsSeries", seriesView.getPanel());
      cellBuilder.addDisposeListener(seriesView);
    }

    private GlobMatcher getSavingsSeriesMatcher() {
      return GlobMatchers.ALL;
    }
  }
}
