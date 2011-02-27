package org.designup.picsou.gui.components;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JPopupButton extends JButton {
  public JPopupButton(String text, final JPopupMenu menu) {
    super(text);

    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        checkPopup(e);
      }

      public void mouseClicked(MouseEvent e) {
        checkPopup(e);
      }

      public void mouseReleased(MouseEvent e) {
        checkPopup(e);
      }

      private void checkPopup(MouseEvent e) {
        if (!menu.isShowing()) {
          menu.show(e.getComponent(), 0, getHeight());
        }
      }
    });
  }
}
