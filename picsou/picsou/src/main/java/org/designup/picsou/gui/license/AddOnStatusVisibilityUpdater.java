package org.designup.picsou.gui.license;

import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.*;

public class AddOnStatusVisibilityUpdater {
  public static void install(GlobRepository repository, final Component component) {
    AddOnStatusListener.install(repository, new AddOnStatusListener() {
      protected void statusChanged(boolean addOnActivated) {
        component.setVisible(addOnActivated);
        Component parent = component.getParent();
        if (parent != null && (parent instanceof JComponent)) {
          GuiUtils.revalidate((JComponent)parent);
        }
      }
    });
  }

  public static void installReversed(GlobRepository repository, final Component component) {
    AddOnStatusListener.install(repository, new AddOnStatusListener() {
      protected void statusChanged(boolean addOnActivated) {
        component.setVisible(!addOnActivated);
        Component parent = component.getParent();
        if (parent != null && (parent instanceof JComponent)) {
          GuiUtils.revalidate((JComponent)parent);
        }
      }
    });
  }
}
