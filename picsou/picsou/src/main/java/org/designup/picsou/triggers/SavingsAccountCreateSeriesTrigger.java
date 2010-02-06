package org.designup.picsou.triggers;

import org.designup.picsou.model.*;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesBudgetChangeSetVisitor;
import org.designup.picsou.triggers.savings.UpdateMirrorSeriesChangeSetVisitor;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import static org.globsframework.model.utils.GlobMatchers.*;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;

import java.util.Set;

public class SavingsAccountCreateSeriesTrigger implements ChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (values.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
          if (values.get(Account.ID) != Account.SAVINGS_SUMMARY_ACCOUNT_ID) {
            createSerie(repository, values);
          }
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Account.ACCOUNT_TYPE) && values.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId())) {
          createSerie(repository, repository.get(key));
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
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
                              FieldValue.value(Series.IS_AUTOMATIC, true));
    }
    {

      localRespository.create(Series.TYPE,
                              FieldValue.value(Series.INITIAL_AMOUNT, 0.),
                              FieldValue.value(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()),
                              FieldValue.value(Series.TO_ACCOUNT, values.get(Account.ID)),
                              FieldValue.value(Series.FROM_ACCOUNT, Account.MAIN_SUMMARY_ACCOUNT_ID),
                              FieldValue.value(Series.NAME,
                                               getSeriesName(values, "savings.series.auto.create.name.to.savings")),
                              FieldValue.value(Series.IS_AUTOMATIC, true));
    }
    ChangeSet currentChanges = localRespository.getCurrentChanges();

    localRespository.startChangeSet();
    currentChanges.safeVisit(Series.TYPE, new UpdateMirrorSeriesChangeSetVisitor(localRespository));
    localRespository.completeChangeSet();

    localRespository.startChangeSet();
    currentChanges.safeVisit(SeriesBudget.TYPE, new UpdateMirrorSeriesBudgetChangeSetVisitor(localRespository));
    localRespository.completeChangeSet();

    repository.apply(currentChanges);
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
