package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashSet;
import java.util.Set;

public class SeriesBudgetUpdateTransactionTrigger implements ChangeSetListener {

  public SeriesBudgetUpdateTransactionTrigger() {
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        Glob series = repository.get(Key.create(Series.TYPE, values.get(SeriesBudget.SERIES)));
        if (generatesPlannedTransactions(values, series,
                                         repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH))) {
          Integer monthId = values.get(SeriesBudget.MONTH);
          createPlannedTransaction(series, repository, monthId,
                                   values.get(SeriesBudget.DAY),
                                   values.get(SeriesBudget.AMOUNT));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob seriesBudget = repository.get(key);
        if (seriesBudget.get(SeriesBudget.MONTH) < repository.get(CurrentMonth.KEY).get(CurrentMonth.LAST_TRANSACTION_MONTH)) {
          return;
        }
        Glob series = repository.get(Key.create(Series.TYPE, seriesBudget.get(SeriesBudget.SERIES)));
        if (values.contains(SeriesBudget.ACTIVE)) {
          if (values.get(SeriesBudget.ACTIVE)) {
            Integer monthId = seriesBudget.get(SeriesBudget.MONTH);
            createPlannedTransaction(series, repository, monthId,
                                     seriesBudget.get(SeriesBudget.DAY),
                                     seriesBudget.get(SeriesBudget.AMOUNT));
          }
          else {
            GlobList transactions = getPlanned(repository, repository.get(key));
            repository.delete(transactions);
          }
        }
        else if (values.contains(SeriesBudget.AMOUNT)) {
//          Double diff = values.get(SeriesBudget.AMOUNT) - values.getPrevious(SeriesBudget.AMOUNT);
          Double diff = values.getPrevious(SeriesBudget.AMOUNT) - values.get(SeriesBudget.AMOUNT);
          Glob currentMonth = repository.get(CurrentMonth.KEY);
          TransactionPlannedTrigger.transfertAmount(
            repository.get(Key.create(Series.TYPE, series.get(Series.ID))), diff,
            seriesBudget.get(SeriesBudget.MONTH),
            currentMonth.get(CurrentMonth.LAST_TRANSACTION_MONTH),
            repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList transactions = getPlanned(repository, previousValues);
        repository.delete(transactions);
      }
    });
  }

  private boolean generatesPlannedTransactions(FieldValues values, Glob series, int lastAvailableTransactionMonthId) {
    return values.get(SeriesBudget.ACTIVE) &&
           (values.get(SeriesBudget.AMOUNT) != null) &&
           (!Amounts.isNearZero(values.get(SeriesBudget.AMOUNT)) &&
            (values.get(SeriesBudget.DAY) != null) &&
            values.get(SeriesBudget.MONTH) >= lastAvailableTransactionMonthId
            && !series.get(Series.ID).equals(Series.UNCATEGORIZED_SERIES_ID));
  }

  private GlobList getPlanned(GlobRepository repository, FieldValues seriesBudget) {
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesBudget.get(SeriesBudget.SERIES))
      .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
      .filterSelf(GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.PLANNED, true),
                                   GlobMatchers.ALL
//                                   GlobMatchers.fieldEquals(Transaction.MIRROR, false)
      ),
                  repository)
      .sort(Transaction.DAY);
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

    if (series.get(Series.MIROR_SERIES) != null) {
      account = toAccount.get(Account.ID);
    }
    else if (series.get(Series.IS_MIROR)) {
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
                      value(Transaction.ACCOUNT, account),
                      value(Transaction.AMOUNT, amount),
                      value(Transaction.SERIES, seriesId),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.BANK_DAY, day),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.DAY, day),
                      value(Transaction.LABEL, Series.getPlannedTransactionLabel(series.get(Series.ID), series)),
                      value(Transaction.PLANNED, true),
                      value(Transaction.TRANSACTION_TYPE,
                            amount > 0 ? TransactionType.VIREMENT.getId() : TransactionType.PRELEVEMENT.getId()),
                      value(Transaction.CATEGORY, categoryId));
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

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
