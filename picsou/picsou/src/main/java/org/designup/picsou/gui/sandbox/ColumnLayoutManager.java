package org.designup.picsou.gui.sandbox;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class ColumnLayoutManager implements LayoutManager {
  private int maxColumnCount;
  private static final int MAX_COUNT = 3;

  public ColumnLayoutManager(int maxColumnCount) {
    this.maxColumnCount = maxColumnCount;
  }

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  public Dimension preferredLayoutSize(Container parent) {
    Component[] components = parent.getComponents();
    Rectangle[] positions = new Rectangle[components.length];
    for (int i = 0; i < positions.length; i++) {
      positions[i] = new Rectangle();
    }
    double totalHeight = 0;
    for (Component component : components) {
      Dimension dimension = component.getPreferredSize();
      totalHeight += dimension.getHeight();
    }
     double height = totalHeight / maxColumnCount;
    int tryCount = 0;
    int[] maxheight = new int[maxColumnCount];
    int[] maxWidth = new int[maxColumnCount];
    while (tryCount < MAX_COUNT) {
      int x = 0;
      int y = 0;
      int width = 0;
      int currentColumn = 0;
      int i = 0;
      for (Component component : components) {
        int componentHeight = component.getPreferredSize().height;
        if (y + componentHeight > height && atLeatOneComponentPerColumn(y) && notInLastColumn(currentColumn)) {
          currentColumn++;
          x += width;
          y = 0;
        }
        Rectangle position = positions[i];
        position.x = x;
        position.y = y;
        position.width = width;
        position.height = componentHeight;
        y += componentHeight;
        maxheight[currentColumn] = y;
        i++;
      }
      if (currentColumn == maxColumnCount - 1) {
        int max = maxExpectLast(maxheight);
        if (max < maxheight[currentColumn]) {
          height += (maxheight[currentColumn] - max) / maxColumnCount;
          tryCount++;
        }
        else {
          tryCount = MAX_COUNT;
        }
      }
      else {
        tryCount = MAX_COUNT;
      }
    }
    int i = 0;
    for (Component component : components) {
      Rectangle position = positions[i];
      component.setBounds(position.x, position.y, position.width, position.height);
      i++;
    }

    return new Dimension(100, 100);
  }

  public Dimension minimumLayoutSize(Container parent) {
    return new Dimension(100, 100);
  }

  public void layoutContainer(Container parent) {
    Component[] components = parent.getComponents();
    Rectangle[] positions = new Rectangle[components.length];
    for (int i = 0; i < positions.length; i++) {
      positions[i] = new Rectangle();
    }
    double height = 0;
    double totalHeight = 0;
    int columnCount = 1;
    for (Component component : components) {
      Dimension dimension = component.getPreferredSize();
      totalHeight += dimension.getHeight();
      height += dimension.getHeight();
      if (height > parent.getHeight()) {
        columnCount++;
        height = dimension.getHeight();
      }
    }
    if (columnCount > maxColumnCount) {
      columnCount = maxColumnCount;
      height = totalHeight / maxColumnCount;
    }
    else {
      height = parent.getHeight();
    }
    int tryCount = 0;
    int[] maxheight = new int[columnCount];
    while (tryCount < MAX_COUNT) {
      int x = 0;
      int y = 0;
      int width = parent.getWidth() / columnCount;
      int currentColumn = 0;
      int i = 0;
      for (Component component : components) {
        int componentHeight = component.getPreferredSize().height;
        if (y + componentHeight > height && atLeatOneComponentPerColumn(y) && notInLastColumn(currentColumn)) {
          currentColumn++;
          x += width;
          y = 0;
        }
        Rectangle position = positions[i];
        position.x = x;
        position.y = y;
        position.width = width;
        position.height = componentHeight;
        y += componentHeight;
        maxheight[currentColumn] = y;
        i++;
      }
      if (currentColumn == maxColumnCount - 1) {
        int max = maxExpectLast(maxheight);
        if (max < maxheight[currentColumn]) {
          height += (maxheight[currentColumn] - max) / maxColumnCount;
          tryCount++;
        }
        else {
          tryCount = MAX_COUNT;
        }
      }
      else {
        tryCount = MAX_COUNT;
      }
    }
    int i = 0;
    for (Component component : components) {
      Rectangle position = positions[i];
      component.setBounds(position.x, position.y, position.width, position.height);
      i++;
    }
  }

  private int maxExpectLast(int... elements) {
    int max = elements[0];
    for (int i = 0; i < elements.length - 1; i++) {
      int element = elements[i];
      max = Math.max(max, element);
    }
    return max;
  }

  private boolean notInLastColumn(int currentColumn) {
    return currentColumn + 1 < maxColumnCount;
  }

  private boolean atLeatOneComponentPerColumn(int y) {
    return y > 0;
  }

  public static void main(String[] args) {
    JPanel panel = new JPanel(new ColumnLayoutManager(MAX_COUNT));
    for (int i = 0; i < 11; i++) {
      panel.add(new JLabel("some info " + i));
    }
    GuiUtils.show(panel);
  }
}
