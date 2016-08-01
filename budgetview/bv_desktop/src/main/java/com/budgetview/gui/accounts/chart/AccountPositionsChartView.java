package com.budgetview.gui.accounts.chart;

import com.budgetview.gui.analysis.histobuilders.range.HistoChartRange;
import com.budgetview.gui.card.NavigationService;
import com.budgetview.gui.components.charts.histo.HistoSelection;
import com.budgetview.model.Transaction;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.gui.analysis.histobuilders.HistoChartBuilder;
import com.budgetview.gui.utils.DaySelection;
import com.budgetview.model.Account;
import com.budgetview.model.Day;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.DisposableGroup;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class AccountPositionsChartView extends PositionsChartView {

  public static final HistoChartConfig FULL_CONFIG = new HistoChartConfig(true, false, true, true, true, true, true, true, true, true);
  public static final HistoChartConfig STRIPPED_CONFIG = new HistoChartConfig(false, false, false, false, false, false, false, true, true, true);

  private final Integer accountId;
  private final SidebarAccountChartColors accountChartColors;
  private DisposableGroup disposables = new DisposableGroup();

  public static AccountPositionsChartView stripped(Integer accountId, String componentName, HistoChartRange range,
                                                   final GlobRepository repository, final Directory directory) {
    return new AccountPositionsChartView(accountId, componentName, range, repository, directory, STRIPPED_CONFIG);
  }

  public static AccountPositionsChartView full(Integer accountId, String componentName, HistoChartRange range,
                                               final GlobRepository repository, final Directory directory) {
    return new AccountPositionsChartView(accountId, componentName, range, repository, directory, FULL_CONFIG);
  }

  private AccountPositionsChartView(Integer accountId, String componentName, HistoChartRange range,
                                    final GlobRepository repository, final Directory directory, HistoChartConfig config) {
    super(range, config, componentName, repository, directory);
    this.accountId = accountId;
    this.accountChartColors = disposables.add(new SidebarAccountChartColors(directory));
  }

  protected void processClick(HistoSelection selection, Set<Key> objectKeys, NavigationService navigationService) {
    selectionService.select(repository.get(Key.create(Account.TYPE, accountId)));

    if (objectKeys.size() != 1) {
      return;
    }

    Key objectKey = objectKeys.iterator().next();
    GlobList transactions = getTransactions(objectKey);
    if (transactions.isEmpty()) {
      selectionService.clear(Transaction.TYPE);
    }
    else {
      Integer monthId = objectKey.get(Day.MONTH);
      showTransactions(transactions, monthId, directory.get(SelectionService.class), repository, directory);
    }
  }

  protected GlobList getTransactions(Integer monthId, Integer day) {
    return repository.getAll(Transaction.TYPE,
                             and(fieldEquals(Transaction.ACCOUNT, accountId),
                                 fieldEquals(Transaction.POSITION_MONTH, monthId),
                                 fieldEquals(Transaction.POSITION_DAY, day)));
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {
    histoChartBuilder.showDailyHisto(currentMonthId, accountId,
                                     DaySelection.EMPTY, "daily",
                                     Transaction.ACCOUNT_POSITION,
                                     accountChartColors.getDailyColors());
  }

  public void dispose() {
    super.dispose();
    disposables.dispose();
  }
}
