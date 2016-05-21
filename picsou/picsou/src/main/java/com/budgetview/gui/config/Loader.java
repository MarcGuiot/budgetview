package com.budgetview.gui.config;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public interface Loader {
  public void load(Directory directory, GlobRepository repository);
}
