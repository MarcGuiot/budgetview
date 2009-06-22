package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashSet;
import java.util.Set;

public class NotImportedTransactionAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {

    if (changeSet.containsChanges(Series.TYPE)
        || changeSet.containsChanges(SeriesBudget.TYPE)
        || changeSet.containsChanges(CurrentMonth.TYPE)) {
      deleteAndReCreateTransactionOnTargetOrSourceAccountChange(changeSet, repository);
      updateFromCurrentMonthChange(changeSet, repository);
      updateFromSeriesBudgetChange(changeSet, repository);
    }
  }

  private void deleteAndReCreateTransactionOnTargetOrSourceAccountChange(final ChangeSet changeSet, final GlobRepository repository) {
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
              createTransactionFromSeriesBudget(seriesBudget, repository);
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

  private void updateFromCurrentMonthChange(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(CurrentMonth.KEY, CurrentMonth.CURRENT_MONTH)
        || changeSet.containsChanges(CurrentMonth.KEY, CurrentMonth.CURRENT_DAY)) {
      GlobList series = repository.getAll(Series.TYPE);
      for (Glob oneSeries : series) {
        Integer seriesId = oneSeries.get(Series.ID);
        if (!Account.areNoneImported(repository.findLinkTarget(oneSeries, Series.FROM_ACCOUNT),
                                     repository.findLinkTarget(oneSeries, Series.TO_ACCOUNT))) {
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

          if (transaction.get(Transaction.PLANNED) != isPlanned) {
            repository.update(transaction.getKey(),
                              value(Transaction.PLANNED, isPlanned),
                              value(Transaction.LABEL, Transaction.getLabel(isPlanned, oneSeries)));
          }
        }
      }
    }
  }


  private void updateFromSeriesBudgetChange(final ChangeSet changeSet, final GlobRepository repository) {

    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues seriesBudget) throws Exception {
        createTransactionFromSeriesBudget(seriesBudget, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob seriesBudget = repository.get(key);
        Integer seriesId = seriesBudget.get(SeriesBudget.SERIES);
        Glob series = repository.get(Key.create(Series.TYPE, seriesId));
        if (!Account.areNoneImported(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                     repository.findLinkTarget(series, Series.TO_ACCOUNT))) {
          return;
        }
        Glob currentMonth = repository.get(CurrentMonth.KEY);
        {
          Integer currentDay = currentMonth.get(CurrentMonth.CURRENT_DAY);
          if (values.contains(SeriesBudget.ACTIVE)) {
            if (values.get(SeriesBudget.ACTIVE)) {
              createTransactionFromSeriesBudget(seriesBudget, repository);
            }
            else {
              deleteTransaction(seriesBudget, seriesId, series, repository);
            }
          }
          Set<Integer> accounts = new HashSet<Integer>();
          if (series.get(Series.FROM_ACCOUNT) != null) {
            accounts.add(series.get(Series.FROM_ACCOUNT));
          }
          if (series.get(Series.TO_ACCOUNT) != null) {
            accounts.add(series.get(Series.TO_ACCOUNT));
          }
          if (values.contains(SeriesBudget.DAY)) {
            if (seriesBudget.get(SeriesBudget.MONTH).equals(currentMonth.get(CurrentMonth.CURRENT_MONTH))) {
              int newDay = values.get(SeriesBudget.DAY);
              int previousDay = values.getPrevious(SeriesBudget.DAY);

              GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
                .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
                .filterSelf(GlobMatchers.fieldIn(Transaction.ACCOUNT, accounts), repository);

              Boolean isPlanned = null;
              if (newDay > currentDay && previousDay < currentDay) {
                isPlanned = true;
              }
              if (newDay < currentDay && previousDay > currentDay) {
                isPlanned = false;
              }
              for (Glob transaction : transactions) {
                repository.update(transaction.getKey(), value(Transaction.DAY, newDay),
                                  value(Transaction.PLANNED,
                                                   isPlanned != null ? false : transaction.get(Transaction.PLANNED)));
              }
            }
            else {
              GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
                .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
                .filter(GlobMatchers.fieldIn(Transaction.ACCOUNT, accounts), repository);
              for (Glob transaction : transactions) {
                repository.update(transaction.getKey(), Transaction.DAY, seriesBudget.get(SeriesBudget.DAY));
              }
            }
          }
          if (values.contains(SeriesBudget.AMOUNT)) {
            GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
              .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
              .filterSelf(GlobMatchers.fieldIn(Transaction.ACCOUNT, accounts), repository);
            for (Glob transaction : transactions) {
              if (transaction.get(Transaction.MIRROR)) {
                repository.update(transaction.getKey(), Transaction.AMOUNT, -values.get(SeriesBudget.AMOUNT));
              }
              else {
                repository.update(transaction.getKey(), Transaction.AMOUNT, values.get(SeriesBudget.AMOUNT));
              }
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
        Set<Integer> accounts = new HashSet<Integer>();
        if (series.get(Series.FROM_ACCOUNT) != null) {
          accounts.add(series.get(Series.FROM_ACCOUNT));
        }
        if (series.get(Series.TO_ACCOUNT) != null) {
          accounts.add(series.get(Series.TO_ACCOUNT));
        }
        GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
          .findByIndex(Transaction.MONTH, previousValues.get(SeriesBudget.MONTH)).getGlobs()
          .filterSelf(GlobMatchers.fieldIn(Transaction.ACCOUNT, accounts), repository);
        repository.delete(transactions);
      }
    });
  }

  private void deleteTransaction(Glob seriesBudget, Integer seriesId, Glob series, GlobRepository repository) {
    GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
      .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs();
    Integer fromAccount = series.get(Series.FROM_ACCOUNT);
    Integer toAccount = series.get(Series.TO_ACCOUNT);
    if (fromAccount != null) {
      repository.delete(transactions.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, fromAccount), repository));
    }
    if (toAccount != null) {
      repository.delete(transactions.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, toAccount), repository));
    }
  }

  private void createTransactionFromSeriesBudget(FieldValues seriesBudget, GlobRepository repository) {
    Integer seriesId = seriesBudget.get(SeriesBudget.SERIES);
    Glob series = repository.get(Key.create(Series.TYPE, seriesId));
    if (!Account.areNoneImported(repository.findLinkTarget(series, Series.FROM_ACCOUNT),
                                 repository.findLinkTarget(series, Series.TO_ACCOUNT))) {
      return;
    }
    Integer fromAccountId = series.get(Series.FROM_ACCOUNT);
    Integer toAccountId = series.get(Series.TO_ACCOUNT);

    Glob currentMonth = repository.get(CurrentMonth.KEY);
    GlobList transactions = repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesId)
      .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs();

    Glob transaction = null;
    if (fromAccountId != null) {
      GlobList transactionForAccount =
        transactions.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, fromAccountId), repository);
      repository.delete(transactionForAccount);
      transaction = TransactionUtils.createTransactionForNotImportedAccount(
        seriesBudget, series, fromAccountId, currentMonth.get(CurrentMonth.CURRENT_MONTH),
        currentMonth.get(CurrentMonth.CURRENT_DAY),
        repository);
      if (transaction != null && transaction.get(Transaction.AMOUNT) > 0) {
        throw new RuntimeException("Bug");
      }
    }
    if (toAccountId != null) {
      GlobList transactionForAccount =
        transactions.filter(GlobMatchers.fieldEquals(Transaction.ACCOUNT, toAccountId), repository);
      repository.delete(transactionForAccount);
      if (fromAccountId != null) {
        // si le budget n'est pas actif ou si le montant est a 0 on n'a pas de transaction
        if (transaction != null) {
          TransactionUtils.createMirrorTransaction(transaction.getKey(), transaction,
                                                   toAccountId, repository);
        }
      }
      else {
        TransactionUtils.createTransactionForNotImportedAccount(
          seriesBudget, series, toAccountId, currentMonth.get(CurrentMonth.CURRENT_MONTH),
          currentMonth.get(CurrentMonth.CURRENT_DAY),
          repository);
      }
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
