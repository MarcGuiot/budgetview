package com.budgetview.desktop.time.mousestates;

import java.awt.event.MouseEvent;

public interface MouseState {
  MouseState mousePressed(MouseEvent e);

  MouseState mouseReleased(MouseEvent e);

  MouseState mouseMoved(MouseEvent e);
}
