package org.designup.picsou.model.initial;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

public class InitialSeries {
  public static void run(GlobRepository repository) {
    repository.findOrCreate(Key.create(Series.TYPE, Series.OCCASIONAL_SERIES_ID),
                            value(Series.BUDGET_AREA, BudgetArea.OCCASIONAL.getId()),
                            value(Series.PROFILE_TYPE, ProfileType.EVERY_MONTH.getId()),
                            value(Series.DEFAULT_CATEGORY, MasterCategory.NONE.getId()),
                            value(Series.IS_AUTOMATIC, true),
                            value(Series.DAY, 1),
                            value(Series.LABEL, "occasional"),
                            value(Series.NAME, "occasional")
    );

    repository.findOrCreate(Key.create(Series.TYPE, Series.UNCATEGORIZED_SERIES_ID),
                            value(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId()),
                            value(Series.PROFILE_TYPE, ProfileType.IRREGULAR.getId()),
                            value(Series.IS_AUTOMATIC, false),
                            value(Series.NAME, "uncategorized"));
  }
}
