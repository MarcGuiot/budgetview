package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public abstract class CustomBoldLabelCustomizer implements LabelCustomizer, ColorChangeListener {
  private Color color;
  private ColorService colorService;

  public CustomBoldLabelCustomizer(Directory directory) {
    colorService = directory.get(ColorService.class);
    colorService.addListener(this);
  }

  public void colorsChanged(ColorLocator locator) {
    color = locator.get(ApplicationColors.CATEGORY_TITLE);
  }

  public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    boolean isBold = isBold(glob);
    label.setFont(isBold ? Gui.DEFAULT_TABLE_FONT_BOLD : Gui.DEFAULT_TABLE_FONT);
    label.setForeground(isSelected ? Color.WHITE : color);
    if (!isBold) {
      label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    }
  }

  protected abstract boolean isBold(Glob glob);

  protected void finalize() throws Throwable {
    super.finalize();
    colorService.removeListener(this);
  }
}
