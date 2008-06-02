package org.designup.picsou.gui.categories;

import org.crossbowlabs.globs.gui.SelectionService;
import org.crossbowlabs.globs.gui.views.CellPainter;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.splits.utils.TransparentIcon;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.model.Category;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CategoryExpansionColumn
  extends AbstractCellEditor
  implements TableCellRenderer, TableCellEditor, ActionListener {

  public static Icon EXPANDED_ICON = Gui.ICON_LOCATOR.get("arrow_down.png");
  public static Icon COLLAPSED_ICON = Gui.ICON_LOCATOR.get("arrow_right.png");
  public static Icon DISABLED_ICON = new TransparentIcon(EXPANDED_ICON.getIconHeight(), EXPANDED_ICON.getIconWidth());

  private CategoryExpansionModel expansionModel;

  private JButton renderButton;
  private JButton editButton;
  private CategoryView view;
  private CategoryBackgroundPainter backgroundPainter;
  private SelectionService selectionService;

  public CategoryExpansionColumn(CategoryBackgroundPainter painter, SelectionService selectionService) {
    this.backgroundPainter = painter;
    this.selectionService = selectionService;
    initButtons();
  }

  public void init(CategoryView view, CategoryExpansionModel model) {
    this.view = view;
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
    Glob category = (Glob) value;
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
    setIcon(renderButton, category);
    renderButton.setUI(new PainterUI(backgroundPainter, (Glob) value, row, column, isSelected, hasFocus));
    return renderButton;
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    setIcon(editButton, (Glob) value);
    editButton.setUI(new PainterUI(backgroundPainter, (Glob) value, row, column, isSelected, true));
    return editButton;
  }

  public Object getCellEditorValue() {
    return null;
  }

  public void actionPerformed(ActionEvent e) {
    Glob category = view.getSelectedCategory();
    expansionModel.toggleExpansion(category);
    stopCellEditing();
  }

  public int getPreferredWidth() {
    return EXPANDED_ICON.getIconWidth() + 6;
  }

  private void setIcon(JButton button, Glob category) {
    if (Category.isMaster(category)) {
      if (Category.isAll(category) || Category.isNone(category)) {
        button.setIcon(null);
      }
      else if (expansionModel.isExpanded(category)) {
        button.setIcon(EXPANDED_ICON);
      }
      else if (expansionModel.isExpandable(category)) {
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
    private Glob category;
    private int row;
    private int column;
    private boolean selected;
    private boolean hasFocus;

    public PainterUI(CellPainter cellPainter, Glob category, int row, int column, boolean selected, boolean hasFocus) {
      this.cellPainter = cellPainter;
      this.category = category;
      this.row = row;
      this.column = column;
      this.selected = selected;
      this.hasFocus = hasFocus;
    }

    public void paint(Graphics g, JComponent c) {
      cellPainter.paint(g, category, row, column, selected, hasFocus, c.getWidth(), c.getHeight());
      super.paint(g, c);
    }
  }
}
