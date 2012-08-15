package org.globsframework.gui.views;

import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import javax.swing.*;

public interface GlobListIconifier {
  Icon getIcon(GlobList list, GlobRepository repository);
}
