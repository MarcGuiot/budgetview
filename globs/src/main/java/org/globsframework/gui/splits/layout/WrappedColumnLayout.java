package org.globsframework.gui.splits.layout;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class WrappedColumnLayout implements LayoutManager {
  private int maxColumnCount;
  private static final int MAX_COUNT = 3;

  public WrappedColumnLayout() {
    this(3);
  }

  public WrappedColumnLayout(int maxColumnCount) {
    this.maxColumnCount = maxColumnCount;
  }

  public void addLayoutComponent(String name, Component comp) {
  }

  public void removeLayoutComponent(Component comp) {
  }

  interface SizeAccesor {
    Dimension getSize(Component component);
  }

  public Dimension preferredLayoutSize(Container parent) {
    return getAdjustedSize(parent, new SizeAccesor() {
      public Dimension getSize(Component component) {
        return component.getPreferredSize();
      }
    });
  }

  public Dimension minimumLayoutSize(Container parent) {
    return getAdjustedSize(parent, new SizeAccesor() {
      public Dimension getSize(Component component) {
        return component.getMinimumSize();
      }
    });
  }

  private Dimension getAdjustedSize(Container parent, SizeAccesor sizeAccesor) {
    Component[] components = parent.getComponents();
    Rectangle[] positions = new Rectangle[components.length];
    for (int i = 0; i < positions.length; i++) {
      positions[i] = new Rectangle();
    }
    double totalHeight = 0;
    for (Component component : components) {
      Dimension dimension = sizeAccesor.getSize(component);
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
        int componentHeight = sizeAccesor.getSize(component).height;
        if (y + componentHeight > height && atLeatOneComponentPerColumn(y) && notInLastColumn(currentColumn)) {
          x += maxWidth[currentColumn];
          y = 0;
          currentColumn++;
        }
        Rectangle position = positions[i];
        position.x = x;
        position.y = y;
        position.width = width;
        position.height = componentHeight;
        y += componentHeight;
        maxheight[currentColumn] = y;
        maxWidth[currentColumn] = Math.max(maxWidth[currentColumn],
                                           sizeAccesor.getSize(component).width);
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
    Dimension preferredSize = new Dimension();
    for (int width : maxWidth) {
      preferredSize.width += width;
    }
    preferredSize.height = (int)height;
    return preferredSize;
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
      int availableWidth = parent.getWidth() / columnCount;
      int maxPreferedWitdth = 0;
      int currentColumn = 0;
      int i = 0;
      for (Component component : components) {
        Dimension preferredSize = component.getPreferredSize();
        Dimension minSize = component.getMinimumSize();
        int componentHeight = preferredSize.height;
        if (y + componentHeight > height && atLeatOneComponentPerColumn(y) && notInLastColumn(currentColumn)) {
          currentColumn++;
          x += maxPreferedWitdth;
          y = 0;
          maxPreferedWitdth = 0;
        }
        Rectangle position = positions[i];
        position.x = x;
        position.y = y;
        position.width = availableWidth > preferredSize.width ? preferredSize.width :
                         (availableWidth < minSize.width ? minSize.width : availableWidth);
        position.height = componentHeight;
        maxPreferedWitdth = Math.max(maxPreferedWitdth, position.width);
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
    JPanel panel = new JPanel(new WrappedColumnLayout(MAX_COUNT));
    for (int i = 0; i < 11; i++) {
      panel.add(new JLabel("some info " + i));
    }
    GuiUtils.show(panel);
  }
}
