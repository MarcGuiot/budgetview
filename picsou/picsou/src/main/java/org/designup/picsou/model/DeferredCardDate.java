package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.gui.accounts.Day;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

public class DeferredCardDate {
  public static GlobType TYPE;

  @Key
  @Target(Account.class)
  public static LinkField ACCOUNT;

  @Key
  @Target(Month.class)
  public static LinkField MONTH;

  @Target(Day.class)
  public static LinkField DAY;

  public static MultiFieldUniqueIndex ACCOUNT_AND_DATE;

  static {
    GlobTypeLoader loader = GlobTypeLoader.init(DeferredCardDate.class, "DeferredCardDate");
    loader.defineMultiFieldUniqueIndex(ACCOUNT_AND_DATE, ACCOUNT, MONTH);
  }
}