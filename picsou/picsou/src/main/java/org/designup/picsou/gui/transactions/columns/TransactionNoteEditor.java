package org.designup.picsou.gui.transactions.columns;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Transaction;
import org.globsframework.gui.utils.AbstractDocumentListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class TransactionNoteEditor extends DefaultCellEditor {
  private GlobStringifier noteStringifier;
  private GlobRepository globRepository;
  private Glob currentGlob;
  private GlobList transactions;
  private JTextField textField = new JTextField();
  private TextChangeDocumentListener popupDisplayer = new TextChangeDocumentListener();
  private PopupNavigationKeyListener popupNavigator = new PopupNavigationKeyListener();
  private BlockTableKeysListener tableKeyBlocker = new BlockTableKeysListener();
  private JPopupMenu popup = new JPopupMenu();
  private JTable theTable;

  public TransactionNoteEditor(GlobRepository globRepository, Directory directory) {
    super(new JTextField());
    setClickCountToStart(1);

    this.globRepository = globRepository;
    DescriptionService descriptionService = directory.get(DescriptionService.class);
    noteStringifier = descriptionService.getStringifier(Transaction.NOTE);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    theTable = table;
    theTable.setSurrendersFocusOnKeystroke(true);
    hidePopup();
    removeListeners();

    transactions = globRepository.getAll(Transaction.TYPE);
    currentGlob = (Glob)value;
    String valueToDisplay = noteStringifier.toString(currentGlob, globRepository);
    textField = (JTextField)super.getTableCellEditorComponent(table, valueToDisplay, isSelected, row, column);
    textField.setFont(getSmallerFont(table.getFont()));

    textField.requestFocus();
    textField.selectAll();
    theTable.addKeyListener(tableKeyBlocker);
    textField.addKeyListener(popupNavigator);
    textField.getDocument().addDocumentListener(popupDisplayer);

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
    theTable.setSurrendersFocusOnKeystroke(false);
    hidePopup();
    removeListeners();
  }

  private void removeListeners() {
    textField.getDocument().removeDocumentListener(popupDisplayer);
    textField.removeKeyListener(popupNavigator);
    theTable.removeKeyListener(tableKeyBlocker);
  }

  private void hidePopup() {
    popup.setVisible(false);
    popup.removeAll();
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

  private class TextChangeDocumentListener extends AbstractDocumentListener {
    protected void documentChanged(DocumentEvent e) {
      showPopupIfNeeded();
    }

    private void showPopupIfNeeded() {
      if (textField.getText().length() == 0) {
        hidePopup();
        return;
      }
      Set<String> matchingNotes = new HashSet<String>();
      for (Glob transaction : transactions) {
        String note = noteStringifier.toString(transaction, globRepository);
        if (note.toLowerCase().indexOf(textField.getText().toLowerCase()) != -1) {
          matchingNotes.add(note);
        }
      }

      popup.setVisible(false);
      popup.removeAll();
      if (!matchingNotes.isEmpty()) {
        for (String matchingNote : matchingNotes) {
          addItem(popup, matchingNote);
        }
        popup.setInvoker(textField);
        popup.show(textField, 0, textField.getHeight());
        unselectAllPopupElements(popup);
        textField.requestFocus();
      }
    }

    private void addItem(JPopupMenu popup, final String name) {
      AbstractAction action =
        new AbstractAction(name) {
          public void actionPerformed(ActionEvent e) {
            removeListeners();
            textField.setText(name);
            textField.postActionEvent();
            theTable.requestFocus();
          }
        };
      JMenuItem menuItem = new JMenuItem();
      menuItem.setAction(action);
      menuItem.setFont(Gui.DEFAULT_TABLE_FONT);
      popup.add(menuItem);
    }
  }

  private class PopupNavigationKeyListener extends KeyAdapter {

    public void keyPressed(KeyEvent e) {
      if (!popup.isVisible()) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          cancelCellEditing();
          e.consume();
        }
        return;
      }
      int size = popup.getComponentCount();
      int currentIndex = popup.getSelectionModel().getSelectedIndex();
      switch (e.getKeyCode()) {
        case KeyEvent.VK_ESCAPE:
          hidePopup();
          textField.requestFocus();
          e.consume();
          break;
        case KeyEvent.VK_DOWN:
          selectPopupElement(popup, Math.min(currentIndex + 1, size - 1));
          e.consume();
          break;
        case KeyEvent.VK_UP:
          if (currentIndex == -1) {
            break;
          }
          if (currentIndex == 0) {
            unselectAllPopupElements(popup);
          }
          else {
            selectPopupElement(popup, Math.max(currentIndex - 1, 0));
          }
          e.consume();
          break;
        case KeyEvent.VK_ENTER:
          if (currentIndex != -1) {
            JMenuItem menuToValid = (JMenuItem)popup.getComponent(currentIndex);
            menuToValid.doClick();
          }
          break;
        default: // ignore the event
      }
    }
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

