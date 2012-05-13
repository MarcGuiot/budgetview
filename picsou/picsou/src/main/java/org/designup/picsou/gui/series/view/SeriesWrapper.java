package org.designup.picsou.gui.series.view;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SubSeries;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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

  public static final Integer MAIN_POSITION_SUMMARY_ID = -2;
  public static final Integer BALANCE_SUMMARY_ID = -3;
  public static final Integer SAVINGS_POSITION_SUMMARY_ID = -4;

  public static final Integer[] SUMMARY_IDS = {BALANCE_SUMMARY_ID,
                                               MAIN_POSITION_SUMMARY_ID,
                                               SAVINGS_POSITION_SUMMARY_ID};

  static {
    GlobTypeLoader.init(SeriesWrapper.class);
  }

  public static Glob find(GlobRepository repository, SeriesWrapperType type, Integer itemId) {
    return repository.findUnique(SeriesWrapper.TYPE,
                                 value(SeriesWrapper.ITEM_TYPE, type.getId()),
                                 value(SeriesWrapper.ITEM_ID, itemId));
  }

  public static GlobList findAll(GlobRepository repository, SeriesWrapperType type, Integer itemId) {
    return repository.getAll(SeriesWrapper.TYPE,
                             and(fieldEquals(SeriesWrapper.ITEM_TYPE, type.getId()),
                                 fieldEquals(SeriesWrapper.ITEM_ID, itemId)));
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

  public static boolean isSeries(Glob wrapper) {
    return SeriesWrapperType.SERIES.getId().equals(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static boolean isSubSeries(Glob wrapper) {
    return SeriesWrapperType.SUB_SERIES.getId().equals(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static boolean isBudgetArea(Glob wrapper) {
    return SeriesWrapperType.BUDGET_AREA.getId().equals(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static Glob getSeries(Glob wrapper, GlobRepository repository) {
    Integer id = wrapper.get(SeriesWrapper.ITEM_ID);
    return repository.find(org.globsframework.model.Key.create(Series.TYPE, id));
  }

  public static Glob getSubSeries(Glob wrapper, GlobRepository repository) {
    Integer id = wrapper.get(SeriesWrapper.ITEM_ID);
    return repository.find(org.globsframework.model.Key.create(SubSeries.TYPE, id));
  }

  public static BudgetArea getBudgetArea(Glob wrapper) {
    Integer budgetAreaId = wrapper.get(SeriesWrapper.ITEM_ID);
    return BudgetArea.get(budgetAreaId);
  }

  public static String getDescription(Glob wrapper, GlobRepository repository) {
    if (SeriesWrapper.isSeries(wrapper)) {
      Glob series = SeriesWrapper.getSeries(wrapper, repository);
      return series.get(Series.DESCRIPTION);
    }
    else if (SeriesWrapper.isBudgetArea(wrapper)) {
      BudgetArea budgetArea = SeriesWrapper.getBudgetArea(wrapper);
      return budgetArea.getDescription();
    }
    return null;
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

  public static Glob getWrapperForSubSeries(Integer subSeriesId, GlobRepository repository) {
    return repository.findUnique(SeriesWrapper.TYPE,
                                 value(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.SUB_SERIES.getId()),
                                 value(SeriesWrapper.ITEM_ID, subSeriesId));
  }

  public static Glob getWrapper(Glob glob, GlobRepository repository) {
    if (glob.getType().equals(Series.TYPE)) {
      return getWrapperForSeries(glob.get(Series.ID), repository);
    }
    else if (glob.getType().equals(SubSeries.TYPE)) {
      return getWrapperForSubSeries(glob.get(SubSeries.ID), repository);
    }
    else if (glob.getType().equals(BudgetArea.TYPE)) {
      return getWrapperForBudgetArea(BudgetArea.get(glob.get(BudgetArea.ID)), repository);
    }
    else {
      throw new InvalidParameter("Unexpected type: " + glob.getType());
    }

  }

  public static String toString(Collection<Glob> wrappers, GlobRepository repository) {
    List<String> result = new ArrayList<String>();
    for (Glob wrapper : wrappers) {
      result.add(toString(wrapper, repository)); 
    }
    return result.toString();
  }
  
  public static String toString(Glob wrapper, GlobRepository repository) {
    switch (SeriesWrapperType.get(wrapper)) {
      case SERIES:
        return "series(" + getSeries(wrapper, repository).get(Series.NAME) + ")";
      case SUB_SERIES:
        return "subSeries(" + getSubSeries(wrapper, repository).get(SubSeries.NAME) + ")";
      case BUDGET_AREA:
        return "budgetArea(" + getBudgetArea(wrapper).getLabel() + ")";
      case SUMMARY:
        return "summary";
    }
    throw new InvalidState("Unexpected type found for:\n" + GlobPrinter.toString(wrapper));
  }
}
