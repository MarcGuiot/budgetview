package org.designup.picsou.gui.savings;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.accounts.CreateAccountAction;
import org.designup.picsou.gui.accounts.position.AccountPositionLabels;
import org.designup.picsou.gui.accounts.position.SavingsAccountPositionLabels;
import org.designup.picsou.gui.budget.SeriesEditionButtons;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.series.analysis.histobuilders.AccountHistoChartUpdater;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.utils.GlobRepeat;
import org.globsframework.gui.views.GlobLabelView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collections;

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

    SeriesEditionButtons seriesButtons = new SeriesEditionButtons(BudgetArea.SAVINGS, repository, directory);
    seriesButtons.setNames("createSavingsSeries");
    seriesButtons.registerButtons(builder);

    builder.add("toggleToMain", new ToggleToMainAction());

    builder.add("createSavingsAccount", new CreateAccountAction(AccountType.SAVINGS, repository, directory));

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
                                                                 seriesButtons);
      cellBuilder.add("savingsSeries", seriesView.getPanel());
      cellBuilder.addDisposeListener(seriesView);

      final HistoChartBuilder histoChartBuilder =
        new HistoChartBuilder(new HistoChartConfig(false, false, false, true, true, true, false, false),
                              new ScrollableHistoChartRange(2, 6, false, repository),
                              repository, directory, selectionService);
      AccountHistoChartUpdater updater = new AccountHistoChartUpdater(histoChartBuilder, repository, directory) {
        protected void update(Integer currentMonthId, boolean resetPosition) {
          if (account.exists()) {
            histoChartBuilder.showDailyHisto(currentMonthId, false,
                                             Collections.singleton(account.get(Account.ID)),
                                             DaySelection.EMPTY);
          }
        }
      };
      cellBuilder.addDisposeListener(updater);
      cellBuilder.add("savingsAccountChart", histoChartBuilder.getChart());
    }

  }

  private class ToggleToMainAction extends AbstractAction {
    private ToggleToMainAction() {
      super(Lang.get("savingsView.toggleToMain"));
    }

    public void actionPerformed(ActionEvent actionEvent) {
      directory.get(NavigationService.class).gotoBudgetForMainAccounts();
    }
  }
}
