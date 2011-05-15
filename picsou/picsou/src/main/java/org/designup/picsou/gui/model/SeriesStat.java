package org.designup.picsou.gui.model;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.index.NotUniqueIndex;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Glob;

public class SeriesStat {
  public static GlobType TYPE;

  @Key @Target(Series.class)
  public static LinkField SERIES;
  
  @Key @Target(Month.class)
  public static LinkField MONTH;

  public static DoubleField AMOUNT;

  public static DoubleField PLANNED_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField REMAINING_AMOUNT;  

  @DefaultDouble(0.0)
  public static DoubleField OVERRUN_AMOUNT;

  public static DoubleField SUMMARY_AMOUNT;

  public static BooleanField ACTIVE;

  public static NotUniqueIndex MONTH_INDEX;
  public static NotUniqueIndex SERIES_INDEX;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(SeriesStat.class);
    loader.defineNonUniqueIndex(MONTH_INDEX, MONTH);
    loader.defineNonUniqueIndex(SERIES_INDEX, SERIES);
  }

  public static BudgetArea getBudgetArea(org.globsframework.model.Key seriesStatKey, GlobRepository repository) {
    Glob series = repository.get(org.globsframework.model.Key.create(Series.TYPE, seriesStatKey.get(SeriesStat.SERIES)));
    return BudgetArea.get(series.get(Series.BUDGET_AREA));
  }
}
