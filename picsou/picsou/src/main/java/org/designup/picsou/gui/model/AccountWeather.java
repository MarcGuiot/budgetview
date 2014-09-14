package org.designup.picsou.gui.model;

import org.designup.picsou.model.Account;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class AccountWeather {
  public static GlobType TYPE;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Target(WeatherType.class)
  public static LinkField WEATHER;

  public static IntegerField LAST_FORECAST_MONTH;

  @DefaultDouble(0)
  public static DoubleField FUTURE_MIN;

  static {
    GlobTypeLoader.init(AccountWeather.class);
  }

  public static boolean isForMainAccount(Glob accountWeather, GlobRepository repository) {
    return Account.isMain(repository.findLinkTarget(accountWeather, ACCOUNT));
  }
}