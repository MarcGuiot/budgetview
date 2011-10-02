package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.annotations.Required;

public class ImportedSeries {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField NAME;

  @Target(BudgetArea.class)
  @Required
  public static LinkField BUDGET_AREA;

  @Target(Series.class)
  public static LinkField SERIES;

  @Target(SubSeries.class)
  public static LinkField SUB_SERIES;

  static {
    GlobTypeLoader.init(ImportedSeries.class);
  }

}
