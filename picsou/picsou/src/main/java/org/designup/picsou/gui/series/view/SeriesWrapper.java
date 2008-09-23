package org.designup.picsou.gui.series.view;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class SeriesWrapper {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static BooleanField IS_BUDGET_AREA;

  public static IntegerField ITEM_ID;

  @Target(SeriesWrapper.class)
  public static LinkField MASTER;

  public static final Integer ALL_ID = 0;
  public static final Integer UNCATEGORIZED_ID = 1;

  static {
    GlobTypeLoader.init(SeriesWrapper.class);
  }

  public static Glob find(GlobRepository repository, boolean isBudgetArea, Integer itemId) {
    return repository.findUnique(SeriesWrapper.TYPE,
                                 FieldValue.value(SeriesWrapper.IS_BUDGET_AREA, isBudgetArea),
                                 FieldValue.value(SeriesWrapper.ITEM_ID, itemId));
  }

}
