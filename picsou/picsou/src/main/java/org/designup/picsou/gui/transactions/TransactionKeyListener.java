package org.designup.picsou.gui.transactions;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TransactionKeyListener extends KeyAdapter {
  private final JTable table;
  private CategoryChooserAction categoryChooserAction;
  private int noteColumnIndex;

  public TransactionKeyListener(JTable table, CategoryChooserAction categoryChooserAction, int noteColumnIndex) {
    this.table = table;
    this.categoryChooserAction = categoryChooserAction;
    this.noteColumnIndex = noteColumnIndex;
  }

  public void keyPressed(KeyEvent event) {
    int[] selectedRows = table.getSelectedRows();
    if (event.isAltDown() || event.isControlDown()) {
      if (event.getKeyCode() == KeyEvent.VK_SPACE) {
        if (selectedRows.length > 0) {
          categoryChooserAction.actionPerformed(null);
          event.consume();
        }
      }
    }
    else if ((selectedRows.length == 1) &&
             (Character.isLetterOrDigit(event.getKeyCode())) &&
             (InputEvent.getModifiersExText(event.getModifiersEx()).length() == 0)) {
      table.editCellAt(selectedRows[0], noteColumnIndex);
    }
    super.keyPressed(event);
  }
}
