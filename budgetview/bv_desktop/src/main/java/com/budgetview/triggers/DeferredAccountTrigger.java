package com.budgetview.triggers;

import com.budgetview.model.*;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFunctor;

import java.util.Set;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.*;

public class DeferredAccountTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    if (changeSet.containsChanges(Transaction.TYPE)) {
      changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
        public void visitCreation(Key key, FieldValues values) throws Exception {

        }

        // Lors de la categorisation de l'operation de prelevement,
        // on verifie si le deferredTargetAccount du compte a debit differe est a jour
        // sinon on le met a jour. il faut faire ca dans ce trigger car derriere on
        // prend au compte ce changement pour
        public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
          if (values.contains(Transaction.SERIES)) {
            Integer seriesId = values.get(Transaction.SERIES);
            Glob series = repository.get(Key.create(Series.TYPE, seriesId));
            if (series.get(Series.BUDGET_AREA).equals(BudgetArea.OTHER.getId())
                && series.get(Series.FROM_ACCOUNT) != null) {
              Glob target = repository.findLinkTarget(series, Series.FROM_ACCOUNT);
              if (target != null && target.get(Account.DEFERRED_TARGET_ACCOUNT) == null) {
                Glob transaction = repository.get(key);
                repository.update(target, Account.DEFERRED_TARGET_ACCOUNT, transaction.get(Transaction.ACCOUNT));
              }
            }
          }
        }

        public void visitDeletion(Key key, FieldValues previousValues) throws Exception {

        }
      });
    }

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
                                  and(fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                      fieldEquals(Series.FROM_ACCOUNT, key.get(Account.ID)))).getFirst();
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
                          and(
                            fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                            isNotNull(Series.FROM_ACCOUNT)));
      repository.safeApply(Account.TYPE,
                           fieldEquals(Account.CARD_TYPE, AccountCardType.DEFERRED.getId()),
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
                          and(fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                              fieldEquals(Series.FROM_ACCOUNT, key.get(Account.ID))));
      for (Glob series : deferredSeries) {
        SeriesDeletionTrigger.propagateSeriesDeletion(series.getKey(), true, repository);
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
                                    value(Series.NAME, glob.get(Account.NAME)),
                                    value(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
                                    value(Series.FROM_ACCOUNT, accountId),
                                    value(Series.IS_AUTOMATIC, false),
                                    value(Series.INITIAL_AMOUNT, 0.),
                                    value(Series.TARGET_ACCOUNT, accountId));
    SeriesBudgetTrigger budget = new SeriesBudgetTrigger(repository);
    budget.updateSeriesBudget(series, repository);
  }
}
