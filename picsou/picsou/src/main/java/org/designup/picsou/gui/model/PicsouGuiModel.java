package org.designup.picsou.gui.model;

import org.designup.picsou.model.PicsouModel;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.DefaultGlobModel;

public class PicsouGuiModel {
  private static GlobModel INSTANCE = new DefaultGlobModel(
    PicsouModel.get(),
    MonthStat.TYPE,
    SeriesStat.TYPE,
    OccasionalSeriesStat.TYPE,
    Card.TYPE
  );

  public static GlobModel get() {
    return INSTANCE;
  }
}
