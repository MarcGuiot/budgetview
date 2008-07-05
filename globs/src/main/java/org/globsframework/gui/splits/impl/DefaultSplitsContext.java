package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.IconLocator;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.styles.StyleService;
import org.globsframework.utils.directory.Directory;

import java.awt.*;
import java.util.Collections;

public class DefaultSplitsContext extends AbstractSplitsContext {
  private Directory directory;
  private Class referenceClass;

  public DefaultSplitsContext(Directory directory) {
    this.directory = directory;
  }

  public void setReferenceClass(Class referenceClass) {
    this.referenceClass = referenceClass;
  }

  public <T> T getService(Class<T> serviceClass) {
    return directory.get(serviceClass);
  }

  public Class getReferenceClass() {
    return referenceClass;
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
