package com.budgetview.triggers;

import com.budgetview.model.SubSeries;
import com.budgetview.model.Transaction;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobUtils;

import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.fieldIn;

public class SubSeriesDeletionTrigger extends DefaultChangeSetListener {
  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (!changeSet.containsChanges(SubSeries.TYPE)) {
      return;
    }

    Set<Integer> subSeriesIds =
      GlobUtils.getValues(changeSet.getDeleted(SubSeries.TYPE), SubSeries.ID);
    if (subSeriesIds.isEmpty()) {
      return;
    }

    repository.startChangeSet();
    try {
      for (Glob transaction : repository.getAll(Transaction.TYPE, 
                                                fieldIn(Transaction.SUB_SERIES, subSeriesIds))) {
        repository.update(transaction.getKey(),
                          Transaction.SUB_SERIES,
                          null);
      }
    }
    finally {
      repository.completeChangeSet();
    }
  }
}
