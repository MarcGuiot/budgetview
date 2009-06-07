package org.designup.picsou.gui.series.edition;

import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobFunctor;

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
        repository.update(transaction.getKey(),
                          value(Transaction.SERIES, Series.UNCATEGORIZED_SERIES_ID),
                          value(Transaction.SUB_SERIES, null));
      }

      public String toString() {
        return Lang.get("subseries.delete.uncategorize");
      }
    };
  }
}
