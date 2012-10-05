package org.designup.picsou.triggers;

import org.designup.picsou.model.Transaction;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.CurrentMonth;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;

import java.util.Set;

public class ReportTrigger extends AbstractChangeSetListener {
  public void globsChanged(ChangeSet changeSet, final GlobRepository repository) {
    changeSet.safeVisit(Transaction.TYPE, new ChangeSetVisitor() {
      public void visitCreation(Key key, FieldValues values) throws Exception {
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(Transaction.SERIES)){
          Glob transaction = repository.find(key);
          Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
          Glob glob = repository.find(Key.create(Series.TYPE, values.get(Transaction.SERIES)));
          if (glob == null) {
            // should be handle by series deletion trigger
            return;
          }
          if (!series.isTrue(Series.SHOULD_REPORT) && !glob.isTrue(Series.SHOULD_REPORT)) {
            return;
          }
          // !attention on peux avoir un changement de mois en plus

          return;
        }

        if (values.contains(Transaction.BUDGET_MONTH)) {
          Glob transaction = repository.find(key);
          Glob series = repository.findLinkTarget(transaction, Transaction.SERIES);
          if (!series.isTrue(Series.SHOULD_REPORT)){
            return;
          }
          Integer budgetMonthId = transaction.get(Transaction.BUDGET_MONTH);
          Glob currentMonth = repository.get(CurrentMonth.KEY);
          Integer previousMonthId = values.get(Transaction.BUDGET_MONTH);
          if (budgetMonthId <= currentMonth.get(CurrentMonth.CURRENT_MONTH)
            && previousMonthId <= currentMonth.get(CurrentMonth.CURRENT_MONTH)){

          }
          // add
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }
    });
  }
}
