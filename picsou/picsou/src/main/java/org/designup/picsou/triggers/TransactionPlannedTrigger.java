package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesShape;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Utils;
import org.globsframework.utils.collections.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class TransactionPlannedTrigger implements ChangeSetListener {

  public TransactionPlannedTrigger() {
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    repository.startChangeSet();
    final Set<Pair<Integer, Integer>> listOfSeriesAndMonth = new HashSet<Pair<Integer, Integer>>();
    if (changeSet.containsChanges(UserPreferences.KEY, UserPreferences.MULTIPLE_PLANNED,
                                  UserPreferences.MONTH_FOR_PLANNED, UserPreferences.PERIOD_COUNT_FOR_PLANNED)) {
      Glob currenMonth = repository.get(CurrentMonth.KEY);
      SortedSet<Integer> seriesId = repository.getAll(Series.TYPE).getSortedSet(Series.ID);
      GlobList months = repository.getAll(Month.TYPE);
      for (Integer id : seriesId) {
        for (Glob glob : months) {
          if (glob.get(Month.ID) >= currenMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
            listOfSeriesAndMonth.add(new Pair<Integer, Integer>(id, glob.get(Month.ID)));
          }
        }
      }
    }

    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Integer seriesId = values.get(Transaction.SERIES);
        Integer monthId = values.get(Transaction.BUDGET_MONTH);
        if (seriesId != null && monthId != null) {
          listOfSeriesAndMonth.add(new Pair<Integer, Integer>(seriesId, monthId));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Integer previousSeries;
        Integer newSeries;
        Integer newMonth;
        Integer previousMonth;
        Glob transaction = repository.get(key);
        if (values.contains(Transaction.SERIES)) {
          previousSeries = values.getPrevious(Transaction.SERIES);
          newSeries = values.get(Transaction.SERIES);
        }
        else {
          newSeries = transaction.get(Transaction.SERIES);
          previousSeries = newSeries;
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
            || Utils.equal(newSeries, previousSeries)
            || Utils.equal(newMonth, previousMonth)) {
          if (previousSeries != null) {
            listOfSeriesAndMonth.add(new Pair<Integer, Integer>(previousSeries, previousMonth));
          }
          if (newSeries != null) {
            listOfSeriesAndMonth.add(new Pair<Integer, Integer>(newSeries, newMonth));
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer series = previousValues.get(Transaction.SERIES);
        Integer monthId = previousValues.get(Transaction.BUDGET_MONTH);
        if (series != null) {
          listOfSeriesAndMonth.add(new Pair<Integer, Integer>(series, monthId));
        }
      }
    });

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        listOfSeriesAndMonth.add(new Pair<Integer, Integer>(values.get(SeriesBudget.SERIES),
                                                            values.get(SeriesBudget.MONTH)));
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(SeriesBudget.PLANNED_AMOUNT)
            || values.contains(SeriesBudget.ACTIVE)) {
          Glob seriesBudget = repository.get(key);
          listOfSeriesAndMonth.add(new Pair<Integer, Integer>(seriesBudget.get(SeriesBudget.SERIES),
                                                              seriesBudget.get(SeriesBudget.MONTH)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList transactions = getPlannedTransactions(repository, previousValues.get(SeriesBudget.SERIES),
                                                       previousValues.get(SeriesBudget.MONTH));
        repository.delete(transactions);
      }
    });

    changeSet.safeVisit(SeriesShape.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          listOfSeriesAndMonth.add(new Pair<Integer, Integer>(key.get(SeriesShape.SERIES_ID), month.get(Month.ID)));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          listOfSeriesAndMonth.add(new Pair<Integer, Integer>(key.get(SeriesShape.SERIES_ID), month.get(Month.ID)));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        for (Glob month : repository.getAll(Month.TYPE)) {
          listOfSeriesAndMonth.add(new Pair<Integer, Integer>(key.get(SeriesShape.SERIES_ID), month.get(Month.ID)));
        }
      }
    });

    updatePlannedTransactions(repository, listOfSeriesAndMonth);
    repository.completeChangeSet();
  }

  private void updatePlannedTransactions(GlobRepository repository, Set<Pair<Integer, Integer>> listOfSeriesAndMonth) {
    if (listOfSeriesAndMonth.isEmpty()) { //au demarrage il n'y a pas de CurrentMonth.
      return;
    }

    Glob currentMonth = repository.get(CurrentMonth.KEY);
    final Integer currentMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    for (Pair<Integer, Integer> seriesAndMonth : listOfSeriesAndMonth) {
      final Integer monthId = seriesAndMonth.getSecond();
      final Integer seriesId = seriesAndMonth.getFirst();

      GlobList transactions = getPlannedTransactions(repository, seriesId, monthId);
      Glob seriesBudget =
        repository
          .findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesId)
          .findByIndex(SeriesBudget.MONTH, monthId)
          .getGlobs().getFirst();
      if (seriesBudget == null) {
        repository.delete(transactions);
        continue;
      }

      Double observedAmount = seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT);
      if (Amounts.isNullOrZero(seriesBudget.get(SeriesBudget.PLANNED_AMOUNT)) || !seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
        repository.delete(transactions);
      }
      else if (monthId >= currentMonthId) {
        Double wantedAmount = seriesBudget.get(SeriesBudget.PLANNED_AMOUNT, 0);
        double diff = wantedAmount - Utils.zeroIfNull(observedAmount);
        if (((wantedAmount > 0 && diff > 0) || (wantedAmount < 0 && diff < 0)) && !Amounts.isNearZero(diff)) {
          if (repository.get(UserPreferences.KEY).isTrue(UserPreferences.MULTIPLE_PLANNED)) {
            distributePlannedAmount(repository, monthId, transactions, seriesBudget);
          }
          else {
            Glob transaction = transactions.getFirst();
            if (transaction == null) {
              Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
              if (series == null) { // on a un bug : une series a disparu on continue
                Log.write("Missing series " + seriesBudget.get(SeriesBudget.SERIES));
              }
              else {
                createPlannedTransaction(series, repository, monthId, seriesBudget.get(SeriesBudget.DAY), diff);
              }
            }
            else {
              repository.update(transaction.getKey(), Transaction.AMOUNT, diff);
              transactions.remove(0);
              repository.delete(transactions);
            }
          }
        }
        else {
          repository.delete(transactions);
        }
      }
      else {
        repository.delete(transactions);
      }
    }
  }

  private void distributePlannedAmount(GlobRepository repository, Integer monthId, GlobList transactions, Glob seriesBudget) {
    Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
    if (series == null) { // on a un bug : une series a disparu on continue
      repository.delete(transactions);
      Log.write("Missing series " + seriesBudget.get(SeriesBudget.SERIES));
    }
    else {
//      if (series.get(Series.IS_WEEKLY) ){
//
//      }


      TheoricalPlanned[] theoricalPlanneds =
        getTheoricalPlanned(series, repository, monthId, seriesBudget.get(SeriesBudget.PLANNED_AMOUNT));
      Double observedAmount = Utils.zeroIfNull(seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT));
      for (TheoricalPlanned theoricalPlanned : theoricalPlanneds) {
        Double wantedAmount = theoricalPlanned.amountForPeriod;
        double diff = wantedAmount - observedAmount;
        if (((wantedAmount > 0 && diff > 0) || (wantedAmount < 0 && diff < 0)) && !Amounts.isNearZero(diff)) {
          theoricalPlanned.amountForPeriod = diff;
          break;
        }
        else {
          observedAmount -= theoricalPlanned.amountForPeriod;
          theoricalPlanned.amountForPeriod = 0;
        }
      }

      for (TheoricalPlanned theoricalPlanned : theoricalPlanneds) {
        Glob existing = null;
        if (transactions.size() != 0) {
          existing = transactions.remove(0);
        }
        if (theoricalPlanned.amountForPeriod != 0) {
          if (existing != null) {
            repository.update(existing.getKey(), FieldValue.value(Transaction.AMOUNT, theoricalPlanned.amountForPeriod),
                              FieldValue.value(Transaction.DAY, theoricalPlanned.day),
                              FieldValue.value(Transaction.POSITION_DAY, theoricalPlanned.day),
                              FieldValue.value(Transaction.BANK_DAY, theoricalPlanned.day),
                              FieldValue.value(Transaction.BUDGET_DAY, theoricalPlanned.day));
          }
          else {
            createPlanned(series, repository, monthId, theoricalPlanned.accountId, series.get(Series.ID),
                          theoricalPlanned.day, theoricalPlanned.amountForPeriod);
          }
        }
        else {
          if (existing != null) {
            repository.delete(existing);
          }
        }
      }
      repository.delete(transactions);
    }
  }

  private static GlobList getPlannedTransactions(GlobRepository repository, Integer series, Integer month) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series)
      .findByIndex(Transaction.POSITION_MONTH, month)
      .getGlobs()
      .filter(and(isTrue(Transaction.PLANNED),
                  isNotTrue(Transaction.MIRROR)), repository)
      .sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
//    final Set<Pair<Integer, Integer>> listOfSeriesAndMonth = new HashSet<Pair<Integer, Integer>>();
//    repository.safeApply(SeriesBudget.TYPE, GlobMatchers.ALL, new GlobFunctor() {
//      public void run(Glob glob, GlobRepository repository) throws Exception {
//        listOfSeriesAndMonth.add(new Pair<Integer, Integer>(glob.get(SeriesBudget.SERIES), glob.get(SeriesBudget.MONTH)));
//      }
//    });
//    updatePlannedTransactions(repository, listOfSeriesAndMonth);
  }

  Double computeObservedAmount(GlobRepository repository, int seriesId, int monthId) {
    Glob seriesStat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
    if (seriesStat == null) { // il n'y a pas d'operations.
      return null;
    }
    return seriesStat.get(SeriesStat.ACTUAL_AMOUNT);
  }

  static class TheoricalPlanned {
    final int accountId;
    final int day;
    double amountForPeriod;

    TheoricalPlanned(int accountId, int day, double amountForPeriod) {
      this.accountId = accountId;
      this.day = day;
      this.amountForPeriod = amountForPeriod;
    }
  }

  public static TheoricalPlanned[] getTheoricalPlanned(Glob series, GlobRepository repository, int monthId, double amount) {
    final ArrayList<TheoricalPlanned> planneds = new ArrayList<TheoricalPlanned>();
    computePlanned(series, repository, monthId, amount, new DeclarePlanned() {
      public void declare(int account, int day, double amountForPeriod) {
        planneds.add(new TheoricalPlanned(account, day, amountForPeriod));
      }
    });
    return planneds.toArray(new TheoricalPlanned[planneds.size()]);
  }

  public static GlobList createPlannedTransaction(Glob series, GlobRepository repository, int monthId, double amount) {
    CreateDeclarePlanned createDeclarePlanned = new CreateDeclarePlanned(series, repository, monthId);
    computePlanned(series, repository, monthId, amount, createDeclarePlanned);
    return createDeclarePlanned.created;
  }

  private static void computePlanned(Glob series, GlobRepository repository, int monthId, double amount, final DeclarePlanned declarePlanned) {
    Integer minDay = 0;
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    if ((currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId)) {
      minDay = currentMonth.get(CurrentMonth.LAST_TRANSACTION_DAY);
    }

    int account;
    Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
    if (series.get(Series.MIRROR_SERIES) != null) {
      if (fromAccount == null || toAccount == null) {
        Log.write("Series " + series.get(Series.NAME) + " is a saving series with both accounts imported" +
                  " but one of the account is missing.");
        return;
      }
      account = series.get(Series.TARGET_ACCOUNT);
    }
    else if (fromAccount == null && toAccount == null) {
      account = Account.MAIN_SUMMARY_ACCOUNT_ID;
    }
    else {
      if (fromAccount != null && AccountType.MAIN.getId().equals(fromAccount.get(Account.ACCOUNT_TYPE))) {
        account = fromAccount.get(Account.ID);
      }
      else if (toAccount != null && AccountType.MAIN.getId().equals(toAccount.get(Account.ACCOUNT_TYPE))) {
        account = toAccount.get(Account.ID);
      }
      else {
        if (fromAccount == null) {
          account = toAccount.get(Account.ID);
        }
        else { //if (toAccount == null)
          account = fromAccount.get(Account.ID);
        }
      }
    }
    computePlanned(series, repository, monthId, amount, minDay, declarePlanned, account);
  }

  interface DeclarePlanned {
    void declare(int account, int day, double amountForPeriod);
  }

  private static void computePlanned(Glob series, GlobRepository repository, int monthId, double amount,
                                     Integer minDay, final DeclarePlanned planned, int account) {
    Integer seriesId = series.get(Series.ID);

    Integer period = repository.get(UserPreferences.KEY).get(UserPreferences.PERIOD_COUNT_FOR_PLANNED);
    Key key = Key.create(SeriesShape.TYPE, seriesId);
    Glob seriesShape = repository.find(key);
    if (seriesShape == null) {
      Integer miroirId = series.get(Series.MIRROR_SERIES);
      if (miroirId != null) {
        seriesShape = repository.find(Key.create(SeriesShape.TYPE, miroirId));
      }
    }
    if (seriesShape != null && seriesShape.get(SeriesShape.FIXED_DATE) != null) {
      planned.declare(account, Month.getDay(seriesShape.get(SeriesShape.FIXED_DATE), monthId), amount);
    }
    else {
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
        int day = SeriesShape.getDay(period, i, monthId, series.get(Series.BUDGET_AREA).equals(BudgetArea.INCOME.getId()));
        if (minDay != 0 && day < minDay) {
          int nextDay = SeriesShape.getDay(period, i + 1, monthId, series.get(Series.BUDGET_AREA).equals(BudgetArea.INCOME.getId()));
          if (nextDay < minDay && i != period) {
            percentToPropagate += percent;
          }
          else {
            double amountForPeriod = getAmount(alreadyAssignedAmount, amount, percentToPropagate + percent, i == period);
            if (!Amounts.isNearZero(amountForPeriod)) {
              alreadyAssignedAmount += amountForPeriod;
              percentToPropagate = 0;
              planned.declare(account, minDay, amountForPeriod);
            }
          }
        }
        else {
          double amountForPeriod = getAmount(alreadyAssignedAmount, amount, percentToPropagate + percent, i == period);
          if (!Amounts.isNearZero(amountForPeriod)) {
            alreadyAssignedAmount += amountForPeriod;
            percentToPropagate = 0;
            planned.declare(account, day, amountForPeriod);
          }
        }
      }
    }
  }

  private static Glob createPlanned(Glob series, GlobRepository repository, int monthId, int account, Integer seriesId, int day, double amountForPeriod) {
    return repository.create(Transaction.TYPE,
                             value(Transaction.ACCOUNT, account),
                             value(Transaction.AMOUNT, amountForPeriod),
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
                                   amountForPeriod > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()));
  }

  static double getAmount(double alreadyAssignedAmount, double totalAmount, int percent, boolean isLast) {
    if (isLast) {
      return totalAmount - alreadyAssignedAmount;
    }
    else {
      int amount = (int)((totalAmount * percent) / 100);
      if (Math.abs(amount + alreadyAssignedAmount - totalAmount) < 10) {
        return totalAmount - alreadyAssignedAmount;
      }
      return amount;
    }
  }

  private static class CreateDeclarePlanned implements DeclarePlanned {
    private final GlobList created = new GlobList();
    private final Glob series;
    private final GlobRepository repository;
    private final int monthId;
    private final Integer seriesId;

    public CreateDeclarePlanned(Glob series, GlobRepository repository, int monthId) {
      this.series = series;
      this.repository = repository;
      this.monthId = monthId;
      this.seriesId = series.get(Series.ID);
    }

    public void declare(int account, int day, double amountForPeriod) {
      created.add(createPlanned(series, repository, monthId, account, seriesId, day, amountForPeriod));
    }
  }

  public static void createPlannedTransaction(Glob series, GlobRepository repository, int monthId,
                                              Integer day, double amount) {
    Glob month = repository.get(CurrentMonth.KEY);
    if ((month.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId)
        && ((day == null) ||
            (day < month.get(CurrentMonth.LAST_TRANSACTION_DAY)))) {
      day = month.get(CurrentMonth.LAST_TRANSACTION_DAY);
    }

    Integer account;
//    Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
//    Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);
//    if (series.get(Series.MIRROR_SERIES) != null) {
//      if (fromAccount == null || toAccount == null) {
//        Log.write("Series " + series.get(Series.NAME) + " is a saving series with both accounts imported" +
//                  " but one of the account is missing.");
//        return;
//      }
    account = series.get(Series.TARGET_ACCOUNT);
    if (account == null) {
      account = Account.MAIN_SUMMARY_ACCOUNT_ID;
    }
//      if (series.isTrue(Series.IS_MIRROR)) {
//        account = fromAccount.get(Account.ID);
//      }
//      else {
//        account = toAccount.get(Account.ID);
//      }
//    }
//    else if (fromAccount == null && toAccount == null) {
//      account = Account.MAIN_SUMMARY_ACCOUNT_ID;
//    }
//    else {
//      if (fromAccount != null && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
//        account = fromAccount.get(Account.ID);
//      }
//      else if (toAccount != null && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
//        account = toAccount.get(Account.ID);
//      }
//      else {
//        if (fromAccount == null) {
//          account = toAccount.get(Account.ID);
//        }
//        else { //if (toAccount == null)
//          account = fromAccount.get(Account.ID);
//        }
//      }
//    }
    Integer seriesId = series.get(Series.ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ACCOUNT, account),
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
