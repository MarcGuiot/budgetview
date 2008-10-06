package org.globsframework.gui.views.utils;

import javax.swing.*;

public class GlobViewUtils {
  public static void updateSelectionAfterItemMoved(ListSelectionModel selectionModel, int[] previousSelection,
                                                   int previousIndex, int newIndex) {

    selectionModel.setValueIsAdjusting(true);
    selectionModel.clearSelection();
    for (int index : previousSelection) {
      int insertion;
      if (index == previousIndex) {
        insertion = newIndex;
      }
      else {
        insertion = index;
        if (index > previousIndex) {
          insertion--;
        }
        if (index > newIndex) {
          insertion++;
        }
      }
      selectionModel.addSelectionInterval(insertion, insertion);
    }
    selectionModel.setValueIsAdjusting(false);
  }
}
