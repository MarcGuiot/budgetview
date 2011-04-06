package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.model.Transaction;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class TransactionNoteEditor extends DefaultCellEditor {
  private GlobStringifier noteStringifier;
  private GlobRepository globRepository;
  private Glob currentGlob;
  private JTextField textField = new JTextField();
  private BlockTableKeysListener tableKeyBlocker = new BlockTableKeysListener();
  private JTable table;

  public TransactionNoteEditor(GlobRepository globRepository, Directory directory) {
    super(new JTextField());
    setClickCountToStart(1);

    this.globRepository = globRepository;
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    noteStringifier = descriptionService.getStringifier(Transaction.NOTE);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    this.table = table;
    this.table.setSurrendersFocusOnKeystroke(true);
    removeListeners();

    currentGlob = (Glob)value;
    String valueToDisplay = noteStringifier.toString(currentGlob, globRepository);
    textField = (JTextField)super.getTableCellEditorComponent(table, valueToDisplay, isSelected, row, column);
    textField.setFont(getSmallerFont(table.getFont()));

    GuiUtils.selectAndRequestFocus(textField);
    this.table.addKeyListener(tableKeyBlocker);

    return textField;
  }

  private Font getSmallerFont(Font font) {
    return font.deriveFont(font.getStyle(), font.getSize() - 2);
  }

  public boolean stopCellEditing() {
    globRepository.update(currentGlob.getKey(), Transaction.NOTE, textField.getText());
    finishEditing();
    return super.stopCellEditing();
  }

  public void cancelCellEditing() {
    finishEditing();
    super.cancelCellEditing();
  }

  private void finishEditing() {
    table.setSurrendersFocusOnKeystroke(false);
    removeListeners();
  }

  private void removeListeners() {
    table.removeKeyListener(tableKeyBlocker);
  }

  private void unselectAllPopupElements(JPopupMenu popup) {
    MenuSelectionManager.defaultManager().setSelectedPath(new MenuElement[]{popup});
    popup.setSelected(popup);
  }

  private void selectPopupElement(JPopupMenu popup, int index) {
    Component menuItem = popup.getComponent(index);
    MenuSelectionManager.defaultManager().setSelectedPath(new MenuElement[]{popup, (MenuElement)menuItem});
    popup.setSelected(menuItem);
  }

  private static class BlockTableKeysListener implements KeyListener {
    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }
  }
}

