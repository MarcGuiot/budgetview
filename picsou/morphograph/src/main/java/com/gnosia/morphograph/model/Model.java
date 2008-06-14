package com.gnosia.morphograph.model;

import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.utils.DefaultGlobModel;

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
