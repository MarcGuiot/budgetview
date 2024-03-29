package com.budgetview.desktop.series.view;

import com.budgetview.desktop.description.Labels;
import com.budgetview.desktop.model.SeriesStat;
import com.budgetview.desktop.model.SeriesType;
import com.budgetview.model.Account;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesGroup;
import com.budgetview.model.SubSeries;
import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.index.MultiFieldUniqueIndex;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.exceptions.ItemAmbiguity;
import org.globsframework.utils.exceptions.UnexpectedValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SeriesWrapper {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(SeriesWrapperType.class)
  public static LinkField ITEM_TYPE;

  public static IntegerField ITEM_ID;

  @Target(SeriesWrapper.class)
  public static LinkField PARENT;

  public static MultiFieldUniqueIndex INDEX;

  public static final Integer ALL_ID = 0;
  public static final Integer UNCATEGORIZED_ID = 1;

  public static final Integer MAIN_POSITION_SUMMARY_ID = -2;
  public static final Integer BALANCE_SUMMARY_ID = -3;
  public static final Integer SAVINGS_POSITION_SUMMARY_ID = -4;

  public static final Integer[] SUMMARY_IDS = {BALANCE_SUMMARY_ID,
                                               MAIN_POSITION_SUMMARY_ID,
                                               SAVINGS_POSITION_SUMMARY_ID};

  static {
    TypeLoader loader = TypeLoader.init(SeriesWrapper.class, "seriesWrapper");
    loader.defineMultiFieldUniqueIndex(INDEX, ITEM_TYPE, ITEM_ID, PARENT);
  }

  public static Glob find(GlobRepository repository, SeriesWrapperType type, Integer itemId) {
    return findUnique(itemId, repository, type.getId());
  }

  public static GlobList findAll(GlobRepository repository, SeriesWrapperType type, Integer itemId) {
    return repository.findByIndex(INDEX, SeriesWrapper.ITEM_TYPE, type.getId())
      .findByIndex(SeriesWrapper.ITEM_ID, itemId).getGlobs();
  }

  public static boolean isAll(Glob wrapper) {
    return wrapper.get(SeriesWrapper.ID).equals(ALL_ID);
  }

  public static boolean isSeries(Glob wrapper) {
    return SeriesWrapperType.SERIES.getId().equals(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static boolean isGroup(Glob wrapper) {
    return SeriesWrapperType.SERIES_GROUP.getId().equals(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static boolean isGroupPart(Glob wrapper, GlobRepository repository) {
    return isSeries(wrapper) && isGroup(getParent(wrapper, repository));
  }

  public static boolean isSubSeries(Glob wrapper) {
    return SeriesWrapperType.SUB_SERIES.getId().equals(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static boolean isBudgetArea(Glob wrapper) {
    return SeriesWrapperType.BUDGET_AREA.getId().equals(wrapper.get(SeriesWrapper.ITEM_TYPE));
  }

  public static Glob getSeries(Glob wrapper, GlobRepository repository) {
    if (!SeriesWrapperType.SERIES.getId().equals(wrapper.get(ITEM_TYPE))) {
      throw new UnexpectedValue("Unexpected type " + wrapper.get(ITEM_TYPE) + " for: " + wrapper);
    }
    Integer id = wrapper.get(SeriesWrapper.ITEM_ID);
    return repository.find(org.globsframework.model.Key.create(Series.TYPE, id));
  }

  public static Glob getGroup(Glob wrapper, GlobRepository repository) {
    if (!SeriesWrapperType.SERIES_GROUP.getId().equals(wrapper.get(ITEM_TYPE))) {
      throw new UnexpectedValue("Unexpected type " + wrapper.get(ITEM_TYPE) + " for: " + wrapper);
    }
    Integer id = wrapper.get(SeriesWrapper.ITEM_ID);
    return repository.find(org.globsframework.model.Key.create(SeriesGroup.TYPE, id));
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
    if (SeriesWrapper.isBudgetArea(wrapper)) {
      BudgetArea budgetArea = SeriesWrapper.getBudgetArea(wrapper);
      return Labels.getDescription(budgetArea);
    }
    return null;
  }

  public static GlobList getWrappedGlobs(Collection<Glob> wrappers, GlobRepository repository) {
    GlobList globs = new GlobList();
    for (Glob wrapper : wrappers) {
      globs.add(repository.get(getWrappedKey(wrapper)));
    }
    return globs;
  }

  private static org.globsframework.model.Key getWrappedKey(Glob wrapper) {
    Integer itemId = wrapper.get(SeriesWrapper.ITEM_ID);
    switch (SeriesWrapperType.get(wrapper)) {
      case SERIES:
        return org.globsframework.model.Key.create(Series.TYPE, itemId);
      case SERIES_GROUP:
        return org.globsframework.model.Key.create(SeriesGroup.TYPE, itemId);
      case SUB_SERIES:
        return org.globsframework.model.Key.create(SubSeries.TYPE, itemId);
      case BUDGET_AREA:
        return org.globsframework.model.Key.create(BudgetArea.TYPE, itemId);
      case SUMMARY:
        if (BALANCE_SUMMARY_ID.equals(itemId)) {
          return BudgetArea.ALL.getKey();
        }
        if (MAIN_POSITION_SUMMARY_ID.equals(itemId)) {
          return Account.MAIN_SUMMARY_KEY;
        }
        if (SAVINGS_POSITION_SUMMARY_ID.equals(itemId)) {
          return Account.SAVINGS_SUMMARY_KEY;
        }
    }
    throw new InvalidParameter("Unexpected wrapper type for " + wrapper);
  }

  public static BudgetArea getBudgetAreaForTarget(Glob wrapper, GlobRepository repository) {
    switch (SeriesWrapperType.get(wrapper)) {
      case SERIES:
        Glob series = getSeries(wrapper, repository);
        return BudgetArea.get(series.get(Series.BUDGET_AREA));
      case SERIES_GROUP:
        Glob group = getGroup(wrapper, repository);
        return BudgetArea.get(group.get(SeriesGroup.BUDGET_AREA));
      case BUDGET_AREA:
        return getBudgetArea(wrapper);
    }
    throw new InvalidParameter("Cannot get budgetArea for wrapper: " + wrapper);
  }


  public static Glob getWrapperForBudgetArea(BudgetArea budgetArea, GlobRepository repository) {
    Integer budgetAreaId = budgetArea.getId();
    return getWrapperForBudgetArea(budgetAreaId, repository);
  }

  protected static Glob getWrapperForBudgetArea(Integer budgetAreaId, GlobRepository repository) {
    return findUnique(budgetAreaId, repository, SeriesWrapperType.BUDGET_AREA.getId());
  }

  public static Glob getWrapperForSeries(Integer seriesId, GlobRepository repository) {
    return findUnique(seriesId, repository, SeriesWrapperType.SERIES.getId());
  }

  public static Glob getWrapperForSeriesGroup(Integer groupId, GlobRepository repository) {
    return findUnique(groupId, repository, SeriesWrapperType.SERIES_GROUP.getId());
  }

  private static Glob findUnique(Integer itemId, GlobRepository repository, Integer id) {
    GlobList globs = repository.findByIndex(INDEX, SeriesWrapper.ITEM_TYPE, id)
      .findByIndex(SeriesWrapper.ITEM_ID, itemId).getGlobs();
    if (globs.size() > 1) {
      throw new ItemAmbiguity("Several elements " + globs);
    }
    return globs.getFirst();
  }

  public static Glob getWrapperForSubSeries(Integer subSeriesId, GlobRepository repository) {
    return findUnique(subSeriesId, repository, SeriesWrapperType.SUB_SERIES.getId());
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
    else if (glob.getType().equals(SeriesGroup.TYPE)) {
      return getWrapperForSeriesGroup(glob.get(SeriesGroup.ID), repository);
    }
    else {
      throw new InvalidParameter("Unexpected type: " + glob.getType());
    }
  }

  public static Glob getParent(Glob wrapper, GlobRepository repository) {
    return repository.findLinkTarget(wrapper, SeriesWrapper.PARENT);
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
      case SERIES_GROUP:
        return "seriesGroup(" + getGroup(wrapper, repository).get(SeriesGroup.NAME) + ")";
      case SUB_SERIES:
        return "subSeries(" + getSubSeries(wrapper, repository).get(SubSeries.NAME) + ")";
      case BUDGET_AREA:
        return "budgetArea(" + Labels.get(getBudgetArea(wrapper)) + ")";
      case SUMMARY:
        return "summary";
    }
    throw new InvalidState("Unexpected type found for:\n" + GlobPrinter.toString(wrapper));
  }

  public static boolean shouldCreateWrapperForSeries(Glob seriesValues) {
    Integer budgetAreaId = seriesValues.get(Series.BUDGET_AREA);
    if (BudgetArea.OTHER.getId().equals(budgetAreaId)) {
      return false;
    }

    if (BudgetArea.TRANSFER.getId().equals(budgetAreaId)) {
      Integer targetAccountId = seriesValues.get(Series.TARGET_ACCOUNT);
      return targetAccountId != null &&
             Account.EXTERNAL_ACCOUNT_ID != targetAccountId &&
             seriesValues.get(Series.FROM_ACCOUNT).equals(targetAccountId);
    }

    Integer seriesId = seriesValues.get(Series.ID);
    return !seriesId.equals(Series.UNCATEGORIZED_SERIES_ID) &&
           !seriesId.equals(Series.ACCOUNT_SERIES_ID);
  }

  public static org.globsframework.model.Key createSeriesStatKey(Glob wrapper, int monthId) {
    return org.globsframework.model.Key.create(SeriesStat.ACCOUNT, Account.ALL_SUMMARY_ACCOUNT_ID,
                                               SeriesStat.TARGET_TYPE, getSeriesType(wrapper).getId(),
                                               SeriesStat.TARGET, wrapper.get(ITEM_ID),
                                               SeriesStat.MONTH, monthId);
  }

  public static SeriesType getSeriesType(Glob wrapper) {
    switch (SeriesWrapperType.get(wrapper)) {
      case SERIES:
        return SeriesType.SERIES;
      case SERIES_GROUP:
        return SeriesType.SERIES_GROUP;
    }
    throw new InvalidParameter("Cannot convert type " + SeriesWrapperType.get(wrapper) + " for: " + wrapper);
  }
}
