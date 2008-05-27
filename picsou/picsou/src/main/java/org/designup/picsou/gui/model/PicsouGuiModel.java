package org.designup.picsou.gui.model;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.utils.DefaultGlobModel;
import org.designup.picsou.model.PicsouModel;

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
