package org.designup.picsou.gui.accounts.chart;

import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.designup.picsou.gui.card.NavigationService;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.tips.DetailsTip;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Day;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class SelectedAccountPositionsChartView extends PositionsChartView {

  public SelectedAccountPositionsChartView(String componentName, HistoChartRange range,
                                           final GlobRepository repository, final Directory directory) {
    super(range,
          new HistoChartConfig(false, false, false, true, true, true, true, true, true, true),
          componentName, repository, directory);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        update();
      }
    }, Account.TYPE, Transaction.TYPE);
  }

  protected void processClick(HistoSelection selection, Set<Key> objectKeys, NavigationService navigationService) {
    if (objectKeys.size() != 1) {
      return;
    }

    Key objectKey = objectKeys.iterator().next();
    GlobList transactions = getTransactions(objectKey);
    if (transactions.isEmpty()) {
      DetailsTip tip =
        new DetailsTip(histoChartBuilder.getChart(), Lang.get("seriesAnalysis.chart.histo.daily.budgetSummary.noData.tooltip",
                                                              Day.getFullLabel(objectKey)), directory);
      tip.show();
      return;
    }

    Integer monthId = objectKey.get(Day.MONTH);
    showTransactions(transactions, monthId, directory.get(SelectionService.class), repository, directory);
  }

  protected void updateChart(HistoChartBuilder histoChartBuilder, Integer currentMonthId, boolean resetPosition) {

    GlobList accounts = selectionService.getSelection(Account.TYPE);
    Set<Integer> accountIds = accounts.getValueSet(Account.ID);

    DaySelection daySelection = new DaySelection();
    for (Glob transaction : selectionService.getSelection(Transaction.TYPE)) {
      daySelection.add(transaction.get(Transaction.MONTH), transaction.get(Transaction.DAY));
    }

    if (accountIds == null || accountIds.isEmpty()) {
      histoChartBuilder.showDailyHisto(currentMonthId, true,
                                       Matchers.transactionsForMainAccounts(repository),
                                       daySelection, "daily", Transaction.SUMMARY_POSITION);
    }
    else {
      histoChartBuilder.showAccountDailyHisto(currentMonthId, true, accountIds, daySelection, "daily");
    }
  }
}
