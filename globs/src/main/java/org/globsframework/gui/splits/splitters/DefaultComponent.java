package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import java.awt.*;

public class DefaultComponent<T extends Component> extends AbstractSplitter {
  private Class<T> componentClass;
  private String name;

  public DefaultComponent(Class<T> componentClass,
                          String name,
                          SplitProperties properties,
                          Splitter[] subSplitters,
                          boolean acceptsSubSplitters) {
    super(properties, subSplitters);
    this.componentClass = componentClass;
    this.name = name;
    if (!acceptsSubSplitters && (subSplitters.length != 0)) {
      throw new RuntimeException("Subcomponents not allowed for '" + getName() + "' items");
    }
  }

  public String getName() {
    return name;
  }

  public ComponentStretch createRawStretch(SplitsContext context) {
    T component = findOrCreateComponent(context);
    postCreateComponent(component, context);
    return SwingStretches.get(component);
  }

  protected T findOrCreateComponent(SplitsContext context) {
    String ref = properties.getString("ref");
    String componentName = properties.getString("name");
    return context.findOrCreateComponent(ref, componentName, componentClass);
  }

  protected void postCreateComponent(T component, SplitsContext context) {
  }
}
