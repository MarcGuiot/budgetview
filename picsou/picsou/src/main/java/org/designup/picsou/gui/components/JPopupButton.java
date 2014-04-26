package org.designup.picsou.gui.components;

import org.globsframework.gui.utils.PopupMenuFactory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JPopupButton extends JButton {

  private PopupMenuFactory factory;

  public JPopupButton(String text, final JPopupMenu menu) {
    this(text, new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        return menu;
      }
    });
  }

  public JPopupButton(String text, final PopupMenuFactory factory) {
    super(text);
    this.factory = factory;
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JPopupMenu menu = createPopup();
        if (!menu.isShowing()) {
          menu.show(JPopupButton.this, 0, JPopupButton.this.getHeight());
        }
      }
    });
  }

  private JPopupMenu createPopup() {
    return factory.createPopup();
  }

  public void setPopupFactory(PopupMenuFactory factory) {
    this.factory = factory;
  }

  public void setPopupMenu(final JPopupMenu menu) {
    this.factory = new PopupMenuFactory() {
      public JPopupMenu createPopup() {
        return menu;
      }
    };
  }
}
