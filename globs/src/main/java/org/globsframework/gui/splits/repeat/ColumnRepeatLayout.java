package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentConstraints;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public abstract class ColumnRepeatLayout implements RepeatLayout {

  public void check(Splitter[] splitterTemplates, String repeatRef) {
    if (splitterTemplates.length != 1) {
      throw new SplitsException("Repeat component '" + repeatRef + "' must have exactly one subcomponent");
    }
  }

  public void init(JPanel panel) {
    panel.setLayout(getLayout(panel));
  }

  protected abstract LayoutManager getLayout(JPanel panel);

  public void set(JPanel panel, List<ComponentConstraints[]> constraints) {
    panel.removeAll();
    for (ComponentConstraints[] constraintsArray : constraints) {
      panel.add(constraintsArray[0].getComponent());
    }
    panel.validate();
  }

  public void insert(JPanel panel, ComponentConstraints[] constraints, int index) {
    panel.add(constraints[0].getComponent(), index);
    panel.validate();
  }

  public void remove(JPanel panel, int index) {
    panel.remove(index);
    panel.validate();
  }

  public void move(JPanel panel, int previousIndex, int newIndex) {
    Component component = panel.getComponent(previousIndex);
    panel.remove(previousIndex);
    panel.add(component, newIndex);
    panel.validate();
  }

  public boolean managesInsets() {
    return false;
  }
}
