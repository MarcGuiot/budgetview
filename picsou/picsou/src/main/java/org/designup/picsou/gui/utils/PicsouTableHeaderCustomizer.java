package org.designup.picsou.gui.utils;

import org.crossbowlabs.globs.gui.views.LabelCustomizer;
import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.splits.color.ColorService;
import org.crossbowlabs.splits.color.ForegroundColorUpdater;

import javax.swing.*;

public class PicsouTableHeaderCustomizer implements LabelCustomizer {
  private ColorService colorService;
  protected PicsouColors tableHeaderTitle;

  public PicsouTableHeaderCustomizer(Directory directory, PicsouColors titleColor) {
    colorService = directory.get(ColorService.class);
    tableHeaderTitle = titleColor;
  }

  public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    colorService.install(tableHeaderTitle.toString(), new ForegroundColorUpdater(label));
    label.setBorder(BorderFactory.createEmptyBorder());
  }
}
