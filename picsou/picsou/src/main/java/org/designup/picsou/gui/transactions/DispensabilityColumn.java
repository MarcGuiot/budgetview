package org.designup.picsou.gui.transactions;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Transaction;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DispensabilityColumn
  extends AbstractCellEditor
  implements TableCellRenderer, TableCellEditor {

  public static Icon CHECKBOX_SELECTED_ICON = Gui.ICON_LOCATOR.get("menucheckbox.png");
  public static Icon CHECKBOX_NOTSELECTED_ICON = Gui.ICON_LOCATOR.get("menucheckboxblank.png");

  private JLabel renderCheckBox;
  private JLabel editCheckBox;
  private TransactionRendererColors rendererColors;
  private GlobRepository repository;
  private Glob currentTransaction;

  public DispensabilityColumn(TransactionRendererColors rendererColors, GlobRepository repository) {
    this.rendererColors = rendererColors;
    this.repository = repository;
    initComponents();
  }

  public Object getCellEditorValue() {
    return null;
  }

  private void initComponents() {
    renderCheckBox = createCheckbox();

    editCheckBox = createCheckbox();
    editCheckBox.addMouseListener(new DispensabilityAction());
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    renderCheckbox(renderCheckBox, value, isSelected, row);
    return renderCheckBox;
  }

  public Component getTableCellEditorComponent(JTable table, Object value,
                                               boolean isSelected,
                                               int row, int column) {
    renderCheckbox(editCheckBox, value, isSelected, row);
    currentTransaction = (Glob) value;
    return editCheckBox;
  }

  private JLabel createCheckbox() {
    JLabel label = new JLabel(CHECKBOX_NOTSELECTED_ICON);
    label.setOpaque(true);
    label.setToolTipText(Lang.get("dispensability.description"));
    return label;
  }

  private void renderCheckbox(JLabel checkbox, Object value, boolean isSelected, int row) {
    Glob transaction = (Glob) value;
    if (Boolean.TRUE.equals(transaction.get(Transaction.DISPENSABLE))) {
      checkbox.setIcon(CHECKBOX_SELECTED_ICON);
    }
    else {
      checkbox.setIcon(CHECKBOX_NOTSELECTED_ICON);
    }
    rendererColors.setTransactionBackground(editCheckBox, isSelected, row);
  }

  private class DispensabilityAction extends MouseAdapter {
    public void mousePressed(MouseEvent e) {
      repository.update(currentTransaction.getKey(),
                        Transaction.DISPENSABLE,
                        CHECKBOX_NOTSELECTED_ICON.equals(editCheckBox.getIcon()));
      stopCellEditing();
    }
  }
}
