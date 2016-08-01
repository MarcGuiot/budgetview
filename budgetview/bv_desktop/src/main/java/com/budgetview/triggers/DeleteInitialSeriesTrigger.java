package com.budgetview.triggers;

import com.budgetview.model.Series;
import com.budgetview.model.Transaction;
import com.budgetview.model.TransactionImport;
import org.globsframework.model.*;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Set;

public class DeleteInitialSeriesTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsCreationsOrDeletions(TransactionImport.TYPE)) {
      Set<Key> keySet = changeSet.getCreated(TransactionImport.TYPE);
      if (keySet.isEmpty()) {
        return;
      }
      for (Key key : keySet) {
        Glob glob = repository.find(key);
        if (glob != null) {
          if (glob.get(TransactionImport.IS_WITH_SERIES)) {
            GlobList list = repository.getAll(Series.TYPE, GlobMatchers.fieldEquals(Series.IS_INITIAL, Boolean.TRUE));
            for (Glob series : list) {
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
