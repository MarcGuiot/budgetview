package org.designup.picsou.gui.components.charts.histo.button;

import junit.framework.TestCase;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Key;

public class HistoButtonDatasetTest extends TestCase {

  private final Key key1 = Key.create(DummyObject.TYPE, 1);
  private final Key key2 = Key.create(DummyObject.TYPE, 2);
  private final Key key3 = Key.create(DummyObject.TYPE, 3);

  private HistoButtonDataset dataset;

  private static final int FIRST_MONTH = 201011;
  private static final int LAST_MONTH = 201202;
  private static final int CURRENT_MONTH = 201102;

  public void setUp() throws Exception {
    dataset = new HistoButtonDataset("");
    for (int month = FIRST_MONTH; month <= LAST_MONTH; month = Month.next(month)) {
      dataset.addColumn(month, Month.getShortMonthLabel(month), "", Month.toYearString(month),
                        month == CURRENT_MONTH, month > CURRENT_MONTH, false);
    }
  }

  public void testOneRow() throws Exception {
    dataset.addButton(201103, 201106, "1", key1, "");
    dataset.addButton(201109, 201110, "2", key2, "");

    checkBlocks(" ----1111--22---- ");
  }

  public void testOneRowWithContiguousBlocks() throws Exception {
    dataset.addButton(201011, 201106, "1", key1, "");
    dataset.addButton(201107, 201110, "2", key2, "");
    dataset.addButton(201111, 201202, "3", key2, "");

    checkBlocks(" 1111111122223333 ");
  }

  public void testTwoRows() throws Exception {
    dataset.addButton(201103, 201109, "1", key1, "");
    dataset.addButton(201109, 201110, "2", key2, "");
    dataset.addButton(201101, 201105, "3", key3, "");

    checkBlocks(" --33333---22---- \n" +
                " ----1111111----- ");
  }

  public void testTruncatedOnBothSides() throws Exception {
    dataset.addButton(201006, 201206, "1", key1, "");
    dataset.addButton(201103, 201109, "2", key2, "");
    dataset.addButton(201105, 201107, "3", key3, "");

    checkBlocks("x1111111111111111x\n" +
                " ----2222222----- \n" +
                " ------333------- ");
  }

  public void testTruncatedOnEnd() throws Exception {
    dataset.addButton(201106, 201206, "1", key1, "");
    dataset.addButton(201103, 201109, "2", key2, "");
    dataset.addButton(201105, 201107, "3", key3, "");

    checkBlocks(" ----2222222----- \n" +
                " ------333------- \n" +
                " -------111111111x");
  }

  public void testOverlappingBorders() throws Exception {
    dataset.addButton(201011, 201104, "1", key1, "");
    dataset.addButton(201104, 201110, "2", key2, "");
    dataset.addButton(201110, 201202, "3", key3, "");

    checkBlocks(" 111111-----33333 \n" +
                " -----2222222---- ");
  }

  public void testBlocksHiddenBeforeAndAfter() throws Exception {
    dataset.addButton(201008, 201010, "1", key1, "");
    dataset.addButton(201203, 201205, "2", key2, "");
    dataset.addButton(201104, 201110, "3", key3, "");

    checkBlocks(" -----3333333---- ");
  }

  private void checkBlocks(String expected) {
    HistoButtonBlock[] blocks = dataset.getBlocks();
    char[][] result = new char[dataset.getRowCount()][dataset.size() + 2];
    for (int row = 0; row < result.length; row++) {
      for (int index = 0; index < result[row].length - 2; index++) {
        result[row][index + 1] = '-';
      }
      result[row][0] = ' ';
      result[row][result[row].length - 1] = ' ';
    }
    for (HistoButtonBlock block : blocks) {
      for (int index = block.minIndex; index <= block.maxIndex; index++) {
        result[block.row][index + 1] = block.label.charAt(0);
      }
      if (block.truncatedMin) {
        result[block.row][0] = 'x';
      }
      if (block.truncatedMax) {
        result[block.row][result[block.row].length - 1] = 'x';
      }
    }
    StringBuilder builder = new StringBuilder();
    boolean first = true;
    for (char[] chars : result) {
      if (!first) {
        builder.append("\n");
      }
      builder.append(chars);
      first = false;
    }
    assertEquals(expected, builder.toString());
  }


}
