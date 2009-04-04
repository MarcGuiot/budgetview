package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.components.HyperlinkButton;
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

public abstract class HyperlinkTableColumn extends AbstractRolloverEditor implements GlobTableColumn, ColorChangeListener {

  private HyperlinkButton rendererButton;
  private CellPainterPanel rendererPanel;
  private HyperlinkButton editorButton;
  private CellPainterPanel editorPanel;

  private Color normalLinkColor;
  private Color selectedLinkColor;

  public HyperlinkTableColumn(GlobTableView view, DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, descriptionService, repository, directory);
    directory.get(ColorService.class).addListener(this);

    ClickAction action = new ClickAction();
    CellPainter backgroundPainter = view.getDefaultBackgroundPainter();

    rendererButton = createHyperlinkButton(action);
    rendererPanel = initCellPanel(rendererButton, true, new CellPainterPanel());
    rendererPanel.setPainter(backgroundPainter);

    editorButton = createHyperlinkButton(action);
    editorPanel = initCellPanel(editorButton, true, new CellPainterPanel());
    editorPanel.setPainter(backgroundPainter);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    normalLinkColor = colorLocator.get(PicsouColors.TABLE_LINK_NORMAL);
    selectedLinkColor = colorLocator.get(PicsouColors.TABLE_LINK_SELECTED);
  }

  protected Component getComponent(Glob glob, boolean render) {
    HyperlinkButton button;
    CellPainterPanel panel;
    if (render) {
      button = this.rendererButton;
      panel = this.rendererPanel;
    }
    else {
      button = this.editorButton;
      panel = this.editorPanel;
    }

    button.setForeground(isSelected ? selectedLinkColor : normalLinkColor);
    panel.update(glob, row, column, isSelected, hasFocus);

    updateComponent(button, panel, glob, render);

    return panel;
  }

  protected abstract void updateComponent(HyperlinkButton button, JPanel panel, Glob glob, boolean render);

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
}
