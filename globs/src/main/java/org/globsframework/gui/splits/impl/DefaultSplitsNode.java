package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.styles.Style;
import org.globsframework.gui.splits.utils.PropertySetter;

import java.awt.*;

public class DefaultSplitsNode<T extends Component> implements SplitsNode<T> {
  private Component component;
  private SplitsContext context;

  public DefaultSplitsNode(T component, SplitsContext context) {
    this.component = component;
    this.context = context;
  }

  public T getComponent() {
    return (T)component;
  }

  public void applyStyle(String styleName) {
    Style style = context.getStyles().getStyle(styleName);
    if (style == null) {
      throw new SplitsException("Style " + style + " not found");
    }
    SplitProperties properties = style.getProperties();
    PropertySetter.process(component, properties, context);
  }
}
