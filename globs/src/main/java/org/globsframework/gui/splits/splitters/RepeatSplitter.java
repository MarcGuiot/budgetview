package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.repeat.*;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.Strings;

public class RepeatSplitter extends AbstractSplitter {
  private Splitter[] splitterTemplates;
  private String ref;
  private RepeatLayout layout;

  public RepeatSplitter(SplitProperties properties, Splitter[] subSplitters) {
    super(properties, null);
    ref = properties.get("ref");
    if (ref == null) {
      throw new SplitsException("Repeat items must have a 'ref' attribute");
    }

    this.splitterTemplates = subSplitters;

    layout = getLayout(properties.get("layout"), ref);
    layout.check(subSplitters, ref);    
  }

  protected ComponentStretch createRawStretch(SplitsContext context) {
    RepeatHandler repeatHandler = context.getRepeat(ref);
    if (repeatHandler == null) {
      throw new ItemNotFound("Repeat '" + ref + "' not declared");
    }
    RepeatPanel repeatPanel = new RepeatPanel(ref, repeatHandler, layout, splitterTemplates, context);
    return repeatPanel.getStretch();
  }

  private RepeatLayout getLayout(String layoutProperty, String ref) {
    if (Strings.isNullOrEmpty(layoutProperty) || "column".equalsIgnoreCase(layoutProperty)) {
      return new ColumnRepeatLayout();
    }
    else if ("verticalGrid".equalsIgnoreCase(layoutProperty)) {
      return new GridRepeatLayout();
    }
    throw new SplitsException("Unknown layout type '" + layoutProperty + "' for repeat '" + ref +
                              "' - use one of [column|verticalGrid]");
  }

  public String getName() {
    return ref;
  }

  protected String[] getExcludedParameters() {
    return new String[]{"layout"};
  }
}
