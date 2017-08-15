package com.budgetview.desktop.accounts.utils;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;

public class MonthDay {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  static {
    TypeLoader.init(MonthDay.class, "monthDay");
  }
}