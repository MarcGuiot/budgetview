package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import java.awt.*;

public class DefaultComponent<T extends Component> extends AbstractSplitter {

  protected T component;
  private String name;
  private SplitsContext context;

  public DefaultComponent(Class<T> componentClass,
                          String name,
                          SplitsContext context,
                          SplitProperties properties,
                          Splitter[] subSplitters,
                          boolean acceptsSubSplitters) {
    super(properties, subSplitters, context);
    this.name = name;
    this.context = context;
    if (!acceptsSubSplitters && (subSplitters.length != 0)) {
      throw new RuntimeException("Subcomponents not allowed for '" + getName() + "' items");
    }

    String ref = properties.getString("ref");
    String componentName = properties.getString("name");
    this.component = context.findOrCreateComponent(ref, componentName, componentClass);
  }

  public String getName() {
    return name;
  }

  public ComponentStretch createRawStretch() {
    processComponent(component, getProperties(), context);
    return SwingStretches.get(component);
  }

  protected void processComponent(T component, SplitProperties properties, SplitsContext context) {
  }
}
