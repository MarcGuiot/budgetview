package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.transactions.DeleteTransactionDialog;
import org.globsframework.utils.directory.Directory;
import org.globsframework.model.GlobList;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TransactionKeyListener extends KeyAdapter {
  private final JTable table;
  private int noteColumnIndex;
  private Directory directory;
  private boolean deleteEnable;
  private GlobRepository repository;

  public static void install(JTable table, int noteColumnIndex,
                             Directory directory, final GlobRepository repository, boolean deleteEnable) {
    table.addKeyListener(new TransactionKeyListener(table, noteColumnIndex, deleteEnable, repository, directory));
  }

  private TransactionKeyListener(JTable table, int noteColumnIndex, boolean deleteEnable,
                                 GlobRepository repository, Directory directory) {
    this.table = table;
    this.noteColumnIndex = noteColumnIndex;
    this.repository = repository;
    this.directory = directory;
    this.deleteEnable = deleteEnable;
  }

  public void keyPressed(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.VK_DELETE && deleteEnable){
      JFrame parent = directory.get(JFrame.class);
      int[] selectedRows = table.getSelectedRows();
      if (selectedRows == null || selectedRows.length == 0){
        return;
      }
      GlobList list = new GlobList();
      for (int row : selectedRows) {
        list.add((Glob)table.getModel().getValueAt(row, 0));
      }
      DeleteTransactionDialog dialog =
        new DeleteTransactionDialog(list, parent, repository, directory);
      dialog.show();
      return;
    }
    int[] selectedRows = table.getSelectedRows();
    if ((selectedRows.length == 1) &&
        (Character.isLetterOrDigit(event.getKeyCode())) &&
        (InputEvent.getModifiersExText(event.getModifiersEx()).length() == 0)) {
      table.editCellAt(selectedRows[0], noteColumnIndex);
    }
    super.keyPressed(event);
  }
}
