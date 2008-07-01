package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import java.awt.*;

public class NamedComponent extends AbstractSplitter {
  private String name;
  protected Component component;

  public NamedComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
    name = properties.getString("ref");
    if (name == null) {
      throw new SplitsException("Property 'ref' is mandatory for named components");
    }
  }

  public ComponentStretch createRawStretch(SplitsContext context) {
    component = context.findComponent(name);
    if (component == null) {
      throw new SplitsException("No component found for ref: " + name);
    }
    return SwingStretches.get(component);
  }

  public String toString() {
    return name + " (" + component.getClass().getSimpleName() + ")";
  }

  public String getName() {
    return "component";
  }
}
