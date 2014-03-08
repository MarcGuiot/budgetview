package org.designup.picsou.triggers;

import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountCardType;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class DeferredAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Account.TYPE)) {
      changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {
          if (values.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            createDeferredSeries(repository.get(key), repository);
          }
        }

        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Account.NAME)) {
            if (repository.get(key).get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
              Glob deferredSeries =
                repository.getAll(Series.TYPE,
                                  GlobMatchers.and(
                                    GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                    GlobMatchers.fieldEquals(Series.FROM_ACCOUNT, key.get(Account.ID)))).getFirst();
              if (deferredSeries != null) {
                repository.update(deferredSeries.getKey(), Series.NAME, values.get(Account.NAME));
              }
            }
          }
          if (!values.contains(Account.CARD_TYPE)) {
            return;
          }
          if (values.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            createDeferredSeries(repository.get(key), repository);
          }
          else if (values.getPrevious(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            deleteDeferredSeries(key, repository);
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
          if (previousValues.get(Account.CARD_TYPE).equals(AccountCardType.DEFERRED.getId())) {
            deleteDeferredSeries(key, repository);
          }
        }
      });
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Account.TYPE)) {
      final GlobList deferredSeries =
        repository.getAll(Series.TYPE,
                          GlobMatchers.and(
                            GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                            GlobMatchers.isNotNull(Series.FROM_ACCOUNT)));
      repository.safeApply(Account.TYPE,
                           GlobMatchers.fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()),
                           new GlobFunctor() {
                             public void run(Glob glob, GlobRepository repository) throws Exception {
                               Integer accountId = glob.get(Account.ID);
                               for (Glob series : deferredSeries) {
                                 if (accountId.equals(series.get(Series.FROM_ACCOUNT))) {
                                   return;
                                 }
                               }
                               createDeferredSeries(glob, repository);
                             }
                           });
    }
  }

  private void deleteDeferredSeries(Key key, GlobRepository repository) {
    try {
      repository.startChangeSet();
      final GlobList deferredSeries =
        repository.getAll(Series.TYPE,
                          GlobMatchers.and(GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                           GlobMatchers.fieldEquals(Series.FROM_ACCOUNT, key.get(Account.ID))));
      for (Glob series : deferredSeries) {
        SeriesDeletionTrigger.propagateSeriesDeletion(series.getKey(), repository);
      }
      repository.delete(deferredSeries);
    }
    finally {
      repository.completeChangeSet();
    }
  }

  private void createDeferredSeries(Glob glob, GlobRepository repository) {
    Integer accountId = glob.get(Account.ID);
    Glob series = repository.create(Series.TYPE,
                                    FieldValue.value(Series.NAME, glob.get(Account.NAME)),
                                    FieldValue.value(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                    FieldValue.value(Series.FROM_ACCOUNT, accountId),
                                    FieldValue.value(Series.IS_AUTOMATIC, false),
                                    FieldValue.value(Series.INITIAL_AMOUNT, 0.),
                                    FieldValue.value(Series.TARGET_ACCOUNT, accountId));
    SeriesBudgetTrigger budget = new SeriesBudgetTrigger(repository);
    budget.updateSeriesBudget(series, repository);
  }
}
