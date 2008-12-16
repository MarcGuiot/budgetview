package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;

import java.util.*;

public class NotImportedTransactionAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    HashMap<Integer, List<Integer>> seriesToNotImportedAccount = new HashMap<Integer, List<Integer>>();

    deleteAndReCreateTransactionOnTargetOrSourceAccountChange(changeSet, repository, seriesToNotImportedAccount);
    updateFromCurrentMonthChange(changeSet, repository, seriesToNotImportedAccount);
    updateFromSeriesBudgetChange(changeSet, repository, seriesToNotImportedAccount);
  }

  private void deleteAndReCreateTransactionOnTargetOrSourceAccountChange(final ChangeSet changeSet, final GlobRepository repository,
                                                                         final Map<Integer, List<Integer>> seriesToNotImportedAccount) {
    changeSet.safeVisit(Series.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Series.FROM_ACCOUNT) || values.contains(Series.TO_ACCOUNT)) {
          Glob series = repository.get(key);
          // il faut detruire toutes les transactions : si soit TO soit FROM change il faut aussi detuire l'autre
          if (values.contains(Series.FROM_ACCOUNT)) {
            Integer previousFrom = values.getPrevious(Series.FROM_ACCOUNT);
            if (previousFrom != null) {
              deleteTransactionsIfNotImported(key, previousFrom, repository);
            }
          }
          Integer newFrom = series.get(Series.FROM_ACCOUNT);
          if (newFrom != null) {
            deleteTransactionsIfNotImported(key, newFrom, repository);
          }
          if (values.contains(Series.TO_ACCOUNT)) {
            Integer previousTo = values.getPrevious(Series.TO_ACCOUNT);
            if (previousTo != null) {
              deleteTransactionsIfNotImported(key, previousTo, repository);
            }
          }

          Integer newTo = series.get(Series.TO_ACCOUNT);
          if (newTo != null) {
            deleteTransactionsIfNotImported(key, newTo, repository);
          }

          if (!Account.areNoneImported(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                       repository.findLinkTarget(series, Series.TO_ACCOUNT))) {
            return;
          }
          GlobList seriesBudgets = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, key.get(Series.ID)).getGlobs();
          for (Glob seriesBudget : seriesBudgets) {
            if (!changeSet.isCreated(seriesBudget.getKey())) {
              createTransactionFromSeriesBudget(seriesBudget, repository, seriesToNotImportedAccount);
            }
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        repository.delete(repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, key.get(Series.ID))
          .getGlobs().filterSelf(GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, true), repository));
      }
    });
  }

  private void deleteTransactionsIfNotImported(Key key, Integer previousAccountId, GlobRepository repository) {
    if (previousAccountId != null) {
      GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES,
                                                     key.get(Series.ID)).getGlobs();
      repository.delete(transactions.filter(GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.ACCOUNT, previousAccountId),
                                                             GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, true)),
                                            repository));
    }
  }

  private void updateFromCurrentMonthChange(ChangeSet changeSet, GlobRepository repository,
                                            HashMap<Integer, List<Integer>> seriesToNotImportedAccount) {
    if (changeSet.containsChanges(CurrentMonth.KEY, CurrentMonth.CURRENT_MONTH)
        || changeSet.containsChanges(CurrentMonth.KEY, CurrentMonth.CURRENT_DAY)) {
      GlobList series = repository.getAll(Series.TYPE);
      for (Glob oneSeries : series) {
        Integer seriesId = oneSeries.get(Series.ID);
        if (!seriesToNotImportedAccount.containsKey(seriesId)) {
          updateCache(seriesId, repository, seriesToNotImportedAccount, oneSeries);
        }
        List<Integer> accountIds = seriesToNotImportedAccount.get(seriesId);
        if (accountIds.isEmpty()) {
          continue;
        }
        GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
          .getGlobs();
        Glob currentMonth = repository.get(CurrentMonth.KEY);
        Integer currentMonthId = currentMonth.get(CurrentMonth.CURRENT_MONTH);
        Integer currentDay = currentMonth.get(CurrentMonth.CURRENT_DAY);
        for (Glob transaction : transactions) {
          boolean isPlanned = (transaction.get(Transaction.MONTH) >= currentMonthId) &&
                              ((transaction.get(Transaction.MONTH) > currentMonthId)
                               || (transaction.get(Transaction.DAY) > currentDay));

          repository.update(transaction.getKey(), Transaction.PLANNED, isPlanned);
        }
      }
    }
  }

  private void updateFromSeriesBudgetChange(final ChangeSet changeSet, final GlobRepository repository,
                                            final Map<Integer, List<Integer>> seriesToNotImportedAccount) {

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues seriesBudget) throws Exception {
        createTransactionFromSeriesBudget(seriesBudget, repository, seriesToNotImportedAccount);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob seriesBudget = repository.get(key);
        Integer seriesId = seriesBudget.get(SeriesBudget.SERIES);
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        if (!seriesToNotImportedAccount.containsKey(seriesId)) {
          updateCache(seriesId, repository, seriesToNotImportedAccount, series);
        }
        List<Integer> accountIds = seriesToNotImportedAccount.get(seriesId);
        if (accountIds.isEmpty()) {
          return;
        }
        Glob currentMonth = repository.get(CurrentMonth.KEY);
        for (Integer accountId : accountIds) {
          Integer currentDay = currentMonth.get(CurrentMonth.CURRENT_DAY);
          if (values.contains(SeriesBudget.ACTIVE)) {
            if (values.get(SeriesBudget.ACTIVE)) {
              TransactionUtils.createTransactionForNotImportedAccount(seriesBudget, series, accountId,
                                                                      currentMonth.get(CurrentMonth.CURRENT_MONTH),
                                                                      currentDay,
                                                                      repository);

            }
            else {
              GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
                .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs();
              repository.delete(transactions.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId),
                                                    repository));
            }
          }
          if (values.contains(SeriesBudget.DAY)) {
            if (seriesBudget.get(SeriesBudget.MONTH).equals(currentMonth.get(CurrentMonth.CURRENT_MONTH))) {
              int newDay = values.get(SeriesBudget.DAY);
              int previousDay = values.getPrevious(SeriesBudget.DAY);
              GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
                .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
                .filterSelf(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId), repository);

              Boolean isPlanned = null;
              if (newDay > currentDay && previousDay < currentDay) {
                isPlanned = true;
              }
              if (newDay < currentDay && previousDay > currentDay) {
                isPlanned = false;
              }
              for (Glob transaction : transactions) {
                repository.update(transaction.getKey(), FieldValue.value(Transaction.DAY, newDay),
                                  FieldValue.value(Transaction.PLANNED,
                                                   isPlanned != null ? false : transaction.get(Transaction.PLANNED)));
              }
            }
            else {
              GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
                .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
                .filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId), repository);
              for (Glob transaction : transactions) {
                repository.update(transaction.getKey(), Transaction.DAY, seriesBudget.get(SeriesBudget.DAY));
              }
            }
          }
          if (values.contains(SeriesBudget.AMOUNT)) {
            GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
              .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
              .filterSelf(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId), repository);
            for (Glob transaction : transactions) {
              repository.update(transaction.getKey(), Transaction.AMOUNT, values.get(SeriesBudget.AMOUNT));
            }
          }
        }

      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        Integer seriesId = previousValues.get(SeriesBudget.SERIES);
        Glob series = repository.find(Key.create(Series.TYPE, seriesId));
        if (series == null) {
          return;
        }
        if (!seriesToNotImportedAccount.containsKey(seriesId)) {
          updateCache(seriesId, repository, seriesToNotImportedAccount,
                      series);
        }
        List<Integer> accountIds = seriesToNotImportedAccount.get(seriesId);
        if (accountIds.isEmpty()) {
          return;
        }
        for (Integer accountId : accountIds) {
          GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
            .findByIndex(Transaction.MONTH, previousValues.get(SeriesBudget.MONTH)).getGlobs()
            .filterSelf(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId), repository);
          repository.delete(transactions);
        }
      }
    });
  }

  private void createTransactionFromSeriesBudget(FieldValues seriesBudget, GlobRepository repository, Map<Integer, List<Integer>> seriesToIsImportedAccount) {
    Integer seriesId = seriesBudget.get(SeriesBudget.SERIES);
    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    if (!Account.areNoneImported(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                 repository.findLinkTarget(series, Series.TO_ACCOUNT))) {
      return;
    }
    if (!seriesToIsImportedAccount.containsKey(seriesId)) {
      updateCache(seriesId, repository, seriesToIsImportedAccount, series);
    }
    List<Integer> accountIds = seriesToIsImportedAccount.get(seriesId);
    if (accountIds.isEmpty()) {
      return;
    }
    Glob currentMonth = repository.get(CurrentMonth.KEY);
    GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
      .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs();
    for (Integer accountId : accountIds) {
      GlobList transactionForAccount =
        transactions.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId), repository);
      if (transactionForAccount.isEmpty()) {
        TransactionUtils.createTransactionForNotImportedAccount(
          seriesBudget, series, accountId, currentMonth.get(CurrentMonth.CURRENT_MONTH),
          currentMonth.get(CurrentMonth.CURRENT_DAY),
          repository);
      }
      transactions.filterSelf(GlobMatchers.not(GlobMatchers.fieldEquals(Transaction.ACCOUNT, accountId)), repository);
    }
    transactions.filterSelf(GlobMatchers.or(GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, true),
                                            GlobMatchers.fieldEquals(Transaction.MIRROR, true)), repository);
    for (Glob transaction : transactions) {
      repository.delete(transaction.getKey());
    }
  }

  private void updateCache(Integer seriesId, GlobRepository repository,
                           Map<Integer, List<Integer>> seriesToCreateTransaction, final Glob series) {
    List<Integer> accountIds = seriesToCreateTransaction.get(seriesId);
    if (accountIds == null) {
      accountIds = new ArrayList<Integer>();
      seriesToCreateTransaction.put(seriesId, accountIds);
    }
    Integer fromAccountId = series.get(Series.FROM_ACCOUNT);
    Integer toAccountId = series.get(Series.TO_ACCOUNT);
    if (fromAccountId == null && toAccountId == null) {
      return;
    }
    if (fromAccountId == null || toAccountId == null) {
      addIsNotImported(repository, accountIds, fromAccountId);
      addIsNotImported(repository, accountIds, toAccountId);
    }
    else {
//      throw new RuntimeException("movement between two not imported account to be tested");
    }
  }

  private void addIsNotImported(GlobRepository repository, List<Integer> accountIds, Integer fromAccountId) {
    if (fromAccountId != null) {
      Glob account = repository.find(Key.create(Account.TYPE, fromAccountId));
      if (!account.get(Account.IS_IMPORTED_ACCOUNT)) { // checker si c'est un vrai comptes?
        accountIds.add(fromAccountId);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
