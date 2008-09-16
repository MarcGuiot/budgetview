package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.impl.AbstractSplitsContext;
import org.globsframework.gui.splits.styles.StyleContext;

public class RepeatContext extends AbstractSplitsContext {
  private SplitsContext innerContext;

  public RepeatContext(SplitsContext innerContext) {
    this.innerContext = innerContext;
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
