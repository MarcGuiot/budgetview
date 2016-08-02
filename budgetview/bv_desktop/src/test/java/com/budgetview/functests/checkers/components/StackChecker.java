package com.budgetview.functests.checkers.components;

import com.budgetview.functests.checkers.GuiChecker;
import com.budgetview.functests.checkers.SeriesEditionDialogChecker;
import com.budgetview.functests.checkers.ViewSelectionChecker;
import com.budgetview.desktop.components.charts.stack.StackChart;
import com.budgetview.desktop.components.charts.stack.StackChartDataset;
import com.budgetview.desktop.description.Formatting;
import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.uispec4j.Key;
import org.uispec4j.Panel;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;
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

  public void checkVisible() {
    UISpecAssert.assertTrue(new Assertion() {
      public void check() {
        if (!getChart().isVisible()) {
          throw new AssertionFailedError(name + " is not visible");
        }
      }
    });
  }

  public void checkHidden() {
    UISpecAssert.assertTrue(new Assertion() {
      public void check() {
        if (getChart().isVisible()) {
          throw new AssertionFailedError(name + " is visible");
        }
      }
    });
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

  public StackChecker select(String... items) {
    StackChart chart = getChart();
    chart.setSize(200, 200);
    boolean add = false;
    for (String item : items) {
      doClick(chart, item, add ? Key.Modifier.CONTROL : Key.Modifier.NONE, false);
      add = true;
    }
    return this;
  }

  public StackChecker addToSelection(String item) {
    StackChart chart = getChart();
    chart.setSize(200, 200);
    doClick(chart, item, Key.Modifier.CONTROL, false);
    return this;
  }

  private void doClick(StackChart chart, String item, Key.Modifier modifier, boolean useRightClick) {
    chart.paint(Empty.NULL_GRAPHICS_2D);
    Rectangle area = chart.getArea(item);
    if (area == null) {
      Assert.fail("Item " + item + " not found - actual labels: " + chart.getAreas());
    }
    click(chart, area, modifier, useRightClick);
  }

  public void checkRightClickOptions(String item, String... options) {
    openRightClickPopup(item).checkChoices(options);
  }

  public void rightClickAndSelect(String item, String option) {
    openRightClickPopup(item).click(option);
  }

  public SeriesEditionDialogChecker rightClickAndEditSeries(final String item, final String option) {
    PopupChecker popupChecker = openRightClickPopup(item);
    return SeriesEditionDialogChecker.open(popupChecker.triggerClick(option));
  }

  private PopupChecker openRightClickPopup(final String item) {
    select(item);
    final StackChart chart = getChart();
    if (!chart.isVisible()) {
      Assert.fail("Chart is not visible");
    }
    return new PopupChecker() {
      protected org.uispec4j.MenuItem openMenu() {
        return PopupMenuInterceptor.run(new Trigger() {
          public void run() throws Exception {
            doClick(chart, item, Key.Modifier.NONE, true);
          }
        });
      }
    };
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
      Assert.assertEquals(getErrorMessage(), Formatting.toString(expected), dataset.getTooltipText(index));
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

    public void dumpCode() {
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
    return (StackChart) panel.getAwtComponent();
  }

}
