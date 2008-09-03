package org.designup.picsou.gui.components;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.color.ColorUpdaters;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.gui.utils.PicsouColors;

import javax.swing.*;

public class PicsouTableHeaderCustomizer implements LabelCustomizer {
  private ColorService colorService;
  protected PicsouColors tableHeaderTitle;

  public PicsouTableHeaderCustomizer(Directory directory, PicsouColors titleColor) {
    colorService = directory.get(ColorService.class);
    tableHeaderTitle = titleColor;
  }

  public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    colorService.install(tableHeaderTitle.toString(), ColorUpdaters.foreground(label));
    label.setBorder(BorderFactory.createEmptyBorder());
  }
}
