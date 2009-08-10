package org.designup.picsou.gui.components.charts.histo;

import junit.framework.TestCase;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.designup.picsou.gui.components.ChartTestCase;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class HistoChartMetricsTest extends ChartTestCase {

  public void testColumns() throws Exception {

    HistoChartMetrics metrics =
      new HistoChartMetrics(200, 140, getFontMetrics(), 10, 1000, 3000);

    assertEquals(50, metrics.left(0));
    assertEquals(65, metrics.left(1));
    assertEquals(80, metrics.left(2));
    assertEquals(185, metrics.left(9));

    try {
      assertEquals(200, metrics.left(10));
    }
    catch (InvalidParameter e) {
      assertEquals("Invalid index 10, chart only contains 10 columns", e.getMessage());
    }

    assertEquals(65, metrics.right(0));
    assertEquals(80, metrics.right(1));
    assertEquals(95, metrics.right(2));
    assertEquals(200, metrics.right(9));
    try {
      assertEquals(200, metrics.right(10));
    }
    catch (InvalidParameter e) {
      assertEquals("Invalid index 10, chart only contains 10 columns", e.getMessage());
    }
  }

  public void testVerticalPositions() throws Exception {

    HistoChartMetrics metrics =
      new HistoChartMetrics(200, 140, getFontMetrics(), 10, 1000, 3000);

    int margin = 10;
    assertEquals(margin + 75, metrics.y(0));
    assertEquals(margin + 0, metrics.y(3000));
    assertEquals(margin + 100, metrics.y(-1000));

    assertEquals(136, metrics.labelY());
    assertEquals(85, metrics.labelX("A", 2));
    assertEquals(82, metrics.labelX("AA", 2));
    assertEquals(100, metrics.labelX("A", 3));
  }

  public void testVerticalPositionsWithNoNegativeValues() throws Exception {
    HistoChartMetrics metrics =
      new HistoChartMetrics(200, 150, getFontMetrics(), 10, 0, 100);

    assertEquals(130, metrics.y(0));
    assertEquals(10, metrics.y(100));
  }

  public void testScaleValues() throws Exception {
    checkScaleValues(0, 0, 100,
                     new double[0]);
    checkScaleValues(140, 0, 900,
                     new double[]{0.0, 250.0, 500.0, 750.0});
    checkScaleValues(140, 1000, 3000,
                     new double[]{0.0, 1000.0, 2000.0, 3000.0});
    checkScaleValues(180, 1000, 3000,
                     new double[]{-1000.0, 0.0, 1000.0, 2000.0, 3000.0});
    checkScaleValues(600, 1000, 3000,
                     new double[]{-1000.0, -750.0, -500.0, -250.0,
                                  0.0, 250.0, 500.0, 750.0, 1000.0, 1250.0, 1500.0, 1750.0, 2000.0, 2250.0,
                                  2500.0, 2750.0, 3000.0});
  }

  private void checkScaleValues(int panelHeight, int maxNegative, int maxPositive, double[] expected) {
    HistoChartMetrics metrics =
      new HistoChartMetrics(200, panelHeight, getFontMetrics(), 10, maxNegative, maxPositive);
    double[] actual = metrics.scaleValues();
    Arrays.sort(actual);
    TestUtils.assertEquals(actual, expected);
  }
}
