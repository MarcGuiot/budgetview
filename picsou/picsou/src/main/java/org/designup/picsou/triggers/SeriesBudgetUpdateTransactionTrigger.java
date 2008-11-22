package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
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
                                         repository.get(CurrentMonth.KEY).get(CurrentMonth.MONTH_ID))) {
          Integer monthId = values.get(SeriesBudget.MONTH);
          createPlannedTransaction(series, repository, monthId,
                                   values.get(SeriesBudget.DAY),
                                   values.get(SeriesBudget.AMOUNT));
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        Glob seriesBudget = repository.get(key);
        if (seriesBudget.get(SeriesBudget.MONTH) < repository.get(CurrentMonth.KEY).get(CurrentMonth.MONTH_ID)) {
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
            GlobList transactions = getPlannedTransactions(key, repository);
            repository.delete(transactions);
          }
        }
        else if (values.contains(SeriesBudget.AMOUNT)) {
          Double diff = values.getPrevious(SeriesBudget.AMOUNT) - values.get(SeriesBudget.AMOUNT);
          Glob currentMonth = repository.get(CurrentMonth.KEY);
          TransactionPlannedTrigger.transfertAmount(
            repository.get(Key.create(Series.TYPE, series.get(Series.ID))), diff,
            seriesBudget.get(SeriesBudget.MONTH),
            currentMonth.get(CurrentMonth.MONTH_ID),
            repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
        GlobList transactions =
          repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, previousValues.get(SeriesBudget.SERIES))
            .findByIndex(Transaction.MONTH, previousValues.get(SeriesBudget.MONTH)).getGlobs()
            .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, true), repository)
            .sort(Transaction.DAY); //??
        repository.delete(transactions);
      }
    });
  }

  private boolean generatesPlannedTransactions(FieldValues values, Glob series, int lastAvailableTransactionMonthId) {
    return values.get(SeriesBudget.ACTIVE) &&
           (values.get(SeriesBudget.AMOUNT) != null) &&
           (Math.abs(values.get(SeriesBudget.AMOUNT)) != 0.0) &&
           (values.get(SeriesBudget.DAY) != null) &&
           values.get(SeriesBudget.MONTH) >= lastAvailableTransactionMonthId
           && !series.get(Series.ID).equals(Series.UNCATEGORIZED_SERIES_ID);
  }

  private GlobList getPlannedTransactions(Key key, GlobRepository repository) {
    Glob seriesBudget = repository.get(key);
    return repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, seriesBudget.get(SeriesBudget.SERIES))
      .findByIndex(Transaction.MONTH, seriesBudget.get(SeriesBudget.MONTH)).getGlobs()
      .filterSelf(GlobMatchers.fieldEquals(Transaction.PLANNED, true), repository)
      .sort(Transaction.DAY);
  }

  public static void createPlannedTransaction(Glob series, GlobRepository repository, int monthId,
                                              Integer day, Double amount) {
    Glob month = repository.get(CurrentMonth.KEY);
    if (month.get(CurrentMonth.MONTH_ID) == monthId && (day == null || day < month.get(CurrentMonth.DAY))) {
      day = month.get(CurrentMonth.DAY);
    }
    Integer categoryId = getCategory(series, repository);
    Integer seriesId = series.get(Series.ID);
    repository.create(Transaction.TYPE,
                      value(Transaction.ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
                      value(Transaction.AMOUNT, amount),
                      value(Transaction.SERIES, seriesId),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.BANK_DAY, day),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.DAY, day),
                      value(Transaction.LABEL, Series.getPlannedTransactionLabel(series.get(Series.ID), series)),
                      value(Transaction.PLANNED, true),
                      value(Transaction.TRANSACTION_TYPE, TransactionType.PLANNED.getId()),
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
