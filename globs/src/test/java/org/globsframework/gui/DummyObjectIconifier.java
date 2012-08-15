package org.globsframework.gui;

import org.globsframework.gui.views.GlobListIconifier;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.*;

public class DummyObjectIconifier implements GlobListIconifier {

  public static final Icon ID1_ICON = new DummyIcon("ID1");
  public static final Icon ID2_ICON = new DummyIcon("ID2");
  public static final Icon EMPTY_ICON = new DummyIcon("EMPTY");
  public static final Icon MULTI_ICON = new DummyIcon("MULTI");

  public Icon getIcon(GlobList list, GlobRepository repository) {
    if (list.isEmpty()) {
      return EMPTY_ICON;
    }
    else if (list.size() > 1) {
      return MULTI_ICON;
    }
    Glob glob = list.getFirst();
    switch (glob.get(DummyObject.ID)) {
      case 1:
        return ID1_ICON;
      case 2:
        return ID2_ICON;
    }
    return null;
  }

  private static class DummyIcon implements Icon {

    private String name;

    public DummyIcon(String name) {
      this.name = name;
    }

    public void paintIcon(Component component, Graphics graphics, int i, int i1) {
    }

    public int getIconWidth() {
      return 10;
    }

    public int getIconHeight() {
      return 10;
    }

    public String toString() {
      return name;
    }
  }
}
