package org.designup.picsou.gui.series.view;

import org.designup.picsou.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldValue;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

public class SeriesWrapper {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(SeriesWrapperType.class)
  public static LinkField ITEM_TYPE;

  public static IntegerField ITEM_ID;

  @Target(SeriesWrapper.class)
  public static LinkField MASTER;

  public static final Integer ALL_ID = 0;
  public static final Integer UNCATEGORIZED_ID = 1;

  public static final Integer BALANCE_SUMMARY_ID = -2;
  public static final Integer MAIN_POSITION_SUMMARY_ID = -3;
  public static final Integer SAVINGS_POSITION_SUMMARY_ID = -4;

  public static final Integer[] SUMMARY_IDS = {BALANCE_SUMMARY_ID,
                                               MAIN_POSITION_SUMMARY_ID,
                                               SAVINGS_POSITION_SUMMARY_ID};

  static {
    GlobTypeLoader.init(SeriesWrapper.class);
  }

  public static Glob find(GlobRepository repository, SeriesWrapperType type, Integer itemId) {
    return repository.findUnique(SeriesWrapper.TYPE,
                                 FieldValue.value(SeriesWrapper.ITEM_TYPE, type.getId()),
                                 FieldValue.value(SeriesWrapper.ITEM_ID, itemId));
  }

  public static boolean isAll(Glob wrapper) {
    return wrapper.get(SeriesWrapper.ID).equals(ALL_ID);
  }

  public static boolean isUncategorized(Glob wrapper) {
    return wrapper.get(SeriesWrapper.ID).equals(UNCATEGORIZED_ID);
  }

  public static boolean isSummary(Glob wrapper) {
    Integer id = wrapper.get(SeriesWrapper.ID);
    for (Integer summaryId : SUMMARY_IDS) {
      if (id.equals(summaryId)) {
        return true;
      }
    }
    return false;
  }

  public static Glob getWrapperForBudgetArea(BudgetArea budgetArea, GlobRepository repository) {
    return repository.findUnique(SeriesWrapper.TYPE,
                                 value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId()),
                                 value(SeriesWrapper.ITEM_ID, budgetArea.getId()));
  }

  public static Glob getWrapperForSeries(Integer seriesId, GlobRepository repository) {
    return repository.findUnique(SeriesWrapper.TYPE,
                                 value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SERIES.getId()),
                                 value(SeriesWrapper.ITEM_ID, seriesId));
  }
}
