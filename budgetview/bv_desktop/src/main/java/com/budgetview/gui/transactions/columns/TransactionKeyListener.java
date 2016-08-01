package com.budgetview.gui.transactions.columns;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TransactionKeyListener extends KeyAdapter {
  private final JTable table;
  private int noteColumnIndex;
  private Action deleteAction;

  public static TransactionKeyListener install(JTable table, int noteColumnIndex) {
    TransactionKeyListener listener = new TransactionKeyListener(table, noteColumnIndex);
    table.addKeyListener(listener);
    return listener;
  }

  private TransactionKeyListener(JTable table, int noteColumnIndex) {
    this.table = table;
    this.noteColumnIndex = noteColumnIndex;
  }

  public void setDeleteEnabled(Action action) {
    this.deleteAction = action;
  }

  public void keyPressed(KeyEvent event) {
    if ((deleteAction != null) && (event.getKeyCode() == KeyEvent.VK_DELETE)) {
      deleteAction.actionPerformed(null);
      return;
    }
    int[] selectedRows = table.getSelectedRows();
    if (noteColumnIndex != -1 && (selectedRows.length == 1) &&
        (Character.isLetterOrDigit(event.getKeyCode())) &&
        (InputEvent.getModifiersExText(event.getModifiersEx()).length() == 0)) {
      table.editCellAt(selectedRows[0], noteColumnIndex);
    }
    super.keyPressed(event);
  }
}
