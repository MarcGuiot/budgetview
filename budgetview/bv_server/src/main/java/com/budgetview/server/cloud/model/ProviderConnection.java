package com.budgetview.server.cloud.model;

import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class ProviderConnection {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  @Target(CloudUser.class)
  public static IntegerField USER;

  @Target(Provider.class)
  public static IntegerField PROVIDER;

  public static IntegerField PROVIDER_CONNECTION;

  public static BooleanField INITIALIZED;

  static {
    GlobTypeLoader.init(ProviderConnection.class, "providerConnection");
  }
}
