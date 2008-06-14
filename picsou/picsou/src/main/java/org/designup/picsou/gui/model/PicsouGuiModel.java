package org.designup.picsou.gui.model;

import org.designup.picsou.model.PicsouModel;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.DefaultGlobModel;

public class PicsouGuiModel {
  private static GlobModel model = new DefaultGlobModel(
    PicsouModel.get(),
    GlobalStat.TYPE,
    MonthStat.TYPE
  );

  public static GlobModel get() {
    return model;
  }
}
