package org.designup.picsou.gui.components.table;

import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.Colors;
import org.globsframework.gui.splits.components.HyperlinkButton;
import org.globsframework.gui.views.GlobTableView;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

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
    normalLinkColor = colorLocator.get("transactionTable.hyperlink.normal");
    selectedLinkColor = colorLocator.get("transactionTable.hyperlink.selected");
  }

  protected void updateComponent(JButton button, JPanel panel, Glob glob, boolean edit) {
    updateColor(button);
  }

  protected void updateColor(JButton button) {
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
