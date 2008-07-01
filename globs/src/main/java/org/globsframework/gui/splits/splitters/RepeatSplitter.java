package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.*;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatPanel;
import org.globsframework.utils.exceptions.ItemNotFound;

public class RepeatSplitter extends AbstractSplitter {
  private Splitter templateSplitter;
  protected String name;

  public RepeatSplitter(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, null);
    name = properties.get("name");
    if (name == null) {
      throw new SplitsException("Repeat items must have a 'name' attribute");
    }

    if (subSplitters.length != 1) {
      throw new SplitsException("Repeat component '" + name + "' must have exactly one subcomponent");
    }
    this.templateSplitter = subSplitters[0];
  }

  protected ComponentStretch createRawStretch(SplitsContext context) {
    Repeat repeat = context.getRepeat(name);
    if (repeat == null) {
      throw new ItemNotFound("Repeat '" + name + "' not declared");
    }
    RepeatPanel repeatPanel = new RepeatPanel(repeat, templateSplitter, context);
    return repeatPanel.getStretch();
  }

  public String getName() {
    return null;
  }

}
