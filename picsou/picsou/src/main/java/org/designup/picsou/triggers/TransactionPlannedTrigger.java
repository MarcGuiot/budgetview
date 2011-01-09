package org.designup.picsou.triggers;

import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.utils.Log;
import org.globsframework.utils.Pair;
import org.globsframework.utils.Utils;

import java.util.HashSet;
import java.util.Set;

public class TransactionPlannedTrigger implements ChangeSetListener {

  public TransactionPlannedTrigger() {
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    final Set<Pair<Integer, Integer>> listOfSeriesAndMonth = new HashSet<Pair<Integer, Integer>>();

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
        if (values.contains(SeriesBudget.AMOUNT)
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

    updatePlannedTransactions(repository, listOfSeriesAndMonth);
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

      Double observedAmount = computeObservedAmount(repository, seriesId, monthId);
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

      repository.update(seriesBudget.getKey(), SeriesBudget.OBSERVED_AMOUNT, observedAmount);
      if (Amounts.isNullOrZero(seriesBudget.get(SeriesBudget.AMOUNT)) || !seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
        repository.delete(transactions);
      }
      else if (monthId >= currentMonthId) {
        Double wantedAmount = seriesBudget.get(SeriesBudget.AMOUNT, 0);
        double diff = wantedAmount - Utils.zeroIfNull(observedAmount);
        if (((wantedAmount > 0 && diff > 0) || (wantedAmount < 0 && diff < 0)) && !Amounts.isNearZero(diff)) {
          Glob transaction = transactions.getFirst();
          if (transaction == null) {
            Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
            if (series == null) { // on a un bug : une series a disparu on continue
              Log.write("Missing series " + seriesBudget.get(SeriesBudget.SERIES));
            }
            else {
              createPlannedTransaction(series, repository,
                                       monthId,
                                       seriesBudget.get(SeriesBudget.DAY),
                                       diff);
            }
          }
          else {
            repository.update(transaction.getKey(), Transaction.AMOUNT, diff);
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

  private static GlobList getPlannedTransactions(GlobRepository repository, Integer series, Integer month) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series)
      .findByIndex(Transaction.POSITION_MONTH, month)
      .getGlobs()
      .filter(and(isTrue(Transaction.PLANNED),
                  not(isTrue(Transaction.MIRROR))), repository)
      .sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    final Set<Pair<Integer, Integer>> listOfSeriesAndMonth = new HashSet<Pair<Integer, Integer>>();
    repository.safeApply(SeriesBudget.TYPE, GlobMatchers.ALL, new GlobFunctor() {
      public void run(Glob glob, GlobRepository repository) throws Exception {
        listOfSeriesAndMonth.add(new Pair<Integer, Integer>(glob.get(SeriesBudget.SERIES), glob.get(SeriesBudget.MONTH)));
      }
    });
    updatePlannedTransactions(repository, listOfSeriesAndMonth);
  }

  Double computeObservedAmount(GlobRepository repository, int seriesId, int monthId) {
    Glob seriesStat = repository.find(Key.create(SeriesStat.SERIES, seriesId, SeriesStat.MONTH, monthId));
    if (seriesStat == null) { // il n'y a pas d'operations.
      return null;
    }
    return seriesStat.get(SeriesStat.AMOUNT);
  }

  public static void createPlannedTransaction(Glob series, GlobRepository repository, int monthId,
                                              Integer day, double amount) {
    Glob month = repository.get(CurrentMonth.KEY);
    if ((month.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId)
        && ((day == null) ||
            (day < month.get(CurrentMonth.LAST_TRANSACTION_DAY)))) {
      day = month.get(CurrentMonth.LAST_TRANSACTION_DAY);
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
      if (series.isTrue(Series.IS_MIRROR)) {
        account = fromAccount.get(Account.ID);
      }
      else {
        account = toAccount.get(Account.ID);
      }
    }
    else if (fromAccount == null && toAccount == null) {
      account = Account.MAIN_SUMMARY_ACCOUNT_ID;
    }
    else {
      if (fromAccount != null && fromAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
        account = fromAccount.get(Account.ID);
      }
      else if (toAccount != null && toAccount.get(Account.ACCOUNT_TYPE).equals(AccountType.MAIN.getId())) {
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
