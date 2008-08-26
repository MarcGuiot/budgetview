package org.globs.samples.swingdemo.model;

import org.globsframework.metamodel.utils.GlobModelBuilder;
import org.globsframework.metamodel.GlobModel;

public class Model {
  public static final GlobModel INSTANCE = GlobModelBuilder.init().get();
}
