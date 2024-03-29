package com.budgetview.desktop.components.table;

import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public abstract class AbstractRolloverEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
  protected GlobTableView tableView;
  protected DescriptionService descriptionService;
  protected GlobRepository repository;
  protected Directory directory;
  protected SelectionService selectionService;
  protected ImageLocator imageLocator;

  protected boolean hasFocus;
  protected boolean isSelected;
  protected int row;
  protected int column;

  private SelectionUpdaterListener selectionUpdater;

  protected AbstractRolloverEditor(GlobTableView view, DescriptionService descriptionService,
                                   GlobRepository repository, Directory directory) {
    this.tableView = view;
    this.descriptionService = descriptionService;
    this.repository = repository;
    this.directory = directory;

    this.selectionService = directory.get(SelectionService.class);
    this.imageLocator = directory.get(ImageLocator.class);
  }

  protected abstract Component getComponent(Glob glob, boolean edit);

  public Object getCellEditorValue() {
    return null;
  }

  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                 boolean hasFocus, int row, int column) {
    if (value == null){
      return null;
    }
    this.hasFocus = hasFocus;
    return getComponentToRender(isSelected, value, row, column, false);
  }

  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    hasFocus = true;
    selectionUpdater = new SelectionUpdaterListener(table, row, column);
    addSelectionUpdater();
    return getComponentToRender(isSelected, value, row, column, true);
  }

  public boolean isCellEditable(EventObject anEvent) {
    if (anEvent instanceof MouseEvent) {
      return ((MouseEvent)anEvent).getClickCount() >= 2;
    }
    return true;
  }

  public void cancelCellEditing() {
    removeSelectionUpdater();
    super.cancelCellEditing();
  }

  public boolean stopCellEditing() {
    removeSelectionUpdater();
    return super.stopCellEditing();
  }

  protected void addSelectionUpdater() {
    tableView.getComponent().getSelectionModel().addListSelectionListener(selectionUpdater);
  }

  protected void removeSelectionUpdater() {
    tableView.getComponent().getSelectionModel().removeListSelectionListener(selectionUpdater);
  }

  private Component getComponentToRender(boolean isSelected, Object value, int row, int column, boolean edit) {
    this.isSelected = isSelected;
    this.row = row;
    this.column = column;
    return getComponent((Glob)value, edit);
  }

  protected <T extends JPanel> T initCellPanel(JComponent component, boolean leftAlign, T panel) {
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    if (leftAlign) {
      panel.add(component);
      panel.add(Box.createHorizontalGlue());
    }
    else {
      panel.add(Box.createHorizontalGlue());
      panel.add(component);
    }
    return panel;
  }

  protected HyperlinkButton createHyperlinkButton(Action action) {
    HyperlinkButton button = new HyperlinkButton(action);
    button.setOpaque(false);
    button.setAutoHide(false);
    button.setUnderline(false);
    return button;
  }

  private class SelectionUpdaterListener implements ListSelectionListener {
    private final JTable table;
    private final int row;
    private final int column;

    public SelectionUpdaterListener(JTable table, int row, int column) {
      this.table = table;
      this.row = row;
      this.column = column;
    }

    public void valueChanged(ListSelectionEvent event) {
      if (table.getSelectionModel().getValueIsAdjusting()) {
        return;
      }
      int[] selection = table.getSelectedRows();
      for (int selectedRow : selection) {
        if (selectedRow == row) {
          refreshDisplay(row, column);
        }
      }
    }

    protected void refreshDisplay(int row, int column) {
      tableView.getComponent().editCellAt(row, column);
    }
  }
}
