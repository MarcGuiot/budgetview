package com.budgetview.desktop.description;

import com.budgetview.desktop.model.PeriodSeriesStat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobTreeComparator;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class PeriodSeriesStatComparator extends GlobTreeComparator {
  private GlobRepository repository;
  private Comparator<Glob> comparator;

  public PeriodSeriesStatComparator(GlobRepository repository, Comparator<Glob> comparator) {
    this.repository = repository;
    this.comparator = comparator;
  }

  protected Glob findParent(Glob glob) {
    return PeriodSeriesStat.findParentStat(glob, repository);
  }

  protected int doCompareFields(Glob glob1, Glob glob2) {
    Boolean glob1Active = glob1.get(PeriodSeriesStat.ACTIVE);
    Boolean glob2Active = glob2.get(PeriodSeriesStat.ACTIVE);
    int activeResult = Utils.reverseCompare(glob1Active, glob2Active);
    if (activeResult != 0) {
      return activeResult;
    }

    if (Boolean.TRUE.equals(glob1Active)) {
      int result = comparator.compare(glob1, glob2);
      if (result != 0) {
        return result;
      }
    }

    return Utils.compare(PeriodSeriesStat.getName(glob1, repository),
                         PeriodSeriesStat.getName(glob2, repository));
  }
}
