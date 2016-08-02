package com.budgetview.desktop.series.edition;

import com.budgetview.model.Series;
import com.budgetview.model.SubSeries;
import com.budgetview.model.Transaction;
import com.budgetview.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

import static org.globsframework.model.FieldValue.value;

public class DeleteSubSeriesFunctors {

  public static GlobFunctor moveToSeries(final Glob series) {
    return new GlobFunctor() {
      public void run(Glob transaction, GlobRepository repository) throws Exception {
        repository.update(transaction.getKey(),
                          value(Transaction.SERIES, series.get(Series.ID)),
                          value(Transaction.SUB_SERIES, null));
      }

      public String toString() {
        return Lang.get("subseries.delete.move.series", series.get(Series.NAME));
      }
    };
  }

  public static GlobFunctor moveToSubSeries(final Glob subSeries) {
    return new GlobFunctor() {
      public void run(Glob transaction, GlobRepository repository) throws Exception {
        repository.update(transaction.getKey(),
                          value(Transaction.SERIES, subSeries.get(SubSeries.SERIES)),
                          value(Transaction.SUB_SERIES, subSeries.get(SubSeries.ID)));
      }

      public String toString() {
        return Lang.get("subseries.delete.move.subseries", subSeries.get(SubSeries.NAME));
      }
    };
  }

  public static GlobFunctor uncategorize() {
    return new GlobFunctor() {
      public void run(Glob transaction, GlobRepository repository) throws Exception {
        Transaction.uncategorize(transaction, repository);
      }

      public String toString() {
        return Lang.get("subseries.delete.uncategorize");
      }
    };
  }
}
