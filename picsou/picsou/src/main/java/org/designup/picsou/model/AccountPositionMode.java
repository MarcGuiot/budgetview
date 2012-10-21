package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AccountPositionMode {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static BooleanField UPDATE_ACCOUNT_POSITION;

  static {
    GlobTypeLoader.init(AccountPositionMode.class, "accountPositionMode");
  }
}
