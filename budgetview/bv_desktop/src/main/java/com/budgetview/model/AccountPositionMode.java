package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;

public class AccountPositionMode {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static BooleanField UPDATE_ACCOUNT_POSITION;

  static {
    TypeLoader.init(AccountPositionMode.class, "accountPositionMode");
  }
}
