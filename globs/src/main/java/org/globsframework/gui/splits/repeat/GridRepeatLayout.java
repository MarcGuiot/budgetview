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
  private Integer gridWrapLimit;

  public enum Direction {
    HORIZONTAL,
    VERTICAL
  }

  public GridRepeatLayout(Direction direction, Integer gridWrapLimit) {
    this.direction = direction;
    this.gridWrapLimit = gridWrapLimit;
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

  public void move(JPanel panel, int index1, int index2) {
    if (index1 > index2) {
      ComponentStretch[] stretches1 = lineStretches.remove(index1);
      ComponentStretch[] stretches2 = lineStretches.remove(index2);
      lineStretches.add(index2, stretches1);
      lineStretches.add(index1, stretches2);
    }
    else {
      ComponentStretch[] stretches2 = lineStretches.remove(index2);
      ComponentStretch[] stretches1 = lineStretches.remove(index1);
      lineStretches.add(index1, stretches2);
      lineStretches.add(index2, stretches1);
    }
    rebuild(panel);
  }

  public boolean managesInsets() {
    return true;
  }

  private void rebuild(JPanel panel) {
    panel.removeAll();
    int offset = 0;
    int group = 0;
    for (ComponentStretch[] stretches : this.lineStretches) {
      int item = 0;
      for (ComponentStretch stretch : stretches) {
        switch (direction) {
          case HORIZONTAL:
            builder.add(stretch, group, item + offset, 1, 1);
            break;
          case VERTICAL:
            builder.add(stretch, item + offset, group, 1, 1);
            break;
        }
        item++;
      }
      group++;
      if ((gridWrapLimit != null) && (group >= gridWrapLimit)) {
        group = 0;
        offset += stretches.length;
      }
    }
    panel.validate();
  }
}
