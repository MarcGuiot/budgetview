package org.designup.picsou.model.initial;

import org.globsframework.model.GlobRepository;
import static org.globsframework.model.FieldValue.value;
import org.designup.picsou.model.Series;

public class InitialSeries {
  public static void run(GlobRepository repository) {
    repository.create(Series.TYPE,
                      value(Series.ID, Series.OCCASIONAL_SERIES_ID),
                      value(Series.NAME, "occasional"));

    repository.create(Series.TYPE,
                      value(Series.ID, Series.UNCATEGORIZED_SERIES_ID),
                      value(Series.NAME, "occasional"));
  }
}
