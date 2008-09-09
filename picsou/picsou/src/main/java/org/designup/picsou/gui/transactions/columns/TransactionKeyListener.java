package org.designup.picsou.gui.transactions.columns;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TransactionKeyListener extends KeyAdapter {
  private final JTable table;
  private int noteColumnIndex;

  public static void install(JTable table, int noteColumnIndex) {
    table.addKeyListener(new TransactionKeyListener(table, noteColumnIndex));    
  }

  private TransactionKeyListener(JTable table, int noteColumnIndex) {
    this.table = table;
    this.noteColumnIndex = noteColumnIndex;
  }

  public void keyPressed(KeyEvent event) {
    int[] selectedRows = table.getSelectedRows();
    if ((selectedRows.length == 1) &&
             (Character.isLetterOrDigit(event.getKeyCode())) &&
             (InputEvent.getModifiersExText(event.getModifiersEx()).length() == 0)) {
      table.editCellAt(selectedRows[0], noteColumnIndex);
    }
    super.keyPressed(event);
  }
}
