package org.designup.picsou.gui.description;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobTreeComparator;
import org.globsframework.utils.Utils;

public class PeriodSeriesStatComparator extends GlobTreeComparator {
  private GlobRepository repository;

  public PeriodSeriesStatComparator(GlobRepository repository) {
    this.repository = repository;
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
      int result = Utils.reverseCompare(glob1.get(PeriodSeriesStat.ABS_SUM_AMOUNT), glob2.get(PeriodSeriesStat.ABS_SUM_AMOUNT));
      if (result != 0) {
        return result;
      }
    }

    return Utils.compare(PeriodSeriesStat.getName(glob1, repository),
                         PeriodSeriesStat.getName(glob2, repository));
  }
}
