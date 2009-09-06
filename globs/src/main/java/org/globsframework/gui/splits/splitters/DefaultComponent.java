package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitsNode;
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

  public SplitComponent createRawStretch(SplitsContext context) {
    SplitsNode<T> component = findOrCreateComponent(context);
    postCreateComponent(component.getComponent(), context);
    return new SplitComponent(SwingStretches.get(component.getComponent()), component);
  }

  protected SplitsNode<T> findOrCreateComponent(SplitsContext context) {
    String ref = properties.getString("ref");
    String componentName = properties.getString("name");
    return context.findOrCreateComponent(ref, componentName, componentClass, getName());
  }

  protected void postCreateComponent(T component, SplitsContext context) {
  }
}
