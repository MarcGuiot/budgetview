package com.budgetview.desktop.series.view;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.UnexpectedValue;

import java.util.Comparator;

public class SeriesWrapperComparator implements Comparator<Glob> {
  private static final Integer ALL_CATEGORY_ID = SeriesWrapper.ALL_ID;
  private static final Integer UNCATEGORIZED_ID = SeriesWrapper.UNCATEGORIZED_ID;

  private GlobRepository parentRepository;
  private GlobRepository localRepository;
  private GlobStringifier nameStringifier;

  public SeriesWrapperComparator(GlobRepository parentRepository, GlobRepository localRepository, GlobStringifier stringifier) {
    this.parentRepository = parentRepository;
    this.localRepository = localRepository;
    this.nameStringifier = stringifier;
  }

  public int compare(Glob wrapper1, Glob wrapper2) {

    if (Utils.equal(wrapper1, wrapper2)) {
      return 0;
    }

    int level1 = getLevel(wrapper1);
    int level2 = getLevel(wrapper2);
    if ((level1 == 0) && (level2 == 0)) {
      return compareTopLevel(wrapper1, wrapper2);
    }

    if (level1 < level2) {
      Glob parent2 = SeriesWrapper.getParent(wrapper2, localRepository);
      if (Utils.equal(wrapper1, parent2)) {
        return -1;
      }
      return compare(wrapper1, parent2);
    }
    else if (level1 > level2) {
      Glob parent1 = SeriesWrapper.getParent(wrapper1, localRepository);
      if (Utils.equal(wrapper2, parent1)) {
        return 1;
      }
      return compare(parent1, wrapper2);
    }
    else  {
      Glob parent1 = SeriesWrapper.getParent(wrapper1, localRepository);
      Glob parent2 = SeriesWrapper.getParent(wrapper2, localRepository);
      if (Utils.equal(parent1, parent2)) {
        return Utils.compare(getName(wrapper1), getName(wrapper2));
      }
      else {
        return compare(parent1, parent2);
      }
    }
  }

  protected int getLevel(Glob wrapper) {
    SeriesWrapperType type = SeriesWrapperType.get(wrapper);
    switch (type) {
      case BUDGET_AREA:
      case SUMMARY:
        return 0;
      case SERIES_GROUP:
        return 1;
      case SUB_SERIES:
        return 3;
      case SERIES:
        Glob parent = SeriesWrapper.getParent(wrapper, localRepository);
        if (SeriesWrapperType.get(parent).equals(SeriesWrapperType.BUDGET_AREA)) {
          return 1;
        }
        else {
          return 2;
        }
    }
    throw new UnexpectedValue("Unexpected type " + type + " for " + wrapper);
  }

  private int compareTopLevel(Glob wrapper1, Glob wrapper2) {
    SeriesWrapperType type1 = SeriesWrapperType.get(wrapper1);
    SeriesWrapperType type2 = SeriesWrapperType.get(wrapper2);
    if ((type1 == SeriesWrapperType.SUMMARY) && (type2 != SeriesWrapperType.SUMMARY)) {
      return -1;
    }
    if ((type1 != SeriesWrapperType.SUMMARY) && (type2 == SeriesWrapperType.SUMMARY)) {
      return 1;
    }
    if ((type1 == SeriesWrapperType.SUMMARY) && (type2 == SeriesWrapperType.SUMMARY)) {
      return Integer.signum(wrapper2.get(SeriesWrapper.ID) - wrapper1.get(SeriesWrapper.ID));
    }

    if (Utils.equal(wrapper1.get(SeriesWrapper.ID), ALL_CATEGORY_ID)) {
      return -1;
    }
    if (Utils.equal(wrapper2.get(SeriesWrapper.ID), ALL_CATEGORY_ID)) {
      return 1;
    }
    if (Utils.equal(wrapper1.get(SeriesWrapper.ID), UNCATEGORIZED_ID)) {
      return -1;
    }
    if (Utils.equal(wrapper2.get(SeriesWrapper.ID), UNCATEGORIZED_ID)) {
      return 1;
    }

    return Integer.signum(wrapper1.get(SeriesWrapper.ITEM_ID) - wrapper2.get(SeriesWrapper.ITEM_ID));
  }

  protected String getName(Glob category) {
    return nameStringifier.toString(category, parentRepository);
  }
}
