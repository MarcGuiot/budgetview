package org.designup.picsou.functests.checkers.components;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.GuiChecker;
import org.designup.picsou.functests.checkers.ViewSelectionChecker;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.uispec4j.Key;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.toolkit.Empty;

import java.awt.*;

public class StackChecker extends GuiChecker {

  private Panel panel;
  private Window window;
  private String containerName;
  private String name;

  public StackChecker(Panel panel) {
    this.panel = panel;
  }

  public StackChecker(Window window, String containerName, String name) {
    this.window = window;
    this.containerName = containerName;
    this.name = name;
  }

  public DatasetChecker getLeftDataset() {
    return new DatasetChecker(getChart().getLeftDataset());
  }

  public DatasetChecker getRightDataset() {
    return new DatasetChecker(getChart().getRightDataset());
  }

  public DatasetChecker getSingleDataset() {
    StackChart chart = getChart();
    Assert.assertTrue("Chart is in double-stack configuration", chart.getRightDataset() == null);
    return new DatasetChecker(chart.getLeftDataset());
  }

  public void select(String... items) {
    StackChart chart = getChart();
    chart.setSize(200, 200);
    boolean add = false;
    for (String item : items) {
      doClick(chart, item, add ? Key.Modifier.CONTROL : Key.Modifier.NONE);
      add = true;
    }
  }

  public void addToSelection(String item) {
    StackChart chart = getChart();
    chart.setSize(200, 200);
    doClick(chart, item, Key.Modifier.CONTROL);
  }

  private void doClick(StackChart chart, String item, Key.Modifier modifier) {
    chart.paint(Empty.NULL_GRAPHICS_2D);
    Rectangle area = chart.getArea(item);
    if (area == null) {
      Assert.fail("Item " + item + " not found - actual labels: " + chart.getAreas());
    }
    click(chart, area, modifier);
  }

  public class DatasetChecker {
    private StackChartDataset dataset;

    public DatasetChecker(StackChartDataset dataset) {
      this.dataset = dataset;
    }

    public DatasetChecker checkSize(int size) {
      Assert.assertEquals(getErrorMessage(), size, dataset.size());
      return this;
    }

    public DatasetChecker checkValue(String label, double expected, boolean selected) {
      int index = getIndex(label);
      Assert.assertEquals(getErrorMessage(), expected, dataset.getValue(index), 0.01);
      Assert.assertEquals(getErrorMessage(), selected, dataset.isSelected(index));
      return this;
    }

    public DatasetChecker checkValue(String label, double expected) {
      checkValue(label, expected, false);
      return this;
    }

    private int getIndex(String label) {
      int index = dataset.indexOf(label);
      if (index < 0) {
        Assert.fail("'" + label + "' not found in dataset - actual contents:\n" + dataset);
      }
      return index;
    }

    private String getErrorMessage() {
      return "Actual dataset contents:\n" + dataset;
    }

    public void dump() {
      if (dataset == null) {
        Assert.fail("Insert: \n  .checkEmpty();");
      }

      StringBuilder builder = new StringBuilder();
      builder.append("  .checkSize(").append(dataset.size()).append(")\n");
      for (int i = 0; i < dataset.size(); i++) {
        double value = dataset.getValue(i);
        builder.append("  .checkValue(\"").append(dataset.getLabel(i)).append("\", ")
          .append(StackChecker.this.toString(value));
        if (dataset.isSelected(i)) {
          builder.append(", true");
        }
        builder.append(")");
        if (i < dataset.size() - 1) {
          builder.append("\n");
        }
        else {
          builder.append(";");
        }
      }
      Assert.fail("Insert: \n" + builder.toString());
    }

    public void checkEmpty() {
      if (dataset != null) {
        checkSize(0);
      }
    }
  }

  private StackChart getChart() {
    if (panel == null) {
      ViewSelectionChecker views = new ViewSelectionChecker(window);
      views.selectAnalysis();
      panel = window.getPanel(containerName).getPanel(name);
    }
    return (StackChart)panel.getAwtComponent();
  }

}
