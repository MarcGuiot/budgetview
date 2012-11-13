package org.designup.picsou.gui.model;

import org.designup.picsou.model.Account;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class AccountPositionError {
  public static GlobType TYPE;

  @Key
  @Target(Account.class)
  public static LinkField ID;

  @DefaultBoolean(false)
  public static BooleanField CLEARED;

  public static DateField UPDATE_DATE;

  public static StringField ACCOUNT_NAME;

  public static DoubleField IMPORTED_POSITION;

  public static DoubleField LAST_REAL_OPERATION_POSITION;

  // Full date : seed Month.toFullDate
  public static IntegerField LAST_PREVIOUS_IMPORT_DATE;

  static {
    GlobTypeLoader.init(AccountPositionError.class);
  }

}
