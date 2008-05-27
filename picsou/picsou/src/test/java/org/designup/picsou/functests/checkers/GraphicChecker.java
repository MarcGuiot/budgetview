package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.crossbowlabs.globs.utils.Range;
import org.crossbowlabs.globs.utils.TestUtils;
import org.uispec4j.Panel;
import org.uispec4j.assertion.Assertion;
import org.uispec4j.assertion.UISpecAssert;
import org.designup.picsou.gui.graphics.HistoricalChart;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GraphicChecker extends DataChecker {
  private ChartPanel chartPanel;
  private Panel panel;

  public GraphicChecker(Panel panel) {
    this.panel = panel;
  }

  public ContentChecker initCheck() {
    return new ContentChecker();
  }

  public void assertVisible(boolean visible) {
    Assert.assertEquals(visible, getChartPanel().isVisible());
  }

  private ChartPanel getChartPanel() {
    if (chartPanel == null) {
      chartPanel = (ChartPanel) panel.findSwingComponent(ChartPanel.class, "historicalChart");
      Assert.assertNotNull(chartPanel);
    }
    return chartPanel;
  }

  public class ContentChecker {

    private int[] months;
    private Double[] income;
    private Double[] expenses;

    public ContentChecker setMonths(int... months) {
      this.months = months;
      return this;
    }

    public ContentChecker setIncome(Double... values) {
      this.income = values;
      return this;
    }

    public ContentChecker setExpenses(Double... values) {
      this.expenses = values;
      return this;
    }

    public ContentChecker setNoIncome() {
      this.income = null;
      return this;
    }

    public ContentChecker setNoExpenses() {
      this.expenses = null;
      return this;
    }

    public void check() {
      Assert.assertNotNull("periods must be set", months);

      XYSeriesCollection dataset = getDataset();
      checkValues(dataset, expenses, HistoricalChart.EXPENSES_ROW);
      checkValues(dataset, income, HistoricalChart.INCOME_ROW);
    }

    private void checkValues(XYSeriesCollection dataset, Double[] expected, String key) {
      if (expected != null) {
        int index = dataset.indexOf(key);
        Assert.assertTrue("Series '" + key + "' not found", index >= 0);
        XYSeries series = dataset.getSeries(index);
        List<Number> actualValues = new ArrayList<Number>();
        List<Integer> actualMonths = new ArrayList<Integer>();
        for (Object item : series.getItems()) {
          XYDataItem dataItem = (XYDataItem) item;
          double x = (Double) dataItem.getX();
          actualMonths.add((int) x);
          actualValues.add(dataItem.getY());
        }
        TestUtils.assertEquals(actualValues, expected);
        Assert.assertEquals(actualMonths.size(), months.length);
      }
      else {
        Assert.assertEquals(-1, dataset.indexOf(key));
      }
    }
  }

  public MarkerChecker initMarker() {
    return new MarkerChecker();
  }

  public class MarkerChecker {
    private List<Range<Double>> expectedRanges = new ArrayList<Range<Double>>();

    public MarkerChecker add(double min, double max) {
      expectedRanges.add(new Range<Double>(min, max));
      return this;
    }

    public void check() {
      UISpecAssert.assertTrue(new Assertion() {
        public void check() throws Exception {
          Collection markers = getPlot().getDomainMarkers(Layer.BACKGROUND);
          List<Range<Double>> actual = new ArrayList<Range<Double>>();
          if (markers == null) {
            Assert.assertTrue("No markers found", expectedRanges.isEmpty());
            return;
          }
          for (Object m : markers) {
            IntervalMarker marker = (IntervalMarker) m;
            actual.add(new Range(marker.getStartValue(), marker.getEndValue()));
          }
          TestUtils.assertEquals(expectedRanges, actual);
        }
      });
    }
  }

  /**
   * indexes start at 0
   */
  public void selectMonthColumn(int index) {
    getPlot().setDomainCrosshairValue(index + 1, true);
  }

  private XYSeriesCollection getDataset() {
    XYPlot plot = getPlot();
    return (XYSeriesCollection) plot.getDataset();
  }

  private XYPlot getPlot() {
    JFreeChart chart = getChartPanel().getChart();
    return chart.getXYPlot();
  }
}