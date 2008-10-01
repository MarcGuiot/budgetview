package org.designup.picsou.model.initial;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class InitialSeries {
  public static void run(GlobRepository repository) {
    repository.findOrCreate(Key.create(Series.TYPE, Series.OCCASIONAL_SERIES_ID),
                            value(Series.BUDGET_AREA, BudgetArea.OCCASIONAL_EXPENSES.getId()),
                            value(Series.PROFILE_TYPE, ProfileType.UNKNOWN.getId()),
                            value(Series.IS_AUTOMATIC, false),
                            value(Series.NAME, "occasional"));

    repository.findOrCreate(Key.create(Series.TYPE, Series.UNCATEGORIZED_SERIES_ID),
                            value(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId()),
                            value(Series.PROFILE_TYPE, ProfileType.UNKNOWN.getId()),
                            value(Series.IS_AUTOMATIC, false),
                            value(Series.NAME, "uncategorized"));
  }
}
