package org.designup.picsou.gui.components.charts.histo;

import com.budgetview.shared.gui.TextMetrics;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import com.budgetview.shared.gui.histochart.HistoChartMetrics;
import org.designup.picsou.gui.components.ChartTestCase;
import org.designup.picsou.gui.components.charts.histo.line.HistoLineDataset;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.Arrays;
import java.util.List;

public class HistoChartMetricsTest extends ChartTestCase {

  public void testColumns() throws Exception {

    HistoChartMetrics metrics =
      createMetrics(180, 135, 10, 1000, 3000, true, false, false, false);

    assertEquals(40, metrics.left(0));
    assertEquals(54, metrics.left(1));
    assertEquals(68, metrics.left(2));
    assertEquals(166, metrics.left(9));

    try {
      assertEquals(180, metrics.left(10));
    }
    catch (InvalidParameter e) {
      assertEquals("Invalid index 10, chart only contains 10 columns", e.getMessage());
    }

    assertEquals(54, metrics.right(0));
    assertEquals(68, metrics.right(1));
    assertEquals(82, metrics.right(2));
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
      createMetrics(180, 140, 10, 1000, 3000, true, false, false, false);

    assertEquals(91, metrics.y(0));
    assertEquals(5, metrics.y(3000));
    assertEquals(120, metrics.y(-1000));

    assertEquals(138, metrics.labelY());
    assertEquals(73, metrics.labelX("A", 2));
    assertEquals(70, metrics.labelX("AA", 2));
    assertEquals(87, metrics.labelX("A", 3));
  }

  public void testVerticalPositionsWithNoNegativeValues() throws Exception {
    HistoChartMetrics metrics =
      createMetrics(180, 145, 10, 0, 100, true, false, false, false);

    assertEquals(125, metrics.y(0));
    assertEquals(5, metrics.y(100));
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
      createMetrics(150, 145, 10, 0, 100, true, false, false, false);
    assertEquals(40, metrics.left(0));
    assertEquals(-1, metrics.getColumnAt(35));
    assertEquals(0, metrics.getColumnAt(45));
    assertEquals(1, metrics.getColumnAt(55));
    assertEquals(2, metrics.getColumnAt(72));
    assertEquals(9, metrics.getColumnAt(145));
  }

  public void testSectionBlocks() throws Exception {
    HistoChartMetrics metrics =
      createMetrics(150, 130, 10, 0, 100, true, true, false, false);

    HistoLineDataset dataset = new HistoLineDataset(null);
    dataset.add(1, 1.0, "item1", "Item 1 A", "section A", false, false, false);
    dataset.add(2, 2.0, "item2", "Item 2 A", "section A", false, false, false);
    dataset.add(3, 3.0, "item3", "Item 3 B", "section B", false, false, false);
    dataset.add(4, 4.0, "item4", "Item 4 B", "section B", false, false, false);
    dataset.add(5, 5.0, "item5", "Item 5 B", "section B", false, false, false);
    dataset.add(6, 6.0, "item6", "Item 6 B", "section B", true, false, false);
    dataset.add(7, 7.0, "item7", "Item 7 B", "section B", false, true, false);
    dataset.add(8, 8.0, "item8", "Item 8 B", "section B", false, true, false);
    dataset.add(9, 9.0, "item9", "Item 9 B", "section B", false, true, false);
    dataset.add(10, 10.0, "item10", "Item 10 C", "section C", false, true, false);

    List<HistoChartMetrics.Section> sections = metrics.getSections(dataset);
    assertEquals(3, sections.size());
    checkSection(sections.get(0), "section A", 29, 15, 40, 22, 110, 20);
    checkSection(sections.get(1), "section B", 78, 15, 62, 77, 110, 20);
    checkSection(sections.get(2), "section C", 122, 15, 139, 11, 110, 20);
  }

  private void checkSection(HistoChartMetrics.Section section,
                            String text,
                            int textX, int textY,
                            int blockX, int blockWidth, int blockY, int blockHeight) {
    HistoChartMetrics.Section expected =
      new HistoChartMetrics.Section(text, textX, textY, blockX, blockWidth, blockY, blockHeight, 10, 20);
    assertEquals(expected.toString(), section.toString());
  }

  private void checkScaleValues(int panelHeight, int maxNegative, int maxPositive, double[] expected) {
    HistoChartMetrics metrics =
      createMetrics(200, panelHeight, 10, maxNegative, maxPositive, false, true, false, false);
    double[] actual = metrics.scaleValues();
    Arrays.sort(actual);
    TestUtils.assertEquals(actual, expected);
  }

  private HistoChartMetrics createMetrics(int panelWidth,
                                          int panelHeight,
                                          int columnCount,
                                          double maxNegativeValue,
                                          double maxPositiveValue,
                                          boolean drawLabels,
                                          boolean drawSections,
                                          boolean drawInnerLabels,
                                          boolean snapToScale) {
    return new HistoChartMetrics(panelWidth, panelHeight,
                                 new TextMetrics() {
                                   public int stringWidth(String text) {
                                     return text.length() * 5;
                                   }

                                   public int getAscent() {
                                     return 5;
                                   }
                                 },
                                 columnCount,
                                 maxNegativeValue, maxPositiveValue,
                                 new HistoChartConfig(drawLabels, drawSections, drawInnerLabels, true, true, true, false, true, true, true),
                                 true, snapToScale);

  }
}
