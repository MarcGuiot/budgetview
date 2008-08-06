package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.GridBagBuilder;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GridRepeatLayout implements RepeatLayout {

  private List<ComponentStretch[]> lineStretches = new ArrayList<ComponentStretch[]>();
  private GridBagBuilder builder;

  public void check(Splitter[] splitterTemplates, String repeatRef) {
  }

  public void init(JPanel panel) {
    builder = new GridBagBuilder(panel);
  }

  public void set(JPanel panel, List<ComponentStretch[]> lineStretches) {
    this.lineStretches = lineStretches;
    rebuild(panel);
  }

  public void insert(JPanel panel, ComponentStretch[] stretches, int index) {
    lineStretches.add(index, stretches);
    rebuild(panel);
  }

  public void remove(JPanel panel, int index) {
    lineStretches.remove(index);
    rebuild(panel);
  }

  public boolean managesInsets() {
    return true;
  }

  private void rebuild(JPanel panel) {
    panel.removeAll();
    int row = 0;
    for (ComponentStretch[] stretches : this.lineStretches) {
      int column = 0;
      for (ComponentStretch stretch : stretches) {
        builder.add(stretch, column, row, 1, 1);
        column++;
      }
      row++;
    }
    panel.validate();
  }
}
