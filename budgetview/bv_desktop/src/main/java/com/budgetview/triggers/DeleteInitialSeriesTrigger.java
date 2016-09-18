package com.budgetview.triggers;

import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionImport;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class DeleteInitialSeriesTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(TransactionImport.TYPE)) {
      Set<Key> keySet = changeSet.getCreated(TransactionImport.TYPE);
      if (keySet.isEmpty()) {
        return;
      }
      for (Key key : keySet) {
        Glob transactionImport = repository.find(key);
        if (transactionImport != null) {
          if (transactionImport.isTrue(TransactionImport.IS_WITH_SERIES) && transactionImport.isTrue(TransactionImport.REPLACE_SERIES)) {
            GlobList seriesList = repository.getAll(Series.TYPE, fieldEquals(Series.IS_INITIAL, Boolean.TRUE));
            for (Glob series : seriesList) {
              if (repository.findByIndex(Transaction.SERIES_INDEX, Transaction.SERIES, series.get(Series.ID))
                .getGlobs().isEmpty()) {
                repository.delete(series);
              }
            }
            return;
          }
        }
      }
    }
  }
}
