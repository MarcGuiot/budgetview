package org.designup.picsou.gui.description;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class PeriodSeriesStatComparator implements Comparator<Glob> {
  private GlobRepository repository;

  public PeriodSeriesStatComparator(GlobRepository repository) {
    this.repository = repository;
  }

  public int compare(Glob stat1, Glob stat2) {
    if ((stat1 == null) && (stat2 == null)) {
      return 0;
    }
    if (stat1 == null) {
      return -1;
    }
    if (stat2 == null) {
      return 1;
    }

    Glob parentStat1 = PeriodSeriesStat.findParentStat(stat1, repository);
    Glob parentStat2 = PeriodSeriesStat.findParentStat(stat2, repository);

    if ((parentStat1 != null) && (parentStat2 != null)) {
      if (parentStat1.getKey().equals(parentStat2.getKey())) {
        return doCompareFields(stat1, stat2);
      }
      return doCompareFields(parentStat1, parentStat2);
    }
    else if ((parentStat1 != null) && (parentStat2 == null)) {
      if (parentStat1.getKey().equals(stat2.getKey())) {
        return 1;
      }
      return doCompareFields(parentStat1, stat2);
    }
    else if ((parentStat1 == null) && (parentStat2 != null)) {
      if (stat1.getKey().equals(parentStat2.getKey())) {
        return -1;
      }
      return doCompareFields(stat1, parentStat2);
    }

    return doCompareFields(stat1, stat2);
  }

  private int doCompareFields(Glob glob1, Glob glob2) {
    Boolean glob1Active = glob1.get(PeriodSeriesStat.ACTIVE);
    Boolean glob2Active = glob2.get(PeriodSeriesStat.ACTIVE);
    int activeResult = Utils.reverseCompare(glob1Active, glob2Active);
    if (activeResult != 0) {
      return activeResult;
    }

    if (Boolean.TRUE.equals(glob1Active)) {
      int result = Utils.reverseCompare(glob1.get(PeriodSeriesStat.ABS_SUM_AMOUNT), glob2.get(PeriodSeriesStat.ABS_SUM_AMOUNT));
      if (result != 0) {
        return result;
      }
    }

    return Utils.compare(PeriodSeriesStat.getName(glob1, repository),
                         PeriodSeriesStat.getName(glob2, repository));
  }
}
