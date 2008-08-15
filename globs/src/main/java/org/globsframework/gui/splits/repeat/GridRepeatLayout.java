package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentStretch;
import org.globsframework.gui.splits.layout.GridBagBuilder;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GridRepeatLayout implements RepeatLayout {

  private Direction direction;
  private List<ComponentStretch[]> lineStretches = new ArrayList<ComponentStretch[]>();
  private GridBagBuilder builder;

  public enum Direction {
    HORIZONTAL,
    VERTICAL
  }

  public GridRepeatLayout(Direction direction) {
    this.direction = direction;
  }

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
    int line = 0;
    for (ComponentStretch[] stretches : this.lineStretches) {
      int item = 0;
      for (ComponentStretch stretch : stretches) {
        switch (direction) {
          case HORIZONTAL:
            builder.add(stretch, line, item, 1, 1);
            break;
          case VERTICAL:
            builder.add(stretch, item, line, 1, 1);
            break;
        }
        item++;
      }
      line++;
    }
    panel.validate();
  }
}
