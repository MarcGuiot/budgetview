package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.impl.AbstractSplitsContext;
import org.globsframework.gui.splits.styles.StyleContext;
import org.globsframework.utils.directory.Directory;

public class RepeatContext extends AbstractSplitsContext {
  private SplitsContext innerContext;

  public RepeatContext(SplitsContext innerContext) {
    this.innerContext = innerContext;
  }

  public Directory getDirectory() {
    return innerContext.getDirectory();
  }

  public <T> T getService(Class<T> serviceClass) {
    return innerContext.getService(serviceClass);
  }

  public Class getReferenceClass() {
    return innerContext.getReferenceClass();
  }

  public StyleContext getStyles() {
    return innerContext.getStyles();
  }
}
