package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.exceptions.SplitsException;
import org.crossbowlabs.splits.layout.ComponentStretch;
import org.crossbowlabs.splits.layout.SwingStretches;

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
