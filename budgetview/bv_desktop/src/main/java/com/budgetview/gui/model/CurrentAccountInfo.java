package com.budgetview.gui.model;

import com.budgetview.model.Bank;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.DateField;

public class CurrentAccountInfo {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(Bank.class)
  public static LinkField BANK;

  public static DoubleField POSITION;

  public static DateField POSITION_DATE;


  static {
    GlobTypeLoader.init(CurrentAccountInfo.class);
  }
}
