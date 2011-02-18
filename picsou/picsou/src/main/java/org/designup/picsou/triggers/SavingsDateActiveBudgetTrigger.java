package org.designup.picsou.triggers;

import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.*;
import org.globsframework.utils.Pair;
import org.designup.picsou.model.*;

public class SavingsDateActiveBudgetTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Account.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        updateAllSeries(key, repository);
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Account.OPEN_DATE) || values.contains(Account.CLOSED_DATE)){
          updateAllSeries(key, repository);
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }

  private void updateAllSeries(Key key, GlobRepository repository) {
    Glob account = repository.find(key);
    if (!AccountType.SAVINGS.getId().equals(account.get(Account.ACCOUNT_TYPE))){
      return;
    }
    GlobList list = repository.getAll(Series.TYPE,
                                      GlobMatchers.and(GlobMatchers.fieldEquals(Series.BUDGET_AREA, BudgetArea.SAVINGS.getId()),
                                                       GlobMatchers.or(GlobMatchers.fieldEquals(Series.FROM_ACCOUNT, account.get(Account.ID)),
                                                                       GlobMatchers.fieldEquals(Series.TO_ACCOUNT, account.get(Account.ID)))));
    for (Glob series : list) {
      Pair<Integer,Integer> month = Account.getValidMonth(series, repository);
      ReadOnlyGlobRepository.MultiFieldIndexed index = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
      GlobList seriesBudget = index.getGlobs();
      for (Glob budget : seriesBudget) {
        int monthId = budget.get(SeriesBudget.MONTH);
        boolean isActive = budget.get(SeriesBudget.ACTIVE);
        repository.update(budget.getKey(), SeriesBudget.ACTIVE, 
                          isActive && monthId >=  month.getFirst() && monthId <= month.getSecond());
      }
    }
  }
}
