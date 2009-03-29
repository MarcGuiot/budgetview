package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Pair;

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
        Integer monthId = values.get(Transaction.MONTH);
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
        if (values.contains(Transaction.MONTH)) {
          previousMonth = values.getPrevious(Transaction.MONTH);
          newMonth = values.get(Transaction.MONTH);
        }
        else {
          newMonth = transaction.get(Transaction.MONTH);
          previousMonth = newMonth;
        }
        if (values.contains(Transaction.AMOUNT) || newSeries != previousSeries ||
            newMonth != previousMonth) {
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
        Integer monthId = previousValues.get(Transaction.MONTH);
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
    updatePlannedTransaction(repository, listOfSeriesAndMonth);
  }

  private void updatePlannedTransaction(GlobRepository repository, Set<Pair<Integer, Integer>> listOfSeriesAndMonth) {
    if (listOfSeriesAndMonth.isEmpty()) { //au demmarage il n'y a pas de CurrentMonth.
      return;
    }
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    final Integer currentMonthId = currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH);
    for (Pair<Integer, Integer> seriesAndMonth : listOfSeriesAndMonth) {
      Double observedAmount = computeObservedAmount(repository, seriesAndMonth.getFirst(), seriesAndMonth.getSecond());
      GlobList transactions = getPlannedTransactions(repository, seriesAndMonth.getFirst(), seriesAndMonth.getSecond());
      Glob seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, seriesAndMonth.getFirst())
        .findByIndex(SeriesBudget.MONTH, seriesAndMonth.getSecond()).getGlobs().getFirst();

      if (seriesBudget == null) {
        repository.delete(transactions);
        continue;
      }

      repository.update(seriesBudget.getKey(), SeriesBudget.OBSERVED_AMOUNT, observedAmount);
      if (Amounts.isNearZero(seriesBudget.get(SeriesBudget.AMOUNT)) || !seriesBudget.get(SeriesBudget.ACTIVE)) {
        repository.delete(transactions);
      }
      else if (seriesAndMonth.getSecond() >= currentMonthId) {
        Double wantedAmount = seriesBudget.get(SeriesBudget.AMOUNT);
        double diff = wantedAmount - observedAmount;
        if (wantedAmount > 0 && diff > 0 || wantedAmount < 0 && diff < 0 && !Amounts.isNearZero(diff)) {
          Glob transaction = transactions.getFirst();
          if (transaction == null) {
            Glob series = repository.findLinkTarget(seriesBudget, SeriesBudget.SERIES);
            createPlannedTransaction(series, repository,
                                     seriesAndMonth.getSecond(),
                                     seriesBudget.get(SeriesBudget.DAY),
                                     diff);
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
    return repository.getAll(Transaction.TYPE,
                             GlobMatchers.and(
                               GlobMatchers.fieldEquals(Transaction.SERIES, series),
                               GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                               GlobMatchers.not(
                                 GlobMatchers.fieldEquals(Transaction.MIRROR, true)),
                               GlobMatchers.fieldEquals(Transaction.MONTH, month)))
      .sort(Transaction.DAY);
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    final Set<Pair<Integer, Integer>> listOfSeriesAndMonth = new HashSet<Pair<Integer, Integer>>();
    repository.safeApply(SeriesBudget.TYPE, GlobMatchers.ALL, new GlobFunctor() {
      public void run(Glob glob, GlobRepository repository) throws Exception {
        listOfSeriesAndMonth.add(new Pair<Integer, Integer>(glob.get(SeriesBudget.SERIES), glob.get(SeriesBudget.MONTH)));
      }
    });
    updatePlannedTransaction(repository, listOfSeriesAndMonth);
  }


  Double computeObservedAmount(GlobRepository repository, int seriesId, int monthId) {
    try {
      ComputeObservedFunctor computeObservedFunctor = new ComputeObservedFunctor();
      repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
        .findByIndex(Transaction.MONTH, monthId).callOnGlobs(computeObservedFunctor, repository);
      return computeObservedFunctor.amount;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void createPlannedTransaction(Glob series, GlobRepository repository, int monthId,
                                              Integer day, Double amount) {
    Glob month = repository.get(CurrentMonth.KEY);
    if (month.get(CurrentMonth.LAST_TRANSACTION_MONTH) == monthId && (day == null || day < month.get(CurrentMonth.LAST_TRANSACTION_DAY))) {
      day = month.get(CurrentMonth.LAST_TRANSACTION_DAY);
    }
    int account;
    Glob fromAccount = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
    Glob toAccount = repository.findLinkTarget(series, Series.TO_ACCOUNT);

    if (series.get(Series.MIRROR_SERIES) != null && !series.get(Series.IS_MIRROR)) {
      account = toAccount.get(Account.ID);
    }
    else if (series.get(Series.IS_MIRROR)) {
      account = fromAccount.get(Account.ID);
    }
    else if (fromAccount == null && toAccount == null) {
      account = Account.MAIN_SUMMARY_ACCOUNT_ID;
    }
    else {
      if (fromAccount != null && Account.MAIN_SUMMARY_ACCOUNT_ID == fromAccount.get(Account.ID)) {
        account = fromAccount.get(Account.ID);
      }
      else if (toAccount != null && Account.MAIN_SUMMARY_ACCOUNT_ID == toAccount.get(Account.ID)) {
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
    Integer categoryId = getCategory(series, repository);
    Integer seriesId = series.get(Series.ID);
    repository.create(Transaction.TYPE,
                      org.globsframework.model.FieldValue.value(Transaction.ACCOUNT, account),
                      org.globsframework.model.FieldValue.value(Transaction.AMOUNT, amount),
                      org.globsframework.model.FieldValue.value(Transaction.SERIES, seriesId),
                      org.globsframework.model.FieldValue.value(Transaction.BANK_MONTH, monthId),
                      org.globsframework.model.FieldValue.value(Transaction.BANK_DAY, day),
                      org.globsframework.model.FieldValue.value(Transaction.MONTH, monthId),
                      org.globsframework.model.FieldValue.value(Transaction.DAY, day),
                      org.globsframework.model.FieldValue.value(Transaction.LABEL, Series.getPlannedTransactionLabel(series.get(Series.ID), series)),
                      org.globsframework.model.FieldValue.value(Transaction.PLANNED, true),
                      org.globsframework.model.FieldValue.value(Transaction.TRANSACTION_TYPE,
                                                                amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()),
                      org.globsframework.model.FieldValue.value(Transaction.CATEGORY, categoryId));
  }

  public static Integer getCategory(Glob series, GlobRepository repository) {
    Integer seriesId = series.get(Series.ID);
    Integer categoryId = series.get(Series.DEFAULT_CATEGORY);
    GlobList seriesToCategory = repository.getAll(SeriesToCategory.TYPE, GlobMatchers.fieldEquals(SeriesToCategory.SERIES, seriesId));
    if (seriesToCategory.size() == 1) {
      categoryId = seriesToCategory.get(0).get(SeriesToCategory.CATEGORY);
    }
    else if (seriesToCategory.size() != 0) {
      Set<Integer> categories = new HashSet<Integer>();
      for (Glob glob : seriesToCategory) {
        Glob category = repository.findLinkTarget(glob, SeriesToCategory.CATEGORY);
        categories.add(category.get(Category.MASTER) == null ?
                       category.get(Category.ID) : category.get(Category.MASTER));
      }
      if (categories.size() == 1) {
        categoryId = categories.iterator().next();
      }
      else if (categories.size() > 1) {
        categoryId = MasterCategory.NONE.getId();
      }
    }
    return categoryId;
  }

  private static class ComputeObservedFunctor implements GlobFunctor {
    public double amount = 0;

    public void run(Glob glob, GlobRepository repository) throws Exception {
      if (!glob.get(Transaction.PLANNED) && !glob.get(Transaction.MIRROR)) {
        amount += glob.get(Transaction.AMOUNT);
      }
    }
  }
}
