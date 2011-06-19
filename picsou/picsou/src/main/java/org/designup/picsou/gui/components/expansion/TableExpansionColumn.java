package org.designup.picsou.gui.components.expansion;

import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.utils.TransparentIcon;
import org.globsframework.gui.views.CellPainter;
import org.globsframework.model.Glob;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TableExpansionColumn
  extends AbstractCellEditor
  implements TableCellRenderer, TableCellEditor, ActionListener {

  public static Icon EXPANDED_ICON = Gui.IMAGE_LOCATOR.get("arrow_down.png");
  public static Icon COLLAPSED_ICON = Gui.IMAGE_LOCATOR.get("arrow_right.png");
  public static Icon DISABLED_ICON = new TransparentIcon(EXPANDED_ICON.getIconHeight(), EXPANDED_ICON.getIconWidth());

  private TableExpansionModel expansionModel;
  private ExpandableTable tableView;

  private JButton renderButton;
  private JButton editButton;
  private CellPainter backgroundPainter;

  public TableExpansionColumn(CellPainter painter) {
    this.backgroundPainter = painter;
    initButtons();
  }

  public void init(ExpandableTable tableView, TableExpansionModel model) {
    this.tableView = tableView;
    this.expansionModel = model;
  }

  private void initButtons() {
    renderButton = new JButton();
    configureButton(renderButton);

    editButton = new JButton();
    configureButton(editButton);
    editButton.addActionListener(this);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
                                                 boolean isSelected, boolean hasFocus,
                                                 int row, int column) {
    if (value == null) {
      return null;
    }
    Glob glob = (Glob)value;
    if (hasFocus) {
      renderButton.setForeground(table.getForeground());
      renderButton.setBackground(UIManager.getColor("Button.background"));
    }
    else if (isSelected) {
      renderButton.setForeground(table.getSelectionForeground());
      renderButton.setBackground(table.getSelectionBackground());
    }
    else {
      renderButton.setForeground(table.getForeground());
      renderButton.setBackground(UIManager.getColor("Button.background"));
    }
    setIcon(renderButton, glob);
    setUI(renderButton, value, isSelected, hasFocus, row, column);
    return renderButton;
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    setIcon(editButton, (Glob)value);
    setUI(editButton, value, true, true, row, column);
    return editButton;
  }

  private void setUI(JButton button, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (backgroundPainter != null) {
      button.setUI(new PainterUI(backgroundPainter, (Glob)value, row, column, isSelected, hasFocus));
    }
  }

  public Object getCellEditorValue() {
    return null;
  }

  public void actionPerformed(ActionEvent e) {
    if (Gui.hasModifiers(e)) {
      return;
    }
    Glob glob = tableView.getSelectedGlob();
    expansionModel.toggleExpansion(glob);
    stopCellEditing();
  }

  public int getPreferredWidth() {
    return EXPANDED_ICON.getIconWidth() + 6;
  }

  private void setIcon(JButton button, Glob glob) {
    if (expansionModel.isMaster(glob)) {
      if (expansionModel.isExpansionDisabled(glob)) {
        button.setIcon(null);
      }
      else if (expansionModel.isExpanded(glob)) {
        button.setIcon(EXPANDED_ICON);
      }
      else if (expansionModel.isExpandable(glob)) {
        button.setIcon(COLLAPSED_ICON);
      }
      else {
        button.setIcon(DISABLED_ICON);
      }
    }
    else {
      button.setIcon(null);
      button.setRolloverIcon(null);
    }
  }

  private void configureButton(JButton button) {
    Gui.configureIconButton(button, "expand", new Dimension(EXPANDED_ICON.getIconWidth(),
                                                            EXPANDED_ICON.getIconHeight()));
  }

  private class PainterUI extends BasicButtonUI {
    private CellPainter cellPainter;
    private Glob glob;
    private int row;
    private int column;
    private boolean selected;
    private boolean hasFocus;

    public PainterUI(CellPainter cellPainter, Glob glob, int row, int column, boolean selected, boolean hasFocus) {
      this.cellPainter = cellPainter;
      this.glob = glob;
      this.row = row;
      this.column = column;
      this.selected = selected;
      this.hasFocus = hasFocus;
    }

    public void paint(Graphics g, JComponent c) {
      cellPainter.paint(g, glob, row, column, selected, hasFocus, c.getWidth(), c.getHeight());
      super.paint(g, c);
    }
  }
}
