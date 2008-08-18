package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class SeriesBudgetUpdateTransactionTrigger implements ChangeSetListener {

  public SeriesBudgetUpdateTransactionTrigger() {
  }

  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    repository.enterBulkDispatchingMode();
    try {
      changeSet.safeVisit(SeriesBudget.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          Glob series = repository.get(Key.create(Series.TYPE, values.get(SeriesBudget.SERIES)));
          if (generatesPlannedTransactions(values, series)) {
            Integer monthId = values.get(SeriesBudget.MONTH);
            createPlannedTransaction(series, repository, monthId,
                                     values.get(SeriesBudget.DAY),
                                     values.get(SeriesBudget.AMOUNT));
          }
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          Glob seriesBudget = repository.get(key);
          Glob series = repository.get(Key.create(Series.TYPE, seriesBudget.get(SeriesBudget.SERIES)));
          if (BudgetArea.OCCASIONAL_EXPENSES.getId().equals(series.get(Series.BUDGET_AREA))) {
            return;
          }
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
            GlobList transactions = getPlannedTransactions(key, repository);
            Glob last = transactions.getLast();
            repository.update(last.getKey(), Transaction.AMOUNT, last.get(Transaction.AMOUNT) - diff);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
          GlobList transactions = repository.findByIndex(Transaction.MONTH_INDEX, previousValues.get(SeriesBudget.MONTH))
            .filterSelf(GlobMatchers.and(
              GlobMatchers.fieldEquals(Transaction.PLANNED, true),
              GlobMatchers.fieldEquals(Transaction.SERIES, previousValues.get(SeriesBudget.SERIES))),
                        repository).sort(Transaction.DAY);
          repository.delete(transactions);
        }
      });
    }
    finally {
      repository.completeBulkDispatchingMode();
    }
  }

  private boolean generatesPlannedTransactions(FieldValues values, Glob series) {
    System.out.println("series = " + series);
    return values.get(SeriesBudget.ACTIVE) &&
           (values.get(SeriesBudget.AMOUNT) != null) &&
           (Math.abs(values.get(SeriesBudget.AMOUNT)) != 0.0) &&
           (values.get(SeriesBudget.DAY) != null) &&
           !BudgetArea.OCCASIONAL_EXPENSES.getId().equals(series.get(Series.BUDGET_AREA));
  }

  private GlobList getPlannedTransactions(Key key, GlobRepository repository) {
    Glob seriesBudget = repository.get(key);
    return repository.findByIndex(Transaction.MONTH_INDEX, seriesBudget.get(SeriesBudget.MONTH))
      .filterSelf(GlobMatchers.and(
        GlobMatchers.fieldEquals(Transaction.PLANNED, true),
        GlobMatchers.fieldEquals(Transaction.SERIES, seriesBudget.get(SeriesBudget.SERIES))),
                  repository).sort(Transaction.DAY);
  }

  private void createPlannedTransaction(Glob series, GlobRepository repository, int monthId, Integer day, Double amount) {
    repository.create(Transaction.TYPE,
                      value(Transaction.ACCOUNT, Account.SUMMARY_ACCOUNT_ID),
                      value(Transaction.AMOUNT, -amount),
                      value(Transaction.SERIES, series.get(Series.ID)),
                      value(Transaction.BANK_MONTH, monthId),
                      value(Transaction.BANK_DAY, day),
                      value(Transaction.MONTH, monthId),
                      value(Transaction.DAY, day),
                      value(Transaction.LABEL, series.get(Series.LABEL)),
                      value(Transaction.PLANNED, true),
                      value(Transaction.TRANSACTION_TYPE, TransactionType.PLANNED.getId()),
                      value(Transaction.CATEGORY, series.get(Series.DEFAULT_CATEGORY)));
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
