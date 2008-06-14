package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractPanelComponent<T extends JPanel> extends DefaultComponent<T> {

  protected AbstractPanelComponent(Class<T> componentClass, String name, SplitProperties properties, Splitter[] subSplitters, SplitsContext context) {
    super(componentClass, name, context, properties, subSplitters, true);
    if (subSplitters.length > 1) {
      throw new SplitsException(name + " components cannot have more than one subcomponent");

    }
    if (subSplitters.length == 1) {
      ComponentStretch stretch = subSplitters[0].getComponentStretch(true);
      Component component = stretch.getComponent();
      if (component instanceof JPanel) {
        ((JPanel)component).setOpaque(false);
      }
      GridBagBuilder
        .init(this.component)
        .add(component,
             0, 0, 1, 1, 1.0, 1.0,
             Fill.BOTH, Anchor.CENTER,
             stretch.getInsets());
    }
  }
}
