package org.designup.picsou.gui.budget.components;

import org.designup.picsou.gui.components.charts.DeltaGauge;
import org.designup.picsou.gui.components.charts.Gauge;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.exceptions.SplitsException;
import org.globsframework.gui.splits.layout.ComponentConstraints;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.repeat.RepeatLayout;
import org.globsframework.gui.views.Alignment;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static org.globsframework.utils.Utils.equal;

@SuppressWarnings("UnusedDeclaration")
@NoObfuscation
public class BudgetAreaSeriesLayout implements RepeatLayout {

  public static final int ROW_HEIGHT = 22;
  private static final int ROW_ITEM_COUNT = 7;
  private static final int SPACE = 4;
  private static final int FIRST_ROW_HEIGHT = 6;

  public void checkHeader(Splitter[] splitters, String repeatRef) {
    checkContent(splitters, repeatRef);
  }

  public void checkContent(Splitter[] splitterTemplates, String repeatRef) {
    if (splitterTemplates != null && splitterTemplates.length != ROW_ITEM_COUNT) {
      throw new SplitsException("Repeat '" + repeatRef + "' must have exactly " + ROW_ITEM_COUNT + " components per row");
    }
  }

  public void init(JPanel panel) {
    panel.setLayout(new Layout());
  }

  public void set(JPanel panel, List<ComponentConstraints[]> constraints) {
    panel.removeAll();
    int row = 0;
    for (ComponentConstraints[] constraintsArray : constraints) {
      for (int col = 0; col < ROW_ITEM_COUNT; col++) {
        Component component = constraintsArray[col].getComponent();
        panel.add(component, row * ROW_ITEM_COUNT + col);
      }
      row++;
    }
    panel.validate();
  }

  public void insert(JPanel panel, ComponentConstraints[] constraints, int row) {
    for (int col = 0; col < ROW_ITEM_COUNT; col++) {
      Component component = constraints[col].getComponent();
      panel.add(component, row * ROW_ITEM_COUNT + col);
    }
    panel.validate();
  }

  public void remove(JPanel panel, int row) {
    for (int col = 0; col < ROW_ITEM_COUNT; col++) {
      panel.remove(row * ROW_ITEM_COUNT);
    }
    panel.validate();
  }

  public void move(JPanel panel, int previousRow, int newRow) {
    Component[] componentRow = new Component[ROW_ITEM_COUNT];
    int index = previousRow * ROW_ITEM_COUNT;
    for (int col = 0; col < ROW_ITEM_COUNT; col++) {
      componentRow[col] = panel.getComponent(index);
      panel.remove(index);
    }
    for (int col = ROW_ITEM_COUNT - 1; col >= 0; col--) {
      panel.add(componentRow[col], newRow * ROW_ITEM_COUNT);
    }
    panel.validate();
  }

  public boolean managesInsets() {
    return false;
  }

  private class Layout implements LayoutManager {

    public void layoutContainer(Container panel) {
      Insets insets = panel.getInsets();
      int top = insets.top;
      int bottom = panel.getHeight() - insets.bottom;
      int left = insets.left;
      int width = panel.getWidth();
      int right = width - insets.right;

      int maxLabelWidth = 0;
      int maxToggleWidth = 10;
      int maxActualWidth = 0;
      int maxPlannedWidth = 0;
      int maxGaugeWidth = 20;
      int maxSlashWidth = 0;
      int maxDeltaGaugeWidth = 12;
      Component[] components = panel.getComponents();
      if (components == null) {
        return;
      }

      for (Component component : components) {
        double preferredWidth = component.getPreferredSize().getWidth();
        if (Strings.isNullOrEmpty(component.getName())) {
          continue;
        }
        if (equal(component.getName(), "titleSeries") || equal(component.getName(), "seriesName")) {
          maxLabelWidth = (int)Math.max(maxLabelWidth, preferredWidth);
        }
        else if (equal(component.getName(), "groupToggle")) {
          maxToggleWidth = (int)Math.max(maxToggleWidth, preferredWidth);
        }
        else if (equal(component.getName(), "gauge")) {
          maxGaugeWidth = (int)Math.max(maxGaugeWidth, preferredWidth);
        }
        else if (equal(component.getName(), "titleAmountReal") || equal(component.getName(), "observedSeriesAmount")) {
          maxActualWidth = (int)Math.max(maxActualWidth, preferredWidth);
        }
        else if (equal(component.getName(), "slash")) {
          maxSlashWidth = (int)Math.max(maxSlashWidth, preferredWidth);
        }
        else if (equal(component.getName(), "titleAmountPlanned") || equal(component.getName(), "plannedSeriesAmount")) {
          maxPlannedWidth = (int)Math.max(maxPlannedWidth, preferredWidth);
        }
        else if (equal(component.getName(), "deltaGauge")) {
          maxDeltaGaugeWidth = (int)Math.max(maxDeltaGaugeWidth, preferredWidth);
        }
      }

      int availableWidth = width - maxDeltaGaugeWidth - SPACE
                           - maxPlannedWidth - SPACE - maxSlashWidth - SPACE - maxActualWidth - SPACE
          - maxGaugeWidth - SPACE - maxToggleWidth - SPACE - maxLabelWidth;
      if (availableWidth > 0) {
        maxGaugeWidth += availableWidth;
      }

      int leftDeltaGauge = right - maxDeltaGaugeWidth;
      int leftPlanned = leftDeltaGauge - maxPlannedWidth - SPACE;
      int leftSlash = leftPlanned - SPACE - maxSlashWidth;
      int leftActual = leftSlash - SPACE - maxActualWidth;
      int leftGauge = leftActual - SPACE - maxGaugeWidth;
      int leftToggle = leftGauge - SPACE - maxToggleWidth;

      for (int i = 0; i < components.length; i++) {
        Component component = components[i];
        int row = i / ROW_ITEM_COUNT;
        boolean isHeader = row == 0;
        int rowBottom = top + FIRST_ROW_HEIGHT + row * ROW_HEIGHT;
        switch (i % ROW_ITEM_COUNT) {
          case 0: // Series
            layout(component, rowBottom, -3, left, components[i + 1].isEnabled() ? leftToggle : leftGauge, Alignment.RIGHT, Fill.NONE, isHeader);
            break;
          case 1: // Group toggle
            layout(component, rowBottom, -3, leftToggle, components[i].isEnabled() ? leftGauge : leftToggle, Alignment.LEFT, Fill.NONE, isHeader);
            break;
          case 2: // Gauge
            layout(component, rowBottom, -5, leftGauge, leftActual, Alignment.LEFT, Fill.HORIZONTAL, isHeader);
            break;
          case 3: // Actual
            layout(component, rowBottom, 4, leftActual, leftSlash, Alignment.RIGHT, Fill.NONE, isHeader);
            break;
          case 4: // Slash
            layout(component, rowBottom, 2, leftSlash, leftPlanned, Alignment.CENTER, Fill.NONE, isHeader);
            break;
          case 5: // Planned
            layout(component, rowBottom, 2, leftPlanned, leftDeltaGauge, Alignment.LEFT, Fill.NONE, isHeader);
            break;
          case 6: // Delta gauge
            layout(component, rowBottom, 4, leftDeltaGauge, right, Alignment.LEFT, Fill.NONE, isHeader);
            break;
        }
      }
    }

    private void layout(Component component, int rowBottom, int yOffset, int columnLeft, int columnRight, Alignment alignment, Fill fill, boolean isHeader) {
      if (isHeader) {
        alignment = Alignment.CENTER;
      }
      Dimension size = component.getPreferredSize();
      int columnWidth = columnRight - columnLeft;
      int componentWidth = Math.min(columnRight - columnLeft, size.width);
      switch (fill) {
        case HORIZONTAL:
          componentWidth = columnWidth - SPACE;
      }
      int left = columnLeft;
      switch (alignment) {
        case CENTER:
          left = columnLeft + columnWidth / 2 - componentWidth / 2 - SPACE / 2;
          break;
        case RIGHT:
          left = columnRight - componentWidth - SPACE;
          break;
      }
      int y = isHeader ? FIRST_ROW_HEIGHT : rowBottom + yOffset - ROW_HEIGHT / 2 + size.height / 2;
      Rectangle rect = new Rectangle(left, y, componentWidth, size.height);
      component.setBounds(rect);
    }

    public void addLayoutComponent(String name, Component comp) {
    }

    public void removeLayoutComponent(Component comp) {
    }

    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(500, computeHeight(parent));
    }

    private int computeHeight(Container parent) {
      return FIRST_ROW_HEIGHT + (parent.getComponentCount() / ROW_ITEM_COUNT) * ROW_HEIGHT + 1;
    }

    public Dimension minimumLayoutSize(Container parent) {
      return new Dimension(100, computeHeight(parent));
    }
  }

  private String getLabel(Component component) {
    String name = component.getName();
    if (Strings.isNullOrEmpty(name) || "null".equals(name)) {
      name = "";
    }
    else {
      name = "/" + name;
    }
    String label = "";
    if (component instanceof JButton) {
      label = "Button" + name + "/" + ((JButton)component).getText();
    }
    else if (component instanceof JLabel) {
      label = "Label" + name + "/" + ((JLabel)component).getText();
    }
    else if (component instanceof Gauge) {
      label = "Gauge" + name;
    }
    else if (component instanceof DeltaGauge) {
      label = "DeltaGauge" + name;
    }
    else {
      label = component.getClass().getSimpleName();
    }
    return label;
  }
}
