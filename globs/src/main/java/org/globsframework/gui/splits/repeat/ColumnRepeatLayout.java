package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentStretch;

import javax.swing.*;
import java.util.List;

public class ColumnRepeatLayout implements RepeatLayout {

  public void check(Splitter[] splitterTemplates, String repeatRef) {
    if (splitterTemplates.length != 1) {
      throw new SplitsException("Repeat component '" + repeatRef + "' must have exactly one subcomponent");
    }
  }

  public void init(JPanel panel) {
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
  }

  public void set(JPanel panel, List<ComponentStretch[]> stretches) {
    panel.removeAll();
    for (ComponentStretch[] stretchArray : stretches) {
      panel.add(stretchArray[0].getComponent());
    }
    panel.validate();
  }

  public void insert(JPanel panel, ComponentStretch[] stretches, int index) {
    panel.add(stretches[0].getComponent(), index);
    panel.validate();
  }

  public void remove(JPanel panel, int index) {
    panel.remove(index);
    panel.validate();
  }

  public boolean managesInsets() {
    return false;
  }
}
