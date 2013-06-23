package org.designup.picsou.gui.components;

import org.globsframework.gui.utils.PopupMenuFactory;

import javax.swing.*;

public class JPopupButton extends JButton {

  public JPopupButton(String text, final PopupMenuFactory factory) {
    super(text);
    addMouseListener(new PopupMouseAdapter(factory));
  }

  public JPopupButton(String text, final JPopupMenu menu) {
    this(text, new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        return menu;
      }
    });
  }
}
