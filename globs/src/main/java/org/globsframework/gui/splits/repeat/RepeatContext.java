package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.impl.AbstractSplitsContext;

import java.util.ArrayList;
import java.util.List;

public class RepeatContext extends AbstractSplitsContext {
  private SplitsContext innerContext;
  private List<RepeatCellBuilder.DisposeListener> disposeListeners = new ArrayList<RepeatCellBuilder.DisposeListener>();

  public RepeatContext(SplitsContext innerContext) {
    this.innerContext = innerContext;
  }

  public <T> T getService(Class<T> serviceClass) {
    return innerContext.getService(serviceClass);
  }

  public Class getReferenceClass() {
    return innerContext.getReferenceClass();
  }

  public void addDisposeListener(RepeatCellBuilder.DisposeListener listener) {
    this.disposeListeners.add(listener);
  }

  public void dispose() {
    super.dispose();
    for (RepeatCellBuilder.DisposeListener listener : disposeListeners) {
      listener.dispose();
    }
  }
}
