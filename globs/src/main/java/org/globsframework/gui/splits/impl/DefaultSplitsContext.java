package org.globsframework.gui.splits.impl;

import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.splits.styles.StyleContext;

import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.util.Collections;

public class DefaultSplitsContext extends AbstractSplitsContext {
  private Directory directory;
  private Class referenceClass;
  private StyleContext styleContext = new StyleContext();

  public DefaultSplitsContext(Directory directory) {
    this.directory = directory;
  }

  public void setReferenceClass(Class referenceClass) {
    this.referenceClass = referenceClass;
  }

  public Directory getDirectory() {
    return directory;
  }

  public <T> T getService(Class<T> serviceClass) {
    return directory.get(serviceClass);
  }

  public Class getReferenceClass() {
    return referenceClass;
  }

  public StyleContext getStyles() {
    return styleContext;
  }

  public void cleanUp() {
    Collections.reverse(createdComponents);
    for (Component component : createdComponents) {
      Container parent = component.getParent();
      if (parent != null) {
        parent.remove(component);
      }
    }
    createdComponents.clear();
    super.cleanUp();
  }
}
