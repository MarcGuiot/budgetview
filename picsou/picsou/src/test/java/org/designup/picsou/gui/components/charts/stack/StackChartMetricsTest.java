package org.designup.picsou.gui.components.charts.stack;

import org.designup.picsou.gui.components.ChartTestCase;
import org.globsframework.utils.TestUtils;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionEvent;

public class StackChartMetricsTest extends ChartTestCase {
  private StackChartMetrics metrics;
  private StackChartDataset dataset = new StackChartDataset();
  private AbstractAction action = new AbstractAction() {
    public void actionPerformed(ActionEvent e) {
    }
  };

  protected void setUp() throws Exception {
    super.setUp();
    metrics = new StackChartMetrics(200, 200, getFontMetrics(), getFontMetrics(), 1000);
  }

  public void testStandardBlocks() throws Exception {

    dataset.add("item1", 300.00, action);
    dataset.add("item2", 500.00, action);
    dataset.add("item3", 200.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item2", "item1", "item3");
    checkBlockHeights(blocks, 100, 60, 40);
  }

  public void testOnlyOneItemPresentInDataset() throws Exception {

    dataset.add("item1", 1000.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item1");
    checkBlockHeights(blocks, 200);
    assertEquals("", blocks[0].barText);
  }

  public void testHiddenBlocks() throws Exception {

    dataset.add("item1", 300.00, action);
    dataset.add("item2", 500.00, action);
    dataset.add("item3", 100.00, action);
    dataset.add("item4", 5.00, action);
    dataset.add("item5", 25.00, action);
    dataset.add("item6", 80.00, action);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item2", "item1", "item3", "Other");
    checkBlockPercentages(blocks, "50%", "30%", "10%", "10%");
    checkBlockHeights(blocks, 100, 60, 20, 20);
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
}
