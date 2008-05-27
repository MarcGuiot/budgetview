package org.designup.picsou.gui.categories;

import org.crossbowlabs.globs.gui.views.LabelCustomizer;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorChangeListener;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ColorSource;
import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Category;

import javax.swing.*;
import java.awt.*;

public class CategoryLabelCustomizer implements LabelCustomizer, ColorChangeListener {
  private Color color;

  public CategoryLabelCustomizer(Directory directory) {
    directory.get(ColorService.class).addListener(this);
  }

  public void colorsChanged(ColorSource source) {
    color = source.get(PicsouColors.CATEGORY_TITLE);
  }

  public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    boolean isMaster = Category.isMaster(glob);
    label.setFont(isMaster ? Gui.DEFAULT_TABLE_FONT_BOLD : Gui.DEFAULT_TABLE_FONT);
    label.setForeground(isSelected ? Color.WHITE : color);
  }
}
