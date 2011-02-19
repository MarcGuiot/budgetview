package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesBudgetChangeSetVisitor;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesChangeSetVisitor;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;

import java.util.Set;

public class SavingsAccountUpdateSeriesTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (AccountType.SAVINGS.getId().equals(values.get(Account.ACCOUNT_TYPE))) {
          if (values.get(Account.ID) != Account.SAVINGS_SUMMARY_ACCOUNT_ID
              && values.get(Account.ID) != Account.EXTERNAL_ACCOUNT_ID) {
            createSerie(repository, values);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Account.ACCOUNT_TYPE) &&
            AccountType.SAVINGS.getId().equals(values.get(Account.ACCOUNT_TYPE))) {
          createSerie(repository, repository.get(key));
        }
        else if (values.contains(Account.IS_IMPORTED_ACCOUNT)) {
          deleteCreatedBySeries(key, repository);
          createMirrorSeriesOnChange(key, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void deleteCreatedBySeries(Key key, GlobRepository repository) {
    GlobList savingSeries = repository.getAll(Series.TYPE,
                                              GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    repository.startChangeSet();
    try {
      for (Glob series : savingSeries) {
        if ((series.get(Series.FROM_ACCOUNT).equals(key.get(Account.ID))) ||
            (series.get(Series.TO_ACCOUNT).equals(key.get(Account.ID)))) {
          repository.delete(
            repository.getAll(Transaction.TYPE,
                              GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.SERIES, series.get(Series.ID)),
                                               GlobMatchers.or(
                                                 GlobMatchers.fieldEquals(Transaction.CREATED_BY_SERIES, Boolean.TRUE),
                                                 GlobMatchers.fieldEquals(Transaction.MIRROR, Boolean.TRUE)))));
        }
      }
    }
    finally {
      repository.completeChangeSet();
    }

  }


  void createMirrorSeriesOnChange(Key key, GlobRepository repository) {
    final LocalGlobRepository localRepository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Account.TYPE, AccountType.TYPE, BudgetArea.TYPE, Month.TYPE, CurrentMonth.TYPE,
              Series.TYPE, SeriesBudget.TYPE)
        .get();
    localRepository.addTrigger(new SeriesBudgetTrigger(repository));

    GlobList savingSeries = repository.getAll(Series.TYPE,
                                              GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()));
    localRepository.startChangeSet();
    try {
      for (Glob series : savingSeries) {
        if ((series.get(Series.FROM_ACCOUNT).equals(key.get(Account.ID))) ||
            (series.get(Series.TO_ACCOUNT).equals(key.get(Account.ID)))) {
          Integer newSeriesId = UpdateMirrorSeriesChangeSetVisitor.createMirrorSeries(series.getKey(), localRepository);
          if (newSeriesId != null) {
            Glob newSeries = localRepository.get(Key.create(Series.TYPE, newSeriesId));
            Glob otherSeries = localRepository.findLinkTarget(newSeries, Series.MIRROR_SERIES);
            repository.delete(
              repository.getAll(Transaction.TYPE,
                                GlobMatchers.and(GlobMatchers.fieldEquals(Transaction.SERIES, newSeriesId))));

            repository.delete(
              repository.getAll(Transaction.TYPE,
                                GlobMatchers.and(
                                  GlobMatchers.fieldEquals(Transaction.MIRROR, Boolean.TRUE),
                                  GlobMatchers.fieldEquals(Transaction.SERIES, otherSeries.get(Series.ID)))));
            GlobList transations = repository.getAll(Transaction.TYPE, GlobMatchers.or(
              GlobMatchers.fieldEquals(Transaction.SERIES, newSeriesId),
              GlobMatchers.fieldEquals(Transaction.SERIES, otherSeries.get(Series.ID))
            ));
            for (Glob transation : transations) {
              repository.update(transation.getKey(), FieldValue.value(Transaction.NOT_IMPORTED_TRANSACTION, null),
                                FieldValue.value(Transaction.MIRROR, Boolean.FALSE));
            }
          }
        }
      }
    }
    finally {
      localRepository.completeChangeSet();
    }

    ChangeSet currentChanges = localRepository.getCurrentChanges();

    localRepository.startChangeSet();
    try {
      currentChanges.safeVisit(SeriesBudget.TYPE, new UpdateMirrorSeriesBudgetChangeSetVisitor(localRepository));
    }
    finally {
      localRepository.completeChangeSet();
    }

    repository.apply(currentChanges);
  }

  private void createSerie(GlobRepository repository, FieldValues values) {
    GlobList globList = repository.getAll(Series.TYPE,
                                          and(
                                            fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()),
                                            or(
                                              fieldEquals(Series.FROM_ACCOUNT, values.get(Account.ID)),
                                              fieldEquals(Series.TO_ACCOUNT, values.get(Account.ID))
                                            )));
    if (!globList.isEmpty()) {
      return;
    }
    final LocalGlobRepository localRespository =
      LocalGlobRepositoryBuilder.init(repository)
        .copy(Account.TYPE, AccountType.TYPE, BudgetArea.TYPE, Month.TYPE, CurrentMonth.TYPE)
        .get();
    localRespository.addTrigger(new SeriesBudgetTrigger(localRespository));

    {
      localRespository.create(Series.TYPE,
                              FieldValue.value(Series.INITIAL_AMOUNT, 0.),
                              FieldValue.value(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()),
                              FieldValue.value(Series.FROM_ACCOUNT, values.get(Account.ID)),
                              FieldValue.value(Series.TO_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
                              FieldValue.value(Series.NAME,
                                               getSeriesName(values, "savings.series.auto.create.name.from.savings")),
                              FieldValue.value(Series.IS_AUTOMATIC, false));
    }
    {

      localRespository.create(Series.TYPE,
                              FieldValue.value(Series.INITIAL_AMOUNT, 0.),
                              FieldValue.value(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()),
                              FieldValue.value(Series.TO_ACCOUNT, values.get(Account.ID)),
                              FieldValue.value(Series.FROM_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
                              FieldValue.value(Series.NAME,
                                               getSeriesName(values, "savings.series.auto.create.name.to.savings")),
                              FieldValue.value(Series.IS_AUTOMATIC, false));
    }
    localRespository.commitChanges(true);
//    repository.completeChangeSet();
//    ChangeSet currentChanges = localRespository.getCurrentChanges();
//
//    localRespository.startChangeSet();
//    try {
//      currentChanges.safeVisit(Series.TYPE, new UpdateMirrorSeriesChangeSetVisitor(localRespository));
//    }
//    finally {
//      localRespository.completeChangeSet();
//    }
//
//    localRespository.startChangeSet();
//    try {
//      currentChanges.safeVisit(SeriesBudget.TYPE, new UpdateMirrorSeriesBudgetChangeSetVisitor(localRespository));
//    }
//    finally {
//      localRespository.completeChangeSet();
//    }

//    repository.apply(currentChanges);
  }

  private String getSeriesName(FieldValues values, final String key) {
    String seriesName;
    if (values.get(Account.NAME).startsWith(Lang.get("account.defaultName.standard", ""))) {
      seriesName = Lang.get(key + ".short", values.get(Account.NAME));
    }
    else {
      seriesName = Lang.get(key, values.get(Account.NAME));
    }
    return seriesName;
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
  }
}
