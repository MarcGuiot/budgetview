package org.designup.picsou.gui.time.mousestates;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public interface MouseState {
  MouseState mousePressed(MouseEvent e);

  MouseState mouseReleased(MouseEvent e);

  MouseState mouseMoved(MouseEvent e);

  MouseState keyPressed(KeyEvent e);
}
