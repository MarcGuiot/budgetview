package org.designup.picsou.gui.components.charts.stack;

import org.designup.picsou.gui.components.ChartTestCase;
import org.globsframework.utils.TestUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class StackChartMetricsTest extends ChartTestCase {
  private StackChartMetrics metrics;
  private StackChartDataset dataset = new StackChartDataset();
  private AbstractAction action = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
    }
  };

  protected void setUp() throws Exception {
    super.setUp();
    metrics = new StackChartMetrics(210, 200, getFontMetrics(), getFontMetrics(), getFontMetrics(), 1000);
  }

  public void testStandardBlocks() throws Exception {

    dataset.add("item1", 300.00, action);
    dataset.add("item2", 500.00, action);
    dataset.add("item3", 200.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item2", "item1", "item3");
    checkBlockHeights(blocks, 100, 60, 40);
    checkBlockSelectionIndexes(blocks, 0, 1, 2);
  }

  public void testOnlyOneItemPresentInDataset() throws Exception {

    dataset.add("item1", 1000.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item1");
    checkBlockHeights(blocks, 200);
    assertEquals("", blocks[0].barText);
    checkBlockSelectionIndexes(blocks, 0);
  }

  public void testOtherBlockWithFullHeight() throws Exception {

    dataset.add("item1", 300.00, action);
    dataset.add("item2", 500.00, action);
    dataset.add("item3", 100.00, action);
    dataset.add("item4", 5.00, action);
    dataset.add("item5", 25.00, action);
    dataset.add("item6", 80.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item2", "item1", "item3", "Other");
    checkBlockPercentages(blocks, "50%", "30%", "10%", "10%");
    checkBlockHeights(blocks, 100, 60, 20, 22);
    checkBlockSelectionIndexes(blocks, 0, 1, 2, -1);
  }

  public void testOtherBlockWithPartialHeight() throws Exception {
    dataset.add("item1", 500.00, action);
    dataset.add("item2", 5.00, action);
    dataset.add("item3", 5.00, action);
    dataset.add("item4", 5.00, action);
    dataset.add("item5", 5.00, action);
    dataset.add("item6", 5.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item1", "Other");
    checkBlockPercentages(blocks, "95%", "5%");
    checkBlockHeights(blocks, 100, 5);
    checkBlockSelectionIndexes(blocks, 0, -1);
  }

  public void testVeryLongText() throws Exception {

    dataset.add("item1", 300.00, action);
    dataset.add("item2 with a very long label", 500.00, action);
    dataset.add("item3", 200.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item2 with a very...", "item1", "item3");
  }

  private void checkBlockLabels(StackChartBlock[] blocks, String... expected) {
    List<String> actual = new ArrayList<String>();
    for (StackChartBlock block : blocks) {
      actual.add(block.label);
    }
    TestUtils.assertEquals(actual, expected);
  }

  private void checkBlockHeights(StackChartBlock[] blocks, Integer... expected) {
    List<Integer> actual = new ArrayList<Integer>();
    for (StackChartBlock block : blocks) {
      actual.add(block.blockHeight);
    }
    TestUtils.assertEquals(actual, expected);
  }

  private void checkBlockPercentages(StackChartBlock[] blocks, String... expected) {
    List<String> actual = new ArrayList<String>();
    for (StackChartBlock block : blocks) {
      actual.add(block.barText);
    }
    TestUtils.assertEquals(actual, expected);
  }

  private void checkBlockSelectionIndexes(StackChartBlock[] blocks, Integer... expected) {
    List<Integer> actual = new ArrayList<Integer>();
    for (StackChartBlock block : blocks) {
      actual.add(block.datasetIndex);
    }
    TestUtils.assertEquals(actual, expected);
  }
}
