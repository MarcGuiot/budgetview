package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.accounts.position.SavingsAccountPositionLabels;
import org.designup.picsou.gui.budget.SeriesEditionButtons;
import org.designup.picsou.gui.series.SeriesAmountEditionDialog;
import org.designup.picsou.gui.series.SeriesEditionDialog;
import org.designup.picsou.gui.series.evolution.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.evolution.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

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

    createSavingsBlock(builder);

    parentBuilder.add("savingsView", builder);
  }

  private void createSavingsBlock(GlobsPanelBuilder builder) {

    SeriesEditionButtons seriesButtons = new SeriesEditionButtons(BudgetArea.SAVINGS, repository, directory,
                                                                  directory.get(SeriesEditionDialog.class));
    seriesButtons.setNames("createSavingsSeries", "editAllSavingsSeries");
    seriesButtons.registerButtons(builder);

    AccountPositionLabels.registerReferencePositionLabels(builder, Account.SAVINGS_SUMMARY_ACCOUNT_ID,
                                                          "totalReferenceSavingsPositionAmount",
                                                          "totalReferenceSavingsPositionDate",
                                                          "accountView.total.date");

    Key accountKey = Key.create(Account.TYPE, Account.SAVINGS_SUMMARY_ACCOUNT_ID);
    AccountPositionLabels positionLabels = new SavingsAccountPositionLabels(accountKey, repository, directory);
    builder.add("totalEstimatedSavingsPositionAmount",
                positionLabels.getEstimatedAccountPositionLabel(true));
    builder.add("totalEstimatedSavingsPositionDate",
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

    public void registerComponents(RepeatCellBuilder cellBuilder, final Glob account) {

      cellBuilder.add("accountName",
                      GlobLabelView.init(Account.NAME, repository, directory)
                        .forceSelection(account.getKey()).getComponent());

      final Key accountKey = account.getKey();
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

      HistoChartBuilder histoChartBuilder = new HistoChartBuilder(false, false, repository, directory, selectionService, 6, 12);
      AccountHistoChartUpdater updater = new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
        protected void update(HistoChartBuilder histoChartBuilder, Integer currentMonthId) {
          if (account.exists()) {
            histoChartBuilder.showSavingsAccountHisto(currentMonthId, account.get(Account.ID));
          }
        }
      };
      cellBuilder.addDisposeListener(updater);
      cellBuilder.add("savingsAccountChart", histoChartBuilder.getChart());
    }

  }
}
