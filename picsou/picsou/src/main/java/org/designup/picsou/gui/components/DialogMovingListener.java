package org.designup.picsou.gui.components;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class DialogMovingListener implements MouseListener, MouseMotionListener {
  private boolean shouldMoveWindow = false;
  private Point initialClick;
  private Component component;
  private int titleHeight;

  public DialogMovingListener(Component component, int titleHeight) {
    this.component = component;
    this.titleHeight = titleHeight;
  }

  public void mousePressed(MouseEvent e) {
    shouldMoveWindow = (e.getPoint().getY() < titleHeight);
    if (shouldMoveWindow) {
      initialClick = e.getPoint();
      component.getComponentAt(initialClick);
    }
  }

  public void mouseDragged(MouseEvent e) {
    if (!shouldMoveWindow) {
      return;
    }

    int oldX = component.getLocation().x;
    int oldY = component.getLocation().y;

    int deltaX = (oldX + e.getX()) - (oldX + initialClick.x);
    int deltaY = (oldY + e.getY()) - (oldY + initialClick.y);

    int newX = oldX + deltaX;
    int newY = oldY + deltaY;

    component.setLocation(newX, newY);
  }

  public void mouseReleased(MouseEvent e) {
    shouldMoveWindow = false;
  }

  public void mouseMoved(MouseEvent e) {
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }
}
