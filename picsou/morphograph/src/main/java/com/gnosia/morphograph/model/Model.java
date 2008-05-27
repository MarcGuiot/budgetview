package com.gnosia.morphograph.model;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.utils.DefaultGlobModel;

public class Model {
  private static GlobModel model = new DefaultGlobModel(
    Topic.TYPE,
    Series.TYPE,
    Exercise.TYPE,
    ExerciseType.TYPE,
    Input.TYPE,
    Select.TYPE
  );

  public static GlobModel get() {
    return model;
  }
}
