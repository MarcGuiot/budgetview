package org.globsframework.metamodel.utils;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobList;

public class GlobModelBuilder {
  private DefaultGlobModel model;

  public static GlobModelBuilder init(GlobType... types) {
    return init(null, types);
  }

  public static GlobModelBuilder init(GlobModel inner, GlobType... types) {
    return new GlobModelBuilder(inner, types);
  }

  private GlobModelBuilder(GlobModel inner, GlobType[] types) {
    this.model = new DefaultGlobModel(inner, types);
  }

  public GlobModelBuilder addConstants(GlobList constants) {
    this.model.addConstants(constants);
    return this;
  }

  public GlobModel get() {
    return model;
  }
}
