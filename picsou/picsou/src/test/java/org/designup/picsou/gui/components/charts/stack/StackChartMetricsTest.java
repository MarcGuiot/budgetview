package org.designup.picsou.gui.components.charts.stack;

import org.designup.picsou.gui.components.ChartTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Key;
import org.globsframework.utils.TestUtils;

import java.util.ArrayList;
import java.util.List;

public class StackChartMetricsTest extends ChartTestCase {
  private StackChartMetrics metrics;
  private StackChartDataset dataset = new StackChartDataset();
  private Key key1 = Key.create(DummyObject.TYPE, 1);
  private Key key2 = Key.create(DummyObject.TYPE, 2);
  private Key key3 = Key.create(DummyObject.TYPE, 3);
  private Key key4 = Key.create(DummyObject.TYPE, 4);
  private Key key5 = Key.create(DummyObject.TYPE, 5);
  private Key key6 = Key.create(DummyObject.TYPE, 6);

  protected void setUp() throws Exception {
    super.setUp();
    metrics = new StackChartMetrics(210, 200, getFontMetrics(), getFontMetrics(), getFontMetrics(), 1000);
  }

  public void testStandardBlocks() throws Exception {

    dataset.add("item1", 300.00, key1);
    dataset.add("item2", 500.00, key2);
    dataset.add("item3", 200.00, key3);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item2", "item1", "item3");
    checkBlockHeights(blocks, 100, 60, 40);
    checkBlockSelectionIndices(blocks, key2, key1, key3);
  }

  public void testOnlyOneItemPresentInDataset() throws Exception {

    dataset.add("item1", 1000.00, key1);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item1");
    checkBlockHeights(blocks, 200);
    assertEquals("", blocks[0].barText);
    checkBlockSelectionIndices(blocks, key1);
  }

  public void testOtherBlockWithFullHeight() throws Exception {

    dataset.add("item1", 300.00, key1);
    dataset.add("item2", 500.00, key2);
    dataset.add("item3", 100.00, key3);
    dataset.add("item4", 5.00, key4);
    dataset.add("item5", 25.00, key5);
    dataset.add("item6", 80.00, key6);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item2", "item1", "item3", "Other");
    checkBlockPercentages(blocks, "50%", "30%", "10%", "10%");
    checkBlockHeights(blocks, 100, 60, 20, 22);
    checkBlockSelectionIndices(blocks, key2, key1, key3, null);
  }

  public void testOtherBlockWithPartialHeight() throws Exception {
    dataset.add("item1", 500.00, key1);
    dataset.add("item2", 5.00, key2);
    dataset.add("item3", 5.00, key3);
    dataset.add("item4", 5.00, key4);
    dataset.add("item5", 5.00, key5);
    dataset.add("item6", 5.00, key6);

    StackChartBlock[] blocks = metrics.computeBlocks(dataset);
    checkBlockLabels(blocks, "item1", "Other");
    checkBlockPercentages(blocks, "95%", "5%");
    checkBlockHeights(blocks, 100, 5);
    checkBlockSelectionIndices(blocks, key1, null);
  }

  public void testVeryLongText() throws Exception {

    dataset.add("item1", 300.00, key1);
    dataset.add("item2 with a very long label", 500.00, key2);
    dataset.add("item3", 200.00, key3);

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

  private void checkBlockSelectionIndices(StackChartBlock[] blocks, Key... expected) {
    List<Key> actual = new ArrayList<Key>();
    for (StackChartBlock block : blocks) {
      actual.add(block.key);
    }
    TestUtils.assertEquals(actual, expected);
  }
}
