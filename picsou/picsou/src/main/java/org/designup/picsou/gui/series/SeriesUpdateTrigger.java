package org.designup.picsou.gui.series;

import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.utils.directory.Directory;

import java.util.List;
import java.util.Set;

public class SeriesUpdateTrigger implements ChangeSetListener {
  private Directory directory;
  private GlobRepository repository;
  private TimeService time;

  public SeriesUpdateTrigger(Directory directory, GlobRepository repository) {
    this.directory = directory;
    this.repository = repository;
    time = directory.get(TimeService.class);
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    repository.enterBulkDispatchingMode();
    try {
      if (!changeSet.containsChanges(Series.TYPE)) {
        return;
      }
      Set<Key> createdSeries = changeSet.getCreated(Series.TYPE);
      for (Key series : createdSeries) {
        createMissingBudget(repository.get(series));
        createMissingTransaction(repository.get(series));
      }
    }
    finally {
      repository.completeBulkDispatchingMode();
    }

  }

  private void createMissingTransaction(Glob series) {
    int monthId = time.getCurrentMonthId();
//    repository.findByIndex(Transaction.)
  }

  private void createMissingBudget(Glob series) {
    int monthId = time.getCurrentMonthId();
    Set<Integer> monthWithBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, series.get(Series.ID))
      .getValueSet(SeriesBudget.MONTH);
    int months[] = Month.createMonth(monthId, time.getFuturMonthCount());
    for (int month : months) {
      BooleanField monthField = Series.getField(month);
      if (series.get(monthField) && !monthWithBudget.contains(month)) {
        repository.create(SeriesBudget.TYPE,
                          value(SeriesBudget.SERIES, series.get(Series.ID)),
                          value(SeriesBudget.MONTH, month));
      }
    }
  }

  public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
  }
}
