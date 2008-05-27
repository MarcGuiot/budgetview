package org.globs.samples.swingdemo.model;

import org.crossbowlabs.globs.metamodel.utils.GlobModelBuilder;
import org.crossbowlabs.globs.metamodel.GlobModel;

public class Model {
  public static final GlobModel INSTANCE = GlobModelBuilder.init().get();
}
