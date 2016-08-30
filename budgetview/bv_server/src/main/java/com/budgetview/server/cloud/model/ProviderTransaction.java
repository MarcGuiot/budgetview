package com.budgetview.server.cloud.model;

import com.budgetview.shared.model.DefaultSeries;
import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class ProviderTransaction {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @Target(CloudUser.class)
  public static LinkField USER;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_ID;

  public static IntegerField PROVDER_CATEGORY_ID;

  public static StringField PROVDER_CATEGORY_NAME;

  @Target(ProviderAccount.class)
  public static LinkField ACCOUNT;

  public static StringField LABEL;

  public static StringField ORIGINAL_LABEL;

  public static DoubleField AMOUNT;

  public static DateField OPERATION_DATE;

  public static DateField BANK_DATE;

  @Target(DefaultSeries.class)
  public static LinkField SERIES;

  public static BooleanField DELETED;

  static {
    GlobTypeLoader.init(ProviderTransaction.class, "providerTransaction");
  }
}
