package com.budgetview.desktop.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class DashboardStat {
  public static GlobType TYPE;

  public static final Integer SINGLETON_ID = 0;
  public static org.globsframework.model.Key KEY;

  @Key
  public static IntegerField ID;

  public static IntegerField DAYS_SINCE_LAST_IMPORT;

  @Target(WeatherType.class)
  public static IntegerField WEATHER;

  public static IntegerField LAST_FORECAST_MONTH;

  public static IntegerField UNCATEGORIZED_COUNT;

  public static DoubleField REMAINDER;

  public static DoubleField TOTAL_MAIN_ACCOUNTS;
  public static BooleanField SINGLE_MAIN_ACCOUNT;
  public static DateField TOTAL_MAIN_ACCOUNTS_DATE;
  public static DoubleField TOTAL_ALL_ACCOUNTS;
  public static DateField TOTAL_ALL_ACCOUNTS_DATE;
  public static BooleanField SHOW_ALL_ACCOUNTS;

  static {
    TypeLoader.init(DashboardStat.class, "dashboardStat");
    KEY = org.globsframework.model.Key.create(TYPE, SINGLETON_ID);
  }

  public static Glob get(GlobRepository repository) {
    return repository.findOrCreate(KEY);
  }

  public static WeatherType getWeather(GlobRepository repository) {
    return WeatherType.get(get(repository).get(WEATHER));
  }
}
