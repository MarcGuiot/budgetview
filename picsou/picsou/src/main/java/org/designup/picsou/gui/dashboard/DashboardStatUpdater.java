package org.designup.picsou.gui.dashboard;

import org.designup.picsou.gui.model.AccountWeather;
import org.designup.picsou.gui.model.DashboardStat;
import org.designup.picsou.gui.model.WeatherType;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class DashboardStatUpdater implements ChangeSetListener {

  public static void init(GlobRepository repository) {
    DashboardStatUpdater listener = new DashboardStatUpdater();
    repository.addTrigger(listener);
    listener.updateAll(repository);
  }

  private DashboardStatUpdater() {
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(AccountWeather.TYPE) || changeSet.containsChanges(CurrentMonth.TYPE)) {
      updateWeather(repository);
    }
    if (changeSet.containsChanges(Account.TYPE)) {
      updateAmounts(repository);
    }
    if (changeSet.containsCreations(TransactionImport.TYPE)) {
      updateImport(repository);
    }
    if (changeSet.containsChanges(Transaction.TYPE)) {
      updateUncategorized(repository);
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(AccountWeather.TYPE) || changedTypes.contains(CurrentMonth.TYPE) ||
        changedTypes.contains(Account.TYPE) || changedTypes.contains(TransactionImport.TYPE) ||
        changedTypes.contains(Transaction.TYPE)) {
      updateAll(repository);
    }
  }

  public void updateAll(GlobRepository repository) {
    repository.findOrCreate(DashboardStat.KEY);
    updateWeather(repository);
    updateAmounts(repository);
    updateImport(repository);
    updateUncategorized(repository);
  }

  private void updateImport(GlobRepository repository) {
    SortedSet<Date> dates = repository.getAll(TransactionImport.TYPE).getSortedSet(TransactionImport.IMPORT_DATE);
    Integer dayCount;
    if (dates.isEmpty()) {
      dayCount = null;
    }
    else {
      Date lastDate = dates.last();
      Date today = TimeService.getToday();
      dayCount = Month.distance(Month.getMonthId(lastDate), Month.getDay(lastDate),
                                Month.getMonthId(today), Month.getDay(today));
    }
    repository.update(DashboardStat.KEY, DashboardStat.DAYS_SINCE_LAST_IMPORT, dayCount);
  }

  public void updateWeather(GlobRepository repository) {
    WeatherType summaryWeather = WeatherType.SUNNY;
    double summaryMin = 0;
    Integer lastMonth = Integer.MAX_VALUE;
    for (Glob accountWeather : repository.getAll(AccountWeather.TYPE)) {
      if (AccountWeather.isForMainAccount(accountWeather, repository)) {
        Double min = accountWeather.get(AccountWeather.FUTURE_MIN);
        if (!Double.isNaN(min)) {
          summaryMin += min;
        }
      }
      WeatherType weather = WeatherType.get(accountWeather.get(AccountWeather.WEATHER));
      summaryWeather = weather.worseThan(summaryWeather) ? weather : summaryWeather;
      if (accountWeather.get(AccountWeather.LAST_FORECAST_MONTH) < lastMonth) {
        lastMonth = accountWeather.get(AccountWeather.LAST_FORECAST_MONTH);
      }
    }
    if (Utils.equal(lastMonth, Integer.MAX_VALUE)) {
      lastMonth = Month.getMonthId(TimeService.getToday());
    }
    repository.update(DashboardStat.KEY,
                      value(DashboardStat.WEATHER, summaryWeather.getId()),
                      value(DashboardStat.LAST_FORECAST_MONTH, lastMonth),
                      value(DashboardStat.REMAINDER, summaryMin));
  }

  private void updateUncategorized(GlobRepository repository) {
    repository.update(DashboardStat.KEY,
                      value(DashboardStat.UNCATEGORIZED_COUNT,
                            repository.getAll(Transaction.TYPE,
                                              and(fieldEquals(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                                                  Transaction.getMatcherForRealOperations())).size()));
  }

  private void updateAmounts(GlobRepository repository) {
    if (!repository.contains(Account.MAIN_SUMMARY_KEY) || !repository.contains(Account.SAVINGS_SUMMARY_KEY)) {
      repository.update(DashboardStat.KEY,
                        value(DashboardStat.TOTAL_MAIN_ACCOUNTS, 0.00),
                        value(DashboardStat.TOTAL_MAIN_ACCOUNTS_DATE, new Date()),
                        value(DashboardStat.TOTAL_ALL_ACCOUNTS, 0.00),
                        value(DashboardStat.TOTAL_ALL_ACCOUNTS_DATE, new Date()),
                        value(DashboardStat.HAS_SAVINGS_ACCOUNTS, false));
      return;
    }

    Glob mainSummary = repository.find(Account.MAIN_SUMMARY_KEY);
    double mainAmount = mainSummary.get(Account.POSITION_WITH_PENDING, 0.00);
    Date mainAmountDate = mainSummary.get(Account.POSITION_DATE);
    Glob savingsSummary = repository.find(Account.SAVINGS_SUMMARY_KEY);
    double savingsAmount = savingsSummary.get(Account.POSITION_WITH_PENDING, 0.00);
    Date savingsAmountDate = savingsSummary.get(Account.POSITION_DATE);
    TreeSet<Integer> months = new TreeSet<Integer>();
    months.add(Month.getMonthId(TimeService.getToday()));
    boolean hasSavingsAccounts = repository.contains(Account.TYPE, Account.activeUserCreatedSavingsAccounts(months));
    repository.update(DashboardStat.KEY,
                      value(DashboardStat.TOTAL_MAIN_ACCOUNTS, mainAmount),
                      value(DashboardStat.TOTAL_MAIN_ACCOUNTS_DATE, mainAmountDate),
                      value(DashboardStat.TOTAL_ALL_ACCOUNTS, mainAmount + savingsAmount),
                      value(DashboardStat.TOTAL_ALL_ACCOUNTS_DATE, Utils.min(mainAmountDate, savingsAmountDate)),
                      value(DashboardStat.HAS_SAVINGS_ACCOUNTS, hasSavingsAccounts));

  }
}
