package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import java.awt.*;

public class NamedComponent extends AbstractSplitter {

  public static NamedComponent get(SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    String ref = properties.getString("ref");
    if (ref == null) {
      throw new SplitsException("Property 'ref' is mandatory for named components");
    }
    Component component = context.findComponent(ref);
    if (component == null) {
      throw new SplitsException("No component found for ref: " + ref);
    }
    return new NamedComponent(ref, component, subSplitters, properties, context);
  }

  private String name;
  private Component component;

  public NamedComponent(String name, Component component, Splitter[] subSplitters, SplitProperties properties, SplitsContext context) {
    super(properties, subSplitters, context);
    this.name = name;
    this.component = component;
  }

  public ComponentStretch createRawStretch() {
    return SwingStretches.get(component);
  }

  public String toString() {
    return name + " (" + component.getClass().getSimpleName() + ")";
  }

  public String getName() {
    return "component";
  }
}
