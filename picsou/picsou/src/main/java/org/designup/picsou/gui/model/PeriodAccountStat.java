package org.designup.picsou.gui.model;

import org.designup.picsou.model.Account;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class PeriodAccountStat {
  public static GlobType TYPE;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @DefaultBoolean(true)
  public static BooleanField OK;

  public static IntegerField SEQUENCE;

  static {
    GlobTypeLoader.init(PeriodAccountStat.class);
  }
}