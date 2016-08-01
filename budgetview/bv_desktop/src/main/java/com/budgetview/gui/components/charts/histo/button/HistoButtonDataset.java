package com.budgetview.gui.components.charts.histo.button;

import com.budgetview.shared.gui.histochart.utils.AbstractHistoDataset;
import com.budgetview.shared.gui.histochart.HistoDatasetElement;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidState;

import java.util.*;

public class HistoButtonDataset extends AbstractHistoDataset<HistoDatasetElement> {

  private SortedSet<HistoButtonElement> buttonElements = new TreeSet<HistoButtonElement>();
  private Map<Key,HistoButtonElement> keyMap = new HashMap<Key, HistoButtonElement>();
  private HistoButtonBlock[] blocks;
  private int rowCount;

  public HistoButtonDataset(String tooltipKey) {
    super(tooltipKey);
    resetBlocks();
    maxNegative = 0;
    maxPositive = 1;
  }

  public String getTooltip(int index, Set<Key> objectKey) {
    if (objectKey.isEmpty()) {
      return null;
    }
    return keyMap.get(objectKey.iterator().next()).tooltip;
  }

  public void addColumn(int id, String label, String tooltip, String section, boolean current, boolean future, boolean selected) {
    super.add(new HistoDatasetElement(id, label, tooltip, section, current, future, selected));
    resetBlocks();
  }

  public void addButton(int minId, int maxId, String label, Key key, String tooltip, boolean selected, boolean enabled) {
    HistoButtonElement element = new HistoButtonElement(minId, maxId, label, key, tooltip, selected, enabled);
    buttonElements.add(element);
    keyMap.put(key, element);
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
    List<HistoButtonBlock> blockList = new ArrayList<HistoButtonBlock>();
    for (HistoButtonElement element : buttonElements) {
      HistoButtonBlock block = createBlock(element, occupied);
      if (block != null) {
        blockList.add(block);
      }
    }
    blocks = blockList.toArray(new HistoButtonBlock[blockList.size()]);
  }

  private HistoButtonBlock createBlock(HistoButtonElement element, boolean[][] occupied) {
    HistoDatasetElement firstColumnElement = getElement(0);
    HistoDatasetElement lastColumnElement = getElement(size() - 1);
    if ((element.maxId < firstColumnElement.id) || (element.minId > lastColumnElement.id)) {
      return null;
    }

    int minElementIndex = getIndex(element.minId);
    int minIndex = minElementIndex < 0 ? 0 : minElementIndex;
    boolean truncatedMin = minElementIndex < 0;

    int maxElementIndex = getIndex(element.maxId);
    int maxIndex = element.maxId > lastColumnElement.id ? getIndex(lastColumnElement.id) : maxElementIndex;
    boolean truncatedMax = element.maxId > lastColumnElement.id;

    for (int row = 0; row < occupied.length; row++) {
      boolean freeBlockAvailable = true;
      for (int i = minIndex; i <= maxIndex; i++) {
        if (occupied[i][row]) {
          freeBlockAvailable = false;
          break;
        }
      }
      if (freeBlockAvailable) {
        for (int i = minIndex; i <= maxIndex; i++) {
          occupied[i][row] = true;
        }
        if (row >= rowCount) {
          rowCount = row + 1;
        }
        return new HistoButtonBlock(minIndex, maxIndex, row, truncatedMin, truncatedMax, element.label, element.key, element.selected, element.enabled);
      }
    }
    throw new InvalidState("Cannot find empty space for element: " + element.label);
  }

  public Set<HistoButtonElement> getElements() {
    return Collections.unmodifiableSet(buttonElements);
  }
}
