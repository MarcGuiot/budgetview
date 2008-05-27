package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.SwingStretches;

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
