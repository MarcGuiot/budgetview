package org.designup.picsou.gui.accounts.chart;

import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.series.analysis.histobuilders.HistoChartBuilder;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;

import java.util.Set;

public class TransactionAccountPositionsChartView extends AccountsChartView {

  public TransactionAccountPositionsChartView(String componentName, HistoChartRange range,
                                              final GlobRepository repository, final Directory directory) {
    super(range,
          new HistoChartConfig(false, false, false, true, true, true, true, true, true),
          componentName, repository, directory);
    selectionService.addListener(new GlobSelectionListener() {
      public void selectionUpdated(GlobSelection selection) {
        update();
      }
    }, Account.TYPE, Transaction.TYPE);
    histoChartBuilder.getChart().addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection selection, Key objectKey) {
        MainDailyPositionsChartView.selectTransactions(objectKey, histoChartBuilder.getChart(),
                                                       repository, directory, selectionService);
      }
    });
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
                                       daySelection, "daily");
    }
    else {
      histoChartBuilder.showAccountDailyHisto(currentMonthId, true, accountIds, daySelection, "daily");
    }
  }
}
