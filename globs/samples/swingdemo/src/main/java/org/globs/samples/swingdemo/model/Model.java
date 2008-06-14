package org.globs.samples.swingdemo.model;

import org.globsframework.globs.metamodel.utils.GlobModelBuilder;
import org.globsframework.globs.metamodel.GlobModel;

public class Model {
  public static final GlobModel INSTANCE = GlobModelBuilder.init().get();
}
