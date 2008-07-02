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

  protected AbstractPanelComponent(Class<T> componentClass, String name, SplitProperties properties,
                                   Splitter[] subSplitters) {
    super(componentClass, name, properties, subSplitters, true);
    if (subSplitters.length > 1) {
      throw new SplitsException(name + " components cannot have more than one subcomponent");

    }
  }

  protected void postCreateComponent(T component, SplitsContext context) {
    if (getSubSplitters().length == 1) {
      ComponentStretch stretch = getSubSplitters()[0].createComponentStretch(context, true);
      Component subComponent = stretch.getComponent();
      if (subComponent instanceof JPanel) {
        ((JPanel)subComponent).setOpaque(false);
      }
      GridBagBuilder
        .init(component)
        .add(subComponent,
             0, 0, 1, 1, 1.0, 1.0,
             Fill.BOTH, Anchor.CENTER,
             stretch.getInsets());
    }

  }
}
