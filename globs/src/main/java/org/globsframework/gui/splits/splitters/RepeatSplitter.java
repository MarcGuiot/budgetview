package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.repeat.RepeatHandler;
import org.globsframework.gui.splits.repeat.RepeatPanel;
import org.globsframework.utils.exceptions.ItemNotFound;

public class RepeatSplitter extends AbstractSplitter {
  private Splitter templateSplitter;
  private String ref;

  public RepeatSplitter(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, null);
    ref = properties.get("ref");
    if (ref == null) {
      throw new SplitsException("Repeat items must have a 'ref' attribute");
    }

    if (subSplitters.length != 1) {
      throw new SplitsException("Repeat component '" + ref + "' must have exactly one subcomponent");
    }
    this.templateSplitter = subSplitters[0];
  }

  protected ComponentStretch createRawStretch(SplitsContext context) {
    RepeatHandler repeatHandler = context.getRepeat(ref);
    if (repeatHandler == null) {
      throw new ItemNotFound("Repeat '" + ref + "' not declared");
    }
    RepeatPanel repeatPanel = new RepeatPanel(ref, repeatHandler, templateSplitter, context);
    return repeatPanel.getStretch();
  }

  public String getName() {
    return ref;
  }

}
