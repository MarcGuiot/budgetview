package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.ApplicationColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
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

public abstract class HyperlinkTableColumn extends ButtonTableColumn implements ColorChangeListener {

  private ColorService colorService;

  private Color normalLinkColor;
  private Color selectedLinkColor;

  protected HyperlinkTableColumn(GlobTableView view, DescriptionService descriptionService, GlobRepository repository, Directory directory) {
    super(view, descriptionService, repository, directory);
    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    normalLinkColor = colorLocator.get(ApplicationColors.TABLE_LINK_NORMAL);
    selectedLinkColor = colorLocator.get(ApplicationColors.TABLE_LINK_SELECTED);
  }

  protected void updateComponent(JButton button, JPanel panel, Glob glob, boolean edit) {
    button.setForeground(isSelected ? selectedLinkColor : normalLinkColor);
  }

  protected JButton createButton(Action action) {
    return createHyperlinkButton(action);
  }

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
