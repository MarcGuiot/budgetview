package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.impl.AbstractSplitsContext;
import org.globsframework.gui.splits.styles.StyleContext;
import org.globsframework.utils.Functor;
import org.globsframework.utils.directory.Directory;

import javax.swing.event.HyperlinkListener;

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

  public HyperlinkListener getHyperlinkListener(String name) {
    HyperlinkListener listener = super.findHyperlinkListener(name);
    if (listener == null) {
      return innerContext.getHyperlinkListener(name);
    }
    return listener;
  }
}
