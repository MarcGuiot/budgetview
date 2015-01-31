package org.designup.picsou.gui.accounts.chart;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoChartColors;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.daily.HistoDailyColors;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineColors;
import org.designup.picsou.gui.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Day;
import org.designup.picsou.model.Transaction;
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
  private HistoDailyColors dailyColors;
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

    HistoLineColors accountColors = disposables.add(new HistoLineColors(
      "sidebar.histo.account.line.positive",
      "sidebar.histo.account.line.negative",
      "sidebar.histo.account.fill.positive",
      "sidebar.histo.account.fill.negative",
      directory
    ));

    dailyColors = disposables.add(new HistoDailyColors(
      accountColors,
      "sidebar.histo.account.daily.current",
      "sidebar.histo.account.daily.current.annotation",
      "sidebar.histo.account.inner.label.positive",
      "sidebar.histo.account.inner.label.negative",
      "sidebar.histo.account.inner.label.line",
      "sidebar.histo.account.inner.rollover.day",
      "sidebar.histo.account.inner.selected.day",
      directory
    ));
  }

  protected HistoChartColors createChartColors(Directory directory) {
    return new HistoChartColors("sidebar.histo", directory);
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
                                     dailyColors);
  }

  public void dispose() {
    super.dispose();
    disposables.dispose();
  }
}
