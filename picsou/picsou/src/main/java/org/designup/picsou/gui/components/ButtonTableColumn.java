package org.designup.picsou.gui.components;

import org.globsframework.gui.views.CellPainter;
import org.globsframework.gui.views.GlobTableColumn;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.gui.views.utils.CellPainterPanel;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

public abstract class ButtonTableColumn extends AbstractRolloverEditor implements GlobTableColumn {

  private JButton rendererButton;
  private CellPainterPanel rendererPanel;
  private JButton editorButton;
  private CellPainterPanel editorPanel;


  public ButtonTableColumn(GlobTableView view, DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, descriptionService, repository, directory);

    ClickAction action = new ClickAction();
    CellPainter backgroundPainter = view.getDefaultBackgroundPainter();

    rendererButton = createButton(action);
    rendererPanel = initCellPanel(rendererButton, true, new CellPainterPanel());
    rendererPanel.setPainter(backgroundPainter);

    editorButton = createButton(action);
    editorButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fireEditingStopped();
      }
    });
    editorPanel = initCellPanel(editorButton, true, new CellPainterPanel());
    editorPanel.setPainter(backgroundPainter);
  }

  protected JButton createButton(Action action) {
    return new JButton(action);
  }

  public boolean isCellEditable(EventObject anEvent) {
    if (anEvent instanceof MouseEvent) {
      return ((MouseEvent)anEvent).getClickCount() >= 1;
    }
    return true;
  }

  protected Component getComponent(Glob glob, boolean edit) {
    JButton button;
    CellPainterPanel panel;
    if (edit) {
      button = this.editorButton;
      panel = this.editorPanel;
    }
    else {
      button = this.rendererButton;
      panel = this.rendererPanel;
    }

    panel.update(glob, row, column, isSelected, hasFocus);

    updateComponent(button, panel, glob, edit);

    return panel;
  }

  protected abstract void updateComponent(JButton button, JPanel panel, Glob glob, boolean edit);

  protected abstract void processClick();

  private class ClickAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      processClick();
    }
  }

  public final TableCellRenderer getRenderer() {
    return this;
  }

  public final TableCellEditor getEditor() {
    return this;
  }

  public boolean isEditable(int row, Glob glob) {
    return true;
  }

  public boolean isReSizable() {
    return true;
  }
}
