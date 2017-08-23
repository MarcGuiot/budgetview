package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;

public class CloudProviderAccount {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(CloudProviderConnection.class)
  public static LinkField CONNECTION;

  public static IntegerField PROVIDER_ACCOUNT_ID;

  public static StringField NAME;

  public static StringField NUMBER;

  public static BooleanField ENABLED;

  static {
    TypeLoader.init(CloudProviderAccount.class, "cloudProviderAccount");
  }
}
