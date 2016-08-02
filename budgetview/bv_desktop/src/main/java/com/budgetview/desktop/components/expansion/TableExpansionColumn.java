package com.budgetview.desktop.components.expansion;

import com.budgetview.desktop.utils.Gui;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.icons.ArrowIcon;
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

  private static Color DEFAULT_SELECTED_FOREGROUND = Colors.toColor("666666");
  public static Icon EXPANDED_ICON = new ArrowIcon(8,7, ArrowIcon.Orientation.DOWN, DEFAULT_SELECTED_FOREGROUND);
  public static Icon COLLAPSED_ICON = new ArrowIcon(8,7, ArrowIcon.Orientation.RIGHT, DEFAULT_SELECTED_FOREGROUND);
  public static Icon EXPANDED_ICON_SELECTED = new ArrowIcon(8,7, ArrowIcon.Orientation.DOWN, Color.WHITE);
  public static Icon COLLAPSED_ICON_SELECTED = new ArrowIcon(8,7, ArrowIcon.Orientation.RIGHT, Color.WHITE);
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
    setIcon(renderButton, glob, isSelected);
    setUI(renderButton, value, isSelected, hasFocus, row, column);
    return renderButton;
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    setIcon(editButton, (Glob)value, isSelected);
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
    expansionModel.toggleExpansion(glob, true);
    stopCellEditing();
  }

  public int getPreferredWidth() {
    return EXPANDED_ICON.getIconWidth() + 6;
  }

  private void setIcon(JButton button, Glob glob, boolean selected) {
    if (expansionModel.isExpandable(glob)) {
      if (expansionModel.isExpanded(glob)) {
        button.setIcon(selected ? EXPANDED_ICON_SELECTED : EXPANDED_ICON);
      }
      else {
        button.setIcon(selected ? COLLAPSED_ICON_SELECTED : COLLAPSED_ICON);
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
