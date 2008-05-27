package org.designup.picsou.server.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.fields.LongField;
import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;

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
