package org.designup.picsou.gui.signpost.guides;

import org.globsframework.model.Key;
import org.globsframework.utils.Utils;

public class BudgetSignpostService {
  private Key periodicitySeriesKey;
  private Key amountSeriesKey;

  public void setPeriodicitySeriesKey(Key periodicitySeriesKey) {
    this.periodicitySeriesKey = periodicitySeriesKey;
  }

  public boolean isPeriodicitySeries(Key key) {
    return Utils.equal(periodicitySeriesKey, key);
  }

  public void setAmountSeriesKey(Key amountSeriesKey) {
    this.amountSeriesKey = amountSeriesKey;
  }

  public boolean isAmountSeries(Key key) {
    return Utils.equal(amountSeriesKey, key);
  }
}
