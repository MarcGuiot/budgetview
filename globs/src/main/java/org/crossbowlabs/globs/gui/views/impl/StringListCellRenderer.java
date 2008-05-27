package org.crossbowlabs.globs.gui.views.impl;

import org.crossbowlabs.globs.model.Glob;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.format.GlobStringifier;

import javax.swing.*;
import java.awt.*;

public class StringListCellRenderer extends DefaultListCellRenderer {
  private GlobStringifier stringifier;
  private GlobRepository globRepository;

  public StringListCellRenderer(GlobStringifier stringifier, GlobRepository globRepository) {
    this.stringifier = stringifier;
    this.globRepository = globRepository;
  }

  public Component getListCellRendererComponent(JList list, Object object, int index,
                                                boolean isSelected, boolean cellHasFocus) {
    Glob glob = (Glob)object;
    String value = stringifier.toString(glob, globRepository);
    return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
  }
}
