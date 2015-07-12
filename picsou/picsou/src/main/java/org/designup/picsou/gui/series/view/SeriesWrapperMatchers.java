package org.designup.picsou.gui.series.view;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.Month;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;

import static org.globsframework.model.utils.GlobMatchers.fieldEquals;

public class SeriesWrapperMatchers {
  public static GlobMatcher budgetAreas() {
    return fieldEquals(SeriesWrapper.ITEM_TYPE, SeriesWrapperType.BUDGET_AREA.getId());
  }

  public static abstract class ActiveSeries implements GlobMatcher {
    public boolean matches(Glob wrapper, GlobRepository repository) {
      if (!SeriesWrapperType.SERIES.isOfType(wrapper)) {
        return false;
      }

      Integer referenceMonthId = getReferenceMonthId();
      if (referenceMonthId == null) {
        return false;
      }

      for (int monthId : getMonthRange()) {
        Glob seriesStat = repository.find(SeriesStat.createKeyForSeries(wrapper.get(SeriesWrapper.ITEM_ID), monthId));
        if ((seriesStat != null) &&
            (Amounts.isNotZero(seriesStat.get(SeriesStat.PLANNED_AMOUNT))
             || Amounts.isNotZero(seriesStat.get(SeriesStat.ACTUAL_AMOUNT)))) {
          return true;
        }
      }

      return false;
    }

    public abstract Iterable<Integer> getMonthRange();

    public  abstract Integer getReferenceMonthId();
  }

}
