package org.designup.picsou.gui.accounts.position;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.utils.DaySelection;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.TransactionComparator;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DailyAccountPositionComputer {

  private GlobRepository repository;

  public DailyAccountPositionComputer(GlobRepository repository) {
    this.repository = repository;
  }

  public DailyAccountPositionValues getAccountDailyValues(List<Integer> monthIdsToShow, int selectedMonthId, boolean showFullMonthLabels, Set<Integer> accountIds, DaySelection daySelection, String daily) {

    Map<Integer, Double> lastValueForAccounts = new HashMap<Integer, Double>();
    if (!monthIdsToShow.isEmpty()) {
      for (Integer accountId : accountIds) {
        GlobMatcher accountMatcher = Matchers.transactionsForAccount(accountId);
        Double lastValue = getLastValue(accountMatcher, monthIdsToShow.get(0), Transaction.ACCOUNT_POSITION, repository);
        if (lastValue == null) {
          Glob account = repository.find(Key.create(Account.TYPE, accountId));
          if (account != null) {
            lastValue = account.get(Account.POSITION_WITH_PENDING);
          }
          else {
            lastValue = 0.00;
          }
        }
        lastValueForAccounts.put(accountId, lastValue);
      }
    }

    DailyAccountPositionValues positionValues = new DailyAccountPositionValues();

    for (int monthId : monthIdsToShow) {
      int maxDayForMonth = Month.getLastDayNumber(monthId);
      Double[] minValuesForAll = new Double[maxDayForMonth];
      for (Integer accountId : accountIds) {

        GlobMatcher accountMatcher = Matchers.transactionsForAccount(accountId);

        GlobList transactions = repository.findByIndex(Transaction.POSITION_MONTH_INDEX, monthId)
          .filterSelf(accountMatcher, repository)
          .sort(TransactionComparator.ASCENDING_ACCOUNT);

        Double[] minValuesForAccount = new Double[maxDayForMonth];
        Double lastValue = lastValueForAccounts.get(accountId);
        Double newLastValue = getDailyValues(transactions, lastValue, minValuesForAccount, Transaction.ACCOUNT_POSITION);
        for (int dayIndex = 0; dayIndex < maxDayForMonth; dayIndex++) {
          if (minValuesForAll[dayIndex] == null) {
            minValuesForAll[dayIndex] = minValuesForAccount[dayIndex];
          }
          else if (minValuesForAccount[dayIndex] != null) {
            minValuesForAll[dayIndex] += minValuesForAccount[dayIndex];
          }
        }

        lastValueForAccounts.put(accountId, newLastValue);
      }

      positionValues.add(monthId, minValuesForAll, monthId == selectedMonthId, daySelection.getValues(monthId, maxDayForMonth));
    }

    return positionValues;
  }

  public DailyAccountPositionValues getMainValues(List<Integer> monthIdsToShow, int selectedMonthId) {
    return getDailyValues(monthIdsToShow, selectedMonthId, Matchers.transactionsForMainAccounts(repository), DaySelection.EMPTY, Transaction.SUMMARY_POSITION);
  }

  public DailyAccountPositionValues getSavingsValues(List<Integer> monthIdsToShow, int selectedMonthId) {
    return getDailyValues(monthIdsToShow, selectedMonthId, Matchers.transactionsForSavingsAccounts(repository), DaySelection.EMPTY, Transaction.SUMMARY_POSITION);
  }

  public DailyAccountPositionValues getDailyValues(List<Integer> monthIdsToShow, int selectedMonthId, GlobMatcher accountMatcher,
                                                   DaySelection daySelection, final DoubleField position) {

    DailyAccountPositionValues positionValues = new DailyAccountPositionValues();

    Double lastValue = getLastValue(accountMatcher, monthIdsToShow.get(0), position, repository);

    for (int monthId : monthIdsToShow) {
      GlobList transactions = repository.findByIndex(Transaction.POSITION_MONTH_INDEX, monthId)
        .filterSelf(accountMatcher, repository)
        .sort(TransactionComparator.ASCENDING_ACCOUNT);

      int maxDay = Month.getLastDayNumber(monthId);
      if (!transactions.isEmpty()) {
        maxDay = Math.max(maxDay, transactions.getSortedSet(Transaction.POSITION_DAY).last());
      }

      Double[] minValues = new Double[maxDay];
      lastValue = getDailyValues(transactions, lastValue, minValues, position);

      positionValues.add(monthId, minValues, monthId == selectedMonthId, daySelection.getValues(monthId, maxDay));
    }

    return positionValues;
  }

  private Double getDailyValues(GlobList transactions, Double previousLastValue,
                                Double[] minValues, DoubleField positionField) {
    Double lastValue = previousLastValue;
    for (Glob transaction : transactions) {
      int day = transaction.get(Transaction.POSITION_DAY) - 1;
      if (day >= minValues.length) {
        day = minValues.length - 1;
      }
      minValues[day] = transaction.get(positionField);
    }

    for (int i = 0; i < minValues.length; i++) {
      if (minValues[i] == null) {
        minValues[i] = lastValue;
      }
      else {
        lastValue = minValues[i];
      }
    }

    for (int i = minValues.length - 2; i >= 0; i--) {
      if (minValues[i] == null) {
        minValues[i] = minValues[i + 1];
      }
    }

    return lastValue;
  }

  public static Double getLastValue(GlobMatcher accountMatcher, Integer currentMonthId, DoubleField positionField, GlobRepository repository) {

    LastGlobFunctor callback = new LastGlobFunctor(accountMatcher);

    Integer monthId = currentMonthId;
    monthId = Month.previous(monthId);
    while (callback.transaction == null && repository.find(Key.create(Month.TYPE, monthId)) != null) {
      repository.findByIndex(Transaction.POSITION_MONTH_INDEX, monthId).safeApply(callback, repository);
      monthId = Month.previous(monthId);
    }
    if (callback.transaction != null) {
      return callback.transaction.get(positionField);
    }

    FirstGlobFunctor firstCallback = new FirstGlobFunctor(accountMatcher);
    monthId = currentMonthId;
    while (firstCallback.transaction == null && repository.find(Key.create(Month.TYPE, monthId)) != null) {
      repository.findByIndex(Transaction.POSITION_MONTH_INDEX, monthId).safeApply(firstCallback, repository);
      monthId = Month.previous(monthId);
    }
    if (firstCallback.transaction == null) {
      return null;
    }
    return Amounts.diff(firstCallback.transaction.get(positionField), firstCallback.transaction.get(Transaction.AMOUNT));
  }

  private static class LastGlobFunctor implements GlobFunctor {
    private Glob transaction;
    private GlobMatcher accountMatcher;

    public LastGlobFunctor(GlobMatcher accountMatcher) {
      this.accountMatcher = accountMatcher;
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (accountMatcher.matches(glob, repository)) {
        if (transaction == null) {
          transaction = glob;
        }
        if (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, glob) < 0) {
          transaction = glob;
        }
      }
    }
  }

  private static class FirstGlobFunctor implements GlobFunctor {
    private Glob transaction;
    private GlobMatcher accountMatcher;

    public FirstGlobFunctor(GlobMatcher accountMatcher) {
      this.accountMatcher = accountMatcher;
    }

    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (accountMatcher.matches(glob, repository)) {
        if (transaction == null) {
          transaction = glob;
        }
        if (TransactionComparator.ASCENDING_ACCOUNT.compare(transaction, glob) > 0) {
          transaction = glob;
        }
      }
    }
  }
}
