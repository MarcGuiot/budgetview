package org.designup.picsou.gui.model;

import org.designup.picsou.model.PicsouModel;
import org.designup.picsou.gui.series.view.SeriesWrapper;
import org.designup.picsou.gui.series.view.SeriesWrapperType;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.DefaultGlobModel;

public class PicsouGuiModel {
  private static GlobModel INSTANCE = new DefaultGlobModel(
    PicsouModel.get(),
    BudgetStat.TYPE,
    Card.TYPE,
    PeriodSeriesStat.TYPE,
    SavingsBudgetStat.TYPE,
    SeriesStat.TYPE,
    SeriesWrapper.TYPE,
    SeriesWrapperType.TYPE
  );

  public static GlobModel get() {
    return INSTANCE;
  }
}
