package org.globsframework.gui.views.impl;

import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;

public class StringListCellRenderer extends DefaultListCellRenderer {
  private GlobStringifier stringifier;
  private GlobRepository globRepository;
  private boolean useWhiteSpaceForEmptyStrings;

  public StringListCellRenderer(GlobStringifier stringifier, GlobRepository globRepository) {
    this.stringifier = stringifier;
    this.globRepository = globRepository;
  }

  public StringListCellRenderer setUseWhiteSpaceForEmptyStrings(boolean value) {
    this.useWhiteSpaceForEmptyStrings = value;
    return this;
  }

  public Component getListCellRendererComponent(JList list, Object object, int index,
                                                boolean isSelected, boolean cellHasFocus) {
    Glob glob = (Glob)object;
    String value;
    if (glob != null) {
      value = stringifier.toString(glob, globRepository);
    }
    else {
      value = getValueForNull();
    }
    if (useWhiteSpaceForEmptyStrings && Strings.isNullOrEmpty(value)) {
      value = " ";
    }
    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
  }

  protected String getValueForNull() {
    return "";
  }
}
