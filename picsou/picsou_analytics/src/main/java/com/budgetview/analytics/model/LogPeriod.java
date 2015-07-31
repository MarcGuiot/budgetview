package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import java.util.Date;

public class LogPeriod {

  public static org.globsframework.model.Key KEY;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static DateField FIRST_DATE;
  public static DateField LAST_DATE;

  static {
    GlobTypeLoader.init(LogPeriod.class);
    KEY = org.globsframework.model.Key.create(LogPeriod.TYPE, 1);
  }

  public static Date getFirstDate(GlobRepository repository) {
    Glob item = repository.find(KEY);
    if (item == null) return new Date();
    return item.get(LogPeriod.FIRST_DATE);
  }

  public static Date getLastDate(GlobRepository repository) {
    Glob item = repository.find(KEY);
    if (item == null) return new Date();
    return item.get(LogPeriod.LAST_DATE);
  }
}
