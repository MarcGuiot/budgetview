package org.designup.picsou.gui.components.charts.histo.button;

import org.designup.picsou.gui.components.charts.histo.utils.AbstractHistoDataset;
import org.designup.picsou.gui.components.charts.histo.utils.HistoDatasetElement;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.SortedSet;
import java.util.TreeSet;

public class HistoButtonDataset extends AbstractHistoDataset<HistoDatasetElement> {

  private SortedSet<HistoButtonElement> buttonElements = new TreeSet<HistoButtonElement>();
  private HistoButtonBlock[] blocks;
  private int rowCount;

  public HistoButtonDataset(String tooltipKey) {
    super(tooltipKey);
    resetBlocks();
    maxNegative = 0;
    maxPositive = 1;
  }

  public String getTooltip(int index, Key objectKey) {
    return "";
  }

  public void addColumn(int id, String label, String tooltip, String section, boolean current, boolean future, boolean selected) {
    super.add(new HistoDatasetElement(id, label, tooltip, section, current, future, selected));
    resetBlocks();
  }

  public void addButton(int minId, int maxId, String label, Key key) {
    buttonElements.add(new HistoButtonElement(minId, maxId, label, key));
    resetBlocks();
  }

  public int getRowCount() {
    if (blocks == null) {
      computeBlocks();
    }
    return rowCount;
  }

  private void resetBlocks() {
    blocks = null;
    rowCount = 0;
  }

  public HistoButtonBlock[] getBlocks() {
    if (blocks == null) {
      computeBlocks();
    }
    return blocks;
  }

  private void computeBlocks() {
    boolean[][] occupied = new boolean[size()][buttonElements.size()];
    blocks = new HistoButtonBlock[buttonElements.size()];
    int i = 0;
    for (HistoButtonElement element : buttonElements) {
      blocks[i++] = createBlock(element, occupied);
    }
  }

  private HistoButtonBlock createBlock(HistoButtonElement element, boolean[][] occupied) {
    int minElementIndex = getIndex(element.minId);
    int minIndex = minElementIndex < 0 ? 0 : minElementIndex;
    boolean truncatedMin = minElementIndex < 0;

    int maxElementIndex = getIndex(element.maxId);
    HistoDatasetElement lastColumnElement = getElement(size() - 1);
    int maxIndex = element.maxId > lastColumnElement.id ? getIndex(lastColumnElement.id) : maxElementIndex;
    boolean truncatedMax = element.maxId > lastColumnElement.id;

    for (int row = 0; row < occupied.length; row++) {
      boolean free = true;
      for (int i = minIndex; i <= maxIndex; i++) {
        if (occupied[i][row]) {
          free = false;
          break;
        }
      }
      if (free) {
        for (int i = minIndex; i <= maxIndex; i++) {
          occupied[i][row] = true;
        }
        if (row >= rowCount) {
          rowCount = row + 1;
        }
        return new HistoButtonBlock(minIndex, maxIndex, row, truncatedMin, truncatedMax, element.label, element.key);
      }
    }
    throw new InvalidState("Cannot find empty space for element: " + element.label);
  }
}
