package org.designup.picsou.gui.model;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
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

  @DefaultDouble(0.0)
  public static DoubleField AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PLANNED_AMOUNT;

  public static DoubleField SUMMARY_AMOUNT;

  static {
    GlobTypeLoader.init(SeriesStat.class);
  }

  public static BudgetArea getBudgetArea(org.globsframework.model.Key seriesStatKey, GlobRepository repository) {
    Glob series = repository.get(org.globsframework.model.Key.create(Series.TYPE, seriesStatKey.get(SeriesStat.SERIES)));
    return BudgetArea.get(series.get(Series.BUDGET_AREA));
  }
}
