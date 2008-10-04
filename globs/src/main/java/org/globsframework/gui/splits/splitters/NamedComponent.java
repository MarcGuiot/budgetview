package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.SwingStretches;

import java.awt.*;

public class NamedComponent extends AbstractSplitter {
  private String ref;
  protected Component component;

  public NamedComponent(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, subSplitters);
    ref = properties.getString("ref");
    if (ref == null) {
      throw new SplitsException("Property 'ref' is mandatory for named components");
    }
  }

  public ComponentStretch createRawStretch(SplitsContext context) {
    component = context.findComponent(ref);
    if (component == null) {
      throw new SplitsException("No component found for ref: " + ref);
    }
    return SwingStretches.get(component);
  }

  public String toString() {
    return ref + " (" + component.getClass().getSimpleName() + ")";
  }

  public String getName() {
    return "component";
  }
}
