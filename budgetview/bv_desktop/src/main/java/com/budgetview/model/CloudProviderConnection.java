package com.budgetview.model;

import com.budgetview.shared.model.Provider;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class CloudProviderConnection {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Provider.class)
  public static LinkField PROVIDER;

  public static IntegerField PROVIDER_CONNECTION_ID;

  public static StringField NAME;

  static {
    GlobTypeLoader.init(CloudProviderConnection.class);
  }
}
