package org.globsframework.gui.splits.repeat;

import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.GridBagBuilder;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GridRepeatLayout implements RepeatLayout {

  private Direction direction;
  private List<ComponentConstraints[]> lineConstraints = new ArrayList<ComponentConstraints[]>();
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

  public void checkHeader(Splitter[] splitters, String repeatRef) {
  }

  public void checkContent(Splitter[] splitterTemplates, String repeatRef) {
  }

  public void init(JPanel panel) {
    builder = new GridBagBuilder(panel);
  }

  public void set(JPanel panel, List<ComponentConstraints[]> lineConstraints) {
    this.lineConstraints = lineConstraints;
    rebuild(panel);
  }

  public void insert(JPanel panel, ComponentConstraints[] constraints, int index) {
    lineConstraints.add(index, constraints);
    rebuild(panel);
  }

  public void remove(JPanel panel, int index) {
    lineConstraints.remove(index);
    rebuild(panel);
  }

  public void move(JPanel panel, int previousIndex, int newIndex) {
    ComponentConstraints[] constraints = lineConstraints.remove(previousIndex);
    lineConstraints.add(newIndex, constraints);
    rebuild(panel);
  }

  public boolean managesInsets() {
    return true;
  }

  private void rebuild(JPanel panel) {
    panel.removeAll();
    int offset = 0;
    int group = 0;
    for (ComponentConstraints[] allConstraints : this.lineConstraints) {
      int item = 0;
      for (ComponentConstraints constraints : allConstraints) {
        switch (direction) {
          case HORIZONTAL:
            builder.add(constraints, group, item + offset, 1, 1);
            break;
          case VERTICAL:
            builder.add(constraints, item + offset, group, 1, 1);
            break;
        }
        item++;
      }
      group++;
      if ((gridWrapLimit != null) && (group >= gridWrapLimit)) {
        group = 0;
        offset += allConstraints.length;
      }
    }
    panel.validate();
  }
}
