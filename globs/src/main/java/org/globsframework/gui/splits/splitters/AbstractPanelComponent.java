package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.SingleComponentLayout;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractPanelComponent<T extends JPanel> extends DefaultComponent<T> {

  private boolean customLayout;

  protected AbstractPanelComponent(Class<T> componentClass, String name, SplitProperties properties,
                                   Splitter[] subSplitters) {
    super(componentClass, name, properties, subSplitters, true);
    customLayout = properties.contains("layout");
    if (!customLayout && (subSplitters.length > 1)) {
      throw new SplitsException(name + " components cannot have more than one subcomponent, except if they use a custom layout");
    }
  }

  protected void postCreateComponent(T component, SplitsContext context) {
    component.setOpaque(false);
    if (customLayout) {
      for (Splitter subSplitter : getSubSplitters()) {
        ComponentConstraints constraints = subSplitter.createComponentStretch(context, true).componentConstraints;
        Component subComponent = constraints.getComponent();
        if (subComponent instanceof JPanel) {
          ((JPanel)subComponent).setOpaque(false);
        }
        String name = subComponent.getName();
        if (Strings.isNotEmpty(name)) {
          component.add(name, subComponent);
        }
        else {
          component.add(subComponent);
        }
      }
    }
    else if (getSubSplitters().length == 1) {
      ComponentConstraints constraints = getSubSplitters()[0].createComponentStretch(context, true).componentConstraints;
      Component subComponent = constraints.getComponent();
      if (subComponent instanceof JPanel) {
        ((JPanel)subComponent).setOpaque(false);
      }
      component.setLayout(new SingleComponentLayout(constraints.getInsets()));
      component.add(subComponent);
    }
  }

}
