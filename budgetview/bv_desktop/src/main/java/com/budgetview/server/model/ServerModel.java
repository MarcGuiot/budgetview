package com.budgetview.server.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.DefaultGlobModel;

public class ServerModel {
  private static GlobModel model = new DefaultGlobModel(
  );

  public static GlobModel get() {
    return model;
  }

}
