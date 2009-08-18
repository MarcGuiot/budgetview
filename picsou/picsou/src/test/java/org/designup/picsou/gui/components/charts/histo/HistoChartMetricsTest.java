package org.designup.picsou.gui.components.charts.histo;

import org.designup.picsou.gui.components.ChartTestCase;
import org.designup.picsou.gui.components.charts.histo.painters.HistoLineDataset;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Arrays;
import java.util.List;

public class HistoChartMetricsTest extends ChartTestCase {

  public void testColumns() throws Exception {

    HistoChartMetrics metrics =
      new HistoChartMetrics(180, 135, getFontMetrics(), 10, 1000, 3000, false);

    assertEquals(30, metrics.left(0));
    assertEquals(45, metrics.left(1));
    assertEquals(60, metrics.left(2));
    assertEquals(165, metrics.left(9));

    try {
      assertEquals(180, metrics.left(10));
    }
    catch (InvalidParameter e) {
      assertEquals("Invalid index 10, chart only contains 10 columns", e.getMessage());
    }

    assertEquals(45, metrics.right(0));
    assertEquals(60, metrics.right(1));
    assertEquals(75, metrics.right(2));
    assertEquals(180, metrics.right(9));
    try {
      assertEquals(200, metrics.right(10));
    }
    catch (InvalidParameter e) {
      assertEquals("Invalid index 10, chart only contains 10 columns", e.getMessage());
    }
  }

  public void testVerticalPositions() throws Exception {

    HistoChartMetrics metrics =
      new HistoChartMetrics(180, 140, getFontMetrics(), 10, 1000, 3000, false);

    int margin = 10;
    assertEquals(margin + 78, metrics.y(0));
    assertEquals(margin + 0, metrics.y(3000));
    assertEquals(margin + 105, metrics.y(-1000));

    assertEquals(138, metrics.labelY());
    assertEquals(65, metrics.labelX("A", 2));
    assertEquals(62, metrics.labelX("AA", 2));
    assertEquals(80, metrics.labelX("A", 3));
  }

  public void testVerticalPositionsWithNoNegativeValues() throws Exception {
    HistoChartMetrics metrics =
      new HistoChartMetrics(180, 145, getFontMetrics(), 10, 0, 100, false);

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

  public void testColumnAt() throws Exception {
    HistoChartMetrics metrics =
      new HistoChartMetrics(150, 145, getFontMetrics(), 10, 0, 100, false);
    assertEquals(25, metrics.left(0));
    assertEquals(0, metrics.getColumnAt(35));
    assertEquals(1, metrics.getColumnAt(45));
    assertEquals(2, metrics.getColumnAt(55));
    assertEquals(9, metrics.getColumnAt(135));
  }

  public void testSectionBlocks() throws Exception {
    HistoChartMetrics metrics =
      new HistoChartMetrics(150, 130, getFontMetrics(), 10, 0, 100, false);

    HistoLineDataset dataset = new HistoLineDataset();
    dataset.add(1, 1.0, "item1", "section A", false);
    dataset.add(2, 2.0, "item2", "section A", false);
    dataset.add(3, 3.0, "item3", "section B", false);
    dataset.add(4, 4.0, "item4", "section B", false);
    dataset.add(5, 5.0, "item5", "section B", false);
    dataset.add(6, 6.0, "item6", "section B", false);
    dataset.add(7, 7.0, "item7", "section B", false);
    dataset.add(8, 8.0, "item8", "section B", false);
    dataset.add(9, 9.0, "item9", "section B", false);
    dataset.add(10, 10.0, "item10", "section C", false);

    List<HistoChartMetrics.Section> sections = metrics.getSections(dataset);
    assertEquals(3, sections.size());
    checkSection(sections.get(0), "section A", 15, 128, 25, 24, 115, 15);
    checkSection(sections.get(1), "section B", 69, 128, 49, 84, 115, 15);
    checkSection(sections.get(2), "section C", 119, 128, 133, 17, 115, 15);
  }

  private void checkSection(HistoChartMetrics.Section section,
                            String text,
                            int textX, int textY,
                            int blockX, int blockWidth, int blockY, int blockHeight) {
    HistoChartMetrics.Section expected =
      new HistoChartMetrics.Section(text, textX, textY, blockX, blockWidth, blockY, blockHeight);
    assertEquals(expected.toString(), section.toString());
  }

  private void checkScaleValues(int panelHeight, int maxNegative, int maxPositive, double[] expected) {
    HistoChartMetrics metrics =
      new HistoChartMetrics(200, panelHeight, getFontMetrics(), 10, maxNegative, maxPositive, false);
    double[] actual = metrics.scaleValues();
    Arrays.sort(actual);
    TestUtils.assertEquals(actual, expected);
  }
}
