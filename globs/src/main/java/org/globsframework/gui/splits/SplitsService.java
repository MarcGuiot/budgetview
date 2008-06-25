package org.globsframework.gui.splits;

import org.globsframework.utils.directory.Directory;

import java.util.List;

public class SplitsService {
  private Directory directory;
  private List<SplitsBuilder> builders;

  public SplitsService(Directory directory) {
    this.directory = directory;
  }

  public SplitsBuilder createBuilder() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builders.add(builder);
    return builder;
  }
}
