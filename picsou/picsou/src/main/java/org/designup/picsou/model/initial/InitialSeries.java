package org.designup.picsou.model.initial;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobRepository;

public class InitialSeries {
  public static void run(GlobRepository repository) {
    repository.create(Series.TYPE,
                      value(Series.ID, Series.OCCASIONAL_SERIES_ID),
                      value(Series.BUDGET_AREA, BudgetArea.OCCASIONAL_EXPENSES.getId()),
                      value(Series.PROFILE_TYPE, ProfileType.UNKNOWN.getId()),
                      value(Series.NAME, "occasional"));

    repository.create(Series.TYPE,
                      value(Series.ID, Series.UNCATEGORIZED_SERIES_ID),
                      value(Series.BUDGET_AREA, BudgetArea.UNCATEGORIZED.getId()),
                      value(Series.PROFILE_TYPE, ProfileType.UNKNOWN.getId()),
                      value(Series.NAME, "uncategorized"));
  }
}
