package org.designup.picsou.gui.description;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.model.Series;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Utils;

import java.util.Comparator;

public class DefaultPeriodSeriesStatComparator implements Comparator<Glob> {
  private GlobRepository repository;

  public DefaultPeriodSeriesStatComparator(GlobRepository repository) {
    this.repository = repository;
  }

  public int compare(Glob glob1, Glob glob2) {
    if ((glob1 == null) && (glob2 == null)) {
      return 0;
    }
    if (glob1 == null) {
      return -1;
    }
    if (glob2 == null) {
      return 1;
    }
    Boolean glob1Active = glob1.get(PeriodSeriesStat.ACTIVE);
    Boolean glob2Active = glob2.get(PeriodSeriesStat.ACTIVE);
    int activeResult = Utils.reverseCompare(glob1Active, glob2Active);
    if (activeResult != 0) {
      return activeResult;
    }

    if (glob1Active) {
      return Utils.reverseCompare(glob1.get(PeriodSeriesStat.ABS_SUM_AMOUNT), glob2.get(PeriodSeriesStat.ABS_SUM_AMOUNT));
    }

    Glob series1 = repository.findLinkTarget(glob1, PeriodSeriesStat.SERIES);
    Glob series2 = repository.findLinkTarget(glob2, PeriodSeriesStat.SERIES);
    return Utils.compare(series1.get(Series.NAME), series2.get(Series.NAME));
  }
}
