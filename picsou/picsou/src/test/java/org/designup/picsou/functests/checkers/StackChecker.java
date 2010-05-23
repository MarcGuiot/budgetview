package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.description.Formatting;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.interception.toolkit.Empty;

import java.awt.*;

public class StackChecker extends GuiChecker {

  private Window window;
  private String containerName;
  private String name;

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

  public void click(final double xPercent, final double yPercent) {
    final StackChart chart = getChart();
    chart.paint(Empty.NULL_GRAPHICS_2D);
    Dimension size = chart.getSize();
    chart.mouseMoved((int)(size.width * xPercent), (int)(size.height * yPercent));
    chart.click();
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

  }

  private StackChart getChart() {
    Panel panel = window.getPanel(containerName).getPanel(name);
    return (StackChart)panel.getAwtComponent();
  }

}
