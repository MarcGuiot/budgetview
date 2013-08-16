package org.designup.picsou.gui.components;

import org.globsframework.gui.utils.PopupMenuFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JPopupButton extends JButton {

  public JPopupButton(String text, final PopupMenuFactory factory) {
    super(text);
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JPopupMenu menu = factory.createPopup();
        if (!menu.isShowing()) {
          menu.show(JPopupButton.this, 0, JPopupButton.this.getHeight());
        }
      }
    });
  }

  public JPopupButton(String text, final JPopupMenu menu) {
    this(text, new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        return menu;
      }
    });
  }
}
