package org.globsframework.gui.splits.impl;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.styles.Style;
import org.globsframework.gui.splits.utils.PropertySetter;
import org.globsframework.utils.Strings;

import java.awt.*;

public class DefaultSplitsNode<T extends Component> implements SplitsNode<T> {
  private Component component;
  private SplitsContext context;
  private String lastStyleName;

  public DefaultSplitsNode(T component, SplitsContext context) {
    this.component = component;
    this.context = context;
  }

  public T getComponent() {
    return (T)component;
  }

  public void applyStyle(String styleName) {
    this.lastStyleName = styleName;
    Style style = context.getStyles().getStyle(styleName);
    if (style == null) {
      throw new SplitsException("Style '" + styleName + "' not found");
    }
    SplitProperties properties = style.getProperties();
    PropertySetter.process(component, properties, context);
  }

  public void reapplyStyle() {
    if (Strings.isNotEmpty(lastStyleName)) {
      applyStyle(lastStyleName);
    }
  }
}
