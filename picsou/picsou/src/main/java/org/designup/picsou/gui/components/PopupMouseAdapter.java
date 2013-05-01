package org.designup.picsou.gui.components;

import org.globsframework.gui.utils.PopupMenuFactory;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PopupMouseAdapter extends MouseAdapter {
  private final PopupMenuFactory factory;

  public PopupMouseAdapter(PopupMenuFactory factory) {
    this.factory = factory;
  }

  public void mouseReleased(MouseEvent e) {
    showPopup(e);
  }

  public void showPopup(MouseEvent e) {
    JPopupMenu menu = factory.createPopup();
    if (!menu.isShowing()) {
      menu.show(e.getComponent(), 0, e.getComponent().getHeight());
    }
  }
}
