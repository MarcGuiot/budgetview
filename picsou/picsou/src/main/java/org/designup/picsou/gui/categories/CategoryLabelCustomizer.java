package org.designup.picsou.gui.categories;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.utils.PicsouColors;
import org.designup.picsou.model.Category;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorSource;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

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
