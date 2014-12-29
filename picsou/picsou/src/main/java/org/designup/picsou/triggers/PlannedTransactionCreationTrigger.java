package org.designup.picsou.triggers;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.accounts.utils.AccountMatchers;
import org.designup.picsou.gui.model.SeriesShape;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.AmountMap;
import org.designup.picsou.triggers.utils.SeriesAndMonths;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;

import java.util.*;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class PlannedTransactionCreationTrigger implements ChangeSetListener {

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    repository.startChangeSet();
    final SeriesAndMonths seriesAndMonths = new SeriesAndMonths();
    if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.MULTIPLE_PLANNED,
                                  UserPreferences.MONTH_FOR_PLANNED, UserPreferences.PERIOD_COUNT_FOR_PLANNED)) {
      Glob currenMonth = repository.get(CurrentMonth.KEY);
      SortedSet<Integer> seriesIds = repository.getAll(Series.TYPE).getSortedSet(Series.ID);
      GlobList months = repository.getAll(Month.TYPE);
      for (Integer seriesId : seriesIds) {
        for (Glob month : months) {
          if (month.get(Month.ID) >= currenMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
            seriesAndMonths.add(seriesId, month.get(Month.ID));
          }
        }
      }
    }

    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.TARGET_ACCOUNT)) {
          for (Glob month : repository.getAll(Month.TYPE)) {
            Integer seriesId = key.get(Series.ID);
            Integer monthId = month.get(Month.ID);
            seriesAndMonths.add(seriesId, monthId);
            deletePlannedTransactions(seriesId, monthId, repository);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = values.get(Transaction.SERIES);
        Integer monthId = values.get(Transaction.BUDGET_MONTH);
        if (seriesId != null && monthId != null) {
          seriesAndMonths.add(seriesId, monthId);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Integer previousSeriesId;
        Integer newSeriesId;
        Integer newMonth;
        Integer previousMonth;
        Glob transaction = repository.find(key);
        if (transaction == null) {
          return;
        }
        if (values.contains(Transaction.SERIES)) {
          previousSeriesId = values.getPrevious(Transaction.SERIES);
          newSeriesId = values.get(Transaction.SERIES);
        }
        else {
          newSeriesId = transaction.get(Transaction.SERIES);
          previousSeriesId = newSeriesId;
        }
        Glob newSeries = repository.find(Key.create(Series.TYPE, newSeriesId));
        if (!Series.UNCATEGORIZED_SERIES_ID.equals(newSeriesId) && newSeries != null && Utils.equal(newSeries.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
          for (Glob month : repository.getAll(Month.TYPE)) {
            seriesAndMonths.add(newSeriesId, month.get(Month.ID));
          }
        }
        Glob previousSeries = repository.find(Key.create(Series.TYPE, previousSeriesId));
        if (!Series.UNCATEGORIZED_SERIES_ID.equals(previousSeriesId) && previousSeries != null && Utils.equal(previousSeries.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
          for (Glob month : repository.getAll(Month.TYPE)) {
            seriesAndMonths.add(previousSeriesId, month.get(Month.ID));
          }
        }

        if (values.contains(Transaction.BUDGET_MONTH)) {
          previousMonth = values.getPrevious(Transaction.BUDGET_MONTH);
          newMonth = values.get(Transaction.BUDGET_MONTH);
        }
        else {
          newMonth = transaction.get(Transaction.BUDGET_MONTH);
          previousMonth = newMonth;
        }
        if (values.contains(Transaction.AMOUNT)
            || Utils.equal(newSeriesId, previousSeriesId)
            || Utils.equal(newMonth, previousMonth)) {
          if (previousSeriesId != null) {
            seriesAndMonths.add(previousSeriesId, previousMonth);
          }
          if (newSeriesId != null) {
            seriesAndMonths.add(newSeriesId, newMonth);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer series = previousValues.get(Transaction.SERIES);
        Integer monthId = previousValues.get(Transaction.BUDGET_MONTH);
        if (series != null) {
          seriesAndMonths.add(series, monthId);
        }
      }
    });

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        seriesAndMonths.add(values.get(SeriesBudget.SERIES), values.get(SeriesBudget.MONTH));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.PLANNED_AMOUNT)
            || values.contains(SeriesBudget.ACTIVE)) {
          Glob seriesBudget = repository.get(key);
          seriesAndMonths.add(seriesBudget.get(SeriesBudget.SERIES), seriesBudget.get(SeriesBudget.MONTH));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        deletePlannedTransactions(previousValues.get(SeriesBudget.SERIES), previousValues.get(SeriesBudget.MONTH), repository);
      }
    });

    changeSet.safeVisit(SeriesShape.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          seriesAndMonths.add(key.get(SeriesShape.SERIES_ID), month.get(Month.ID));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          seriesAndMonths.add(key.get(SeriesShape.SERIES_ID), month.get(Month.ID));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          seriesAndMonths.add(key.get(SeriesShape.SERIES_ID), month.get(Month.ID));
        }
      }
    });

    if (changeSet.containsCreationsOrDeletions(Account.TYPE)) {
      for (Glob series : repository.getAll(Series.TYPE, fieldEquals(Series.TARGET_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID))) {
        for (Glob month : repository.getAll(Month.TYPE)) {
          seriesAndMonths.add(series.get(Series.ID), month.get(Month.ID));
          deletePlannedTransactions(series.get(Series.ID), month.get(Month.ID), repository);
        }
      }
    }

    if (!seriesAndMonths.isEmpty()) {
      updatePlannedTransactions(seriesAndMonths, repository);
    }

    repository.completeChangeSet();
  }

  public void deletePlannedTransactions(Integer seriesId, Integer monthId, GlobRepository repository) {
    repository.delete(getPlannedTransactions(seriesId, monthId, repository));
  }

  private void updatePlannedTransactions(SeriesAndMonths seriesAndMonths, final GlobRepository repository) {

    Glob currentMonth = repository.get(CurrentMonth.KEY);
    final Integer currentMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    seriesAndMonths.apply(new SeriesAndMonths.Functor() {

      public void apply(Integer seriesId, Integer monthId) {

        GlobList plannedTransactionsForMonth = getSortedPlannedTransactions(seriesId, monthId, repository);

        Glob seriesBudget = SeriesBudget.get(seriesId, monthId, repository);
        if (seriesBudget == null ||
            Amounts.isNullOrZero(seriesBudget.get(SeriesBudget.PLANNED_AMOUNT)) ||
            !seriesBudget.isTrue(SeriesBudget.ACTIVE) ||
            monthId < currentMonthId) {
          repository.delete(plannedTransactionsForMonth);
          return;
        }

        Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
        if (series == null) {
          Log.write("No series for " + seriesBudget);
          return;
        }
        if (Utils.equal(series.get(Series.TARGET_ACCOUNT), Account.MAIN_SUMMARY_ACCOUNT_ID)) {
          repository.delete(plannedTransactionsForMonth);
          plannedTransactionsForMonth.clear();
        }

        Double actualAmount = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT);
        Double plannedAmount = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0);
        double remainder = plannedAmount - Utils.zeroIfNull(actualAmount);
        if (((plannedAmount > 0 && remainder > 0) || (plannedAmount < 0 && remainder < 0)) && !Amounts.isNearZero(remainder)) {
          if (repository.get(UserPreferences.KEY).isTrue(UserPreferences.MULTIPLE_PLANNED)) {
            distributePlannedAmountOnShape(seriesBudget, monthId, plannedTransactionsForMonth, repository);
          }
          else {
            Glob transaction = plannedTransactionsForMonth.getFirst();
            if (transaction == null) {
              createPlannedTransaction(series, monthId, seriesBudget.get(SeriesBudget.DAY), remainder, repository);
            }
            else {
              repository.update(transaction.getKey(), Transaction.AMOUNT, remainder);
              plannedTransactionsForMonth.remove(0);
              repository.delete(plannedTransactionsForMonth);
            }
          }
        }
        else {
          repository.delete(plannedTransactionsForMonth);
        }
      }
    });
  }

  private void distributePlannedAmountOnShape(Glob seriesBudget, Integer monthId, GlobList plannedTransactionsForMonth, GlobRepository repository) {
    Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
    if (series == null) { // bug : une series a disparu - on continue
      repository.delete(plannedTransactionsForMonth);
      Log.write("Missing series " + seriesBudget.get(SeriesBudget.SERIES));
      return;
    }

    DayAmountsCollector dayAmounts =
      computeDayAmounts(series, monthId,
                        seriesBudget.get(SeriesBudget.PLANNED_AMOUNT),
                        seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT, 0.00),
                        repository);

    for (DayAmount dayAmount : dayAmounts) {
      Glob existingTransaction = null;
      if (plannedTransactionsForMonth.size() != 0) {
        existingTransaction = plannedTransactionsForMonth.remove(0);
      }
      if (dayAmount.amount != 0) {
        if (existingTransaction != null) {
          repository.update(existingTransaction.getKey(),
                            value(Transaction.AMOUNT, dayAmount.amount),
                            value(Transaction.DAY, dayAmount.day),
                            value(Transaction.POSITION_DAY, dayAmount.day),
                            value(Transaction.BANK_DAY, dayAmount.day),
                            value(Transaction.BUDGET_DAY, dayAmount.day));
        }
        else {
          createPlannedTransaction(dayAmount.accountId, series, monthId, dayAmount.day, dayAmount.amount, repository);
        }
      }
      else {
        if (existingTransaction != null) {
          repository.delete(existingTransaction);
        }
      }
    }
    repository.delete(plannedTransactionsForMonth);
  }

  private static GlobList getPlannedTransactions(Integer series, Integer month, GlobRepository repository) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series)
      .findByIndex(Transaction.POSITION_MONTH, month)
      .getGlobs()
      .filter(and(isTrue(Transaction.PLANNED),
                  isNotTrue(Transaction.MIRROR)), repository)
      .sort(Transaction.DAY);
  }

  private static GlobList getSortedPlannedTransactions(Integer series, Integer month, GlobRepository repository) {
    return getPlannedTransactions(series, month, repository).sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }

  private static class DayAmount {
    final int accountId;
    final int day;
    double amount;

    DayAmount(int accountId, int day, double amount) {
      this.accountId = accountId;
      this.day = day;
      this.amount = amount;
    }

    public String toString() {
      return accountId + "/" + day + "/" + amount;
    }
  }

  private static class DayAmountsCollector implements Iterable<DayAmount> {
    final List<DayAmount> dayAmounts = new ArrayList<DayAmount>();

    public void add(int account, int day, double amount) {
      dayAmounts.add(new DayAmount(account, day, amount));
    }

    public List<DayAmount> account(int accountId) {
      List<DayAmount> result = new ArrayList<DayAmount>();
      for (DayAmount amountsForDay : dayAmounts) {
        if (accountId == amountsForDay.accountId) {
          result.add(amountsForDay);
        }
      }
      return result;
    }

    public Iterator<DayAmount> iterator() {
      return dayAmounts.iterator();
    }

    public String toString() {
      return dayAmounts.toString();
    }
  }

  private static DayAmountsCollector computeDayAmounts(Glob series, int monthId, double amount, double actual, GlobRepository repository) {

    final DayAmountsCollector dayAmounts = new DayAmountsCollector();

    Integer minDay = 0;
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    if ((currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId)) {
      minDay = currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY);
    }

    Integer targetAccount = series.get(Series.TARGET_ACCOUNT);
    if (targetAccount == null) {
      return dayAmounts;
    }

    if (Account.MAIN_SUMMARY_ACCOUNT_ID == targetAccount) {
      Integer[] accountIds = repository.getAll(Account.TYPE, AccountMatchers.activeUserCreatedMainAccounts(monthId)).getSortedArray(Account.ID);
      AmountMap actualAmounts = getActualsForTargetMonth(series, monthId, repository);
      double[] plannedAmounts = splitAmountBetweenAccounts(amount, accountIds, series.get(Series.ID), currentMonth, repository);
      levelPlannedWhenExceeded(accountIds, plannedAmounts, actualAmounts);
      for (int i = 0; i < accountIds.length; i++) {
        computeDayAmountsForAccount(series, monthId, plannedAmounts[i], accountIds[i], minDay, dayAmounts, repository);
      }
      for (int i = 0; i < accountIds.length; i++) {
        adjustDayAmountsWithActual(dayAmounts.account(accountIds[i]), actualAmounts.get(accountIds[i], 0.00));
      }
      return dayAmounts;
    }

    Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
    if (series.get(Series.MIRROR_SERIES) != null) {
      if (fromAccount == null || toAccount == null) {
        Log.write("Series '" + series.get(Series.NAME) + "' is a savings series with both accounts imported" +
                  " but one of the accounts is missing.");
        return dayAmounts;
      }
    }

    computeDayAmountsForAccount(series, monthId, amount, targetAccount, minDay, dayAmounts, repository);
    adjustDayAmountsWithActual(dayAmounts, actual);
    return dayAmounts;
  }

  public static void levelPlannedWhenExceeded(Integer[] accountIds, double[] plannedAmounts, AmountMap actualAmounts) {
    if (accountIds.length == 1) {
      return;
    }
    if (accountIds.length != plannedAmounts.length) {
      throw new RuntimeException("accounts:" + accountIds.length + " / planned:" + plannedAmounts.length);
    }
    double toRedistribute = 0;
    for (int i = 0; i < accountIds.length; i++) {
      double actual = actualAmounts.get(accountIds[i], 0.00);
      double planned = plannedAmounts[i];
      if (Amounts.sameSign(actual, planned) && Math.abs(actual) > Math.abs(planned)) {
        toRedistribute += actual - planned;
        plannedAmounts[i] = actual;
      }
    }
    for (int i = 0; i < accountIds.length; i++) {
      double actual = actualAmounts.get(accountIds[i], 0.00);
      double planned = plannedAmounts[i];
      if (Amounts.equal(actual, planned)) {
        continue;
      }
      if (Amounts.sameSign(actual, planned)) {
        if (Math.abs(planned) > Math.abs(actual)) {
          double diff = planned - actual;
          if (Amounts.sameSign(toRedistribute, diff)) {
            if (Math.abs(toRedistribute) <= Math.abs(diff)) {
              plannedAmounts[i] -= toRedistribute;
              toRedistribute = 0;
            }
            else {
              plannedAmounts[i] -= diff;
              toRedistribute -= diff;
            }
          }
        }
      }
      if (Amounts.isNearZero(toRedistribute)) {
        return;
      }
    }

  }

  private static AmountMap getActualsForTargetMonth(Glob series, int monthId, GlobRepository repository) {
    AmountMap actualForTargetMonth = new AmountMap();
    GlobList transactions =
      Transaction.getAllForSeriesAndMonth(series.get(Series.ID), monthId, repository)
        .filterSelf(isFalse(Transaction.PLANNED), repository);
    for (Glob transaction : transactions) {
      actualForTargetMonth.add(transaction.get(Transaction.ACCOUNT), transaction.get(Transaction.AMOUNT));
    }
    return actualForTargetMonth;
  }

  private static void adjustDayAmountsWithActual(Iterable<DayAmount> dayAmounts, double actualForAccount) {
    for (DayAmount dayAmount : dayAmounts) {
      Double plannedAmount = dayAmount.amount;
      double remainder = plannedAmount - actualForAccount;
      if (!Amounts.isNearZero(remainder) && Amounts.isSameSign(plannedAmount, remainder)) {
        dayAmount.amount = remainder;
        break;
      }
      else {
        actualForAccount -= dayAmount.amount;
        dayAmount.amount = 0;
      }
    }
  }

  private static double[] splitAmountBetweenAccounts(double amount, Integer[] accountIds, Integer seriesId, Glob currentMonth, GlobRepository repository) {
    if (accountIds.length == 0) {
      return new double[0];
    }
    if (accountIds.length == 1) {
      return new double[]{amount};
    }

    AmountMap actualAmounts = new AmountMap();
    int past = 0;
    for (int monthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
         repository.contains(Key.create(Month.TYPE, monthId)) && (past < 3);
         monthId = Month.previous(monthId)) {
      GlobList transactions =
        Transaction.getAllForSeriesAndMonth(seriesId, monthId, repository)
          .filterSelf(isFalse(Transaction.PLANNED), repository);
      if (!transactions.isEmpty()) {
        past++;
      }
      for (Glob transaction : transactions) {
        Integer accountId = transaction.get(Transaction.ACCOUNT);
        Double transactionAmount = transaction.get(Transaction.AMOUNT);
        actualAmounts.add(accountId, transactionAmount);
      }
    }

    if (actualAmounts.isEmpty()) {
      return Amounts.split(amount, accountIds.length);
    }

    Double[] values = new Double[accountIds.length];
    for (int i = 0; i < accountIds.length; i++) {
      values[i] = actualAmounts.get(accountIds[i], 0.00);
    }
    return Amounts.adjustTotal(values, amount);
  }

  private static void computeDayAmountsForAccount(Glob series, int monthId, double amount, int accountId, Integer minDay,
                                                  final DayAmountsCollector dayAmounts, GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);

    Integer period = repository.get(UserPreferences.KEY).get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
    Key key = Key.create(SeriesShape.TYPE, seriesId);
    Glob seriesShape = repository.find(key);
    if (seriesShape == null) {
      Integer mirrorId = series.get(Series.MIRROR_SERIES);
      if (mirrorId != null) {
        seriesShape = repository.find(Key.create(SeriesShape.TYPE, mirrorId));
      }
    }

    if (seriesShape != null && seriesShape.get(SeriesShape.FIXED_DATE) != null) {
      dayAmounts.add(accountId, Math.max(minDay, Month.getDay(seriesShape.get(SeriesShape.FIXED_DATE), monthId)), amount);
      return;
    }

    if (seriesShape == null
        || seriesShape.get(SeriesShape.TOTAL, 0) == 0
        || Math.abs(amount / seriesShape.get(SeriesShape.TOTAL)) > 3) {
      seriesShape = SeriesShape.getDefault(key, period);
    }
    Integer percentToPropagate = 0;
    double alreadyAssignedAmount = 0;
    for (int i = 1; i <= period; i++) {
      IntegerField field = SeriesShape.getField(i);
      Integer percent = seriesShape.get(field);
      percent = percent == null ? 0 : percent;
      int day = SeriesShape.getDay(period, i, monthId);
      if (minDay != 0 && day < minDay) {
        int nextDay = SeriesShape.getDay(period, i + 1, monthId);
        if (nextDay < minDay && i != period) {
          percentToPropagate += percent;
        }
        else {
          double amountForPeriod = getAmount(alreadyAssignedAmount, amount, percentToPropagate + percent, i == period);
          if (!Amounts.isNearZero(amountForPeriod)) {
            alreadyAssignedAmount += amountForPeriod;
            percentToPropagate = 0;
            dayAmounts.add(accountId, minDay, amountForPeriod);
          }
        }
      }
      else {
        double amountForPeriod = getAmount(alreadyAssignedAmount, amount, percentToPropagate + percent, i == period);
        if (!Amounts.isNearZero(amountForPeriod)) {
          alreadyAssignedAmount += amountForPeriod;
          percentToPropagate = 0;
          dayAmounts.add(accountId, day, amountForPeriod);
        }
      }
    }
  }

  private static double getAmount(double alreadyAssignedAmount, double totalAmount, int percent, boolean isLast) {
    if (isLast) {
      return totalAmount - alreadyAssignedAmount;
    }
    else {
      int amount = (int) ((totalAmount * percent) / 100);
      if (Math.abs(amount + alreadyAssignedAmount - totalAmount) < 10) {
        return totalAmount - alreadyAssignedAmount;
      }
      return amount;
    }
  }

  public static void createPlannedTransaction(Glob series, int monthId, Integer day, double amount, GlobRepository repository) {
    Glob month = repository.get(CurrentMonth.KEY);
    if ((month.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId)
        && ((day == null) ||
            (day < month.get(CurrentMonth.LAST_TRANSACTION_DAY)))) {
      day = month.get(CurrentMonth.LAST_TRANSACTION_DAY);
    }

    Integer account = series.get(Series.TARGET_ACCOUNT);
    if (account == null) {
      account = Account.MAIN_SUMMARY_ACCOUNT_ID;
    }

    createPlannedTransaction(account, series, monthId, day, amount, repository);
  }

  private static void createPlannedTransaction(int accountId, Glob series, int monthId, int day, double amount, GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ACCOUNT, accountId),
                      value(Transaction.AMOUNT, amount),
                      value(Transaction.SERIES, seriesId),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.BANK_DAY, day),
                      value(Transaction.POSITION_MONTH, monthId),
                      value(Transaction.POSITION_DAY, day),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.DAY, day),
                      value(Transaction.BUDGET_MONTH, monthId),
                      value(Transaction.BUDGET_DAY, day),
                      value(Transaction.LABEL, Series.getPlannedTransactionLabel(seriesId, series)),
                      value(Transaction.PLANNED, true),
                      value(Transaction.TRANSACTION_TYPE,
                            amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()));
  }
}
