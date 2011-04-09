package org.designup.picsou.gui.categorization.components;

import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class BlankDynamicMessage extends DynamicMessage {
  public BlankDynamicMessage(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  protected boolean isVisible() {
    return false;
  }
}
