package org.designup.picsou.server.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class WordStatistic {

  public static GlobType TYPE;

  @Key
  public static LongField ID;

  public static StringField WORD;
  public static IntegerField TOTAL;

  static {
    GlobTypeLoader.init(WordStatistic.class);
  }
}
