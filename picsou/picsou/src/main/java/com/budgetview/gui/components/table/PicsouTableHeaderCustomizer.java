package com.budgetview.gui.components.table;

import org.globsframework.gui.splits.color.*;
import org.globsframework.gui.splits.color.utils.ForegroundColorUpdater;
import org.globsframework.gui.views.LabelCustomizer;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class PicsouTableHeaderCustomizer implements LabelCustomizer {
  private ColorService colorService;
  protected String tableHeaderTitle;

  public PicsouTableHeaderCustomizer(Directory directory, String titleColor) {
    colorService = directory.get(ColorService.class);
    tableHeaderTitle = titleColor;
  }

  public void process(JLabel label, Glob glob, boolean isSelected, boolean hasFocus, int row, int column) {
    ColorUpdater updater = new ForegroundColorUpdater(tableHeaderTitle, label);
    updater.install(colorService);
    label.setBorder(BorderFactory.createEmptyBorder());
  }
}
