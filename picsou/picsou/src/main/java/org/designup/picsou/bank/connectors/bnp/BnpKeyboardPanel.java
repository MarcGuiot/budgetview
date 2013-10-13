package org.designup.picsou.bank.connectors.bnp;

import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

class BnpKeyboardPanel extends JPanel {
  private InOutMouseMotionListener motionListener = new InOutMouseMotionListener();
  private BufferedImage image;
  private CoordinateListener coordinateListener = CoordinateListener.NULL;
  private int divider;

  public BnpKeyboardPanel(int divider) {
    this.divider = divider;
    addMouseListener(motionListener);
    addMouseMotionListener(motionListener);
  }

  public void paint(Graphics g) {
    g.clearRect(0, 0, getWidth(), getHeight());
    if (image == null) {
      g.setColor(getForeground());
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(getForeground().darker());
      g.drawRect(0, 0, getWidth(), getHeight());
      return;
    }

    g.setColor(getBackground());
    g.fillRect(0, 0, getWidth(), getHeight());
    g.drawImage(image, 0, 0, new NullImageObserver());
  }

  public void setImage(BufferedImage clavier, CoordinateListener coordinateListener) throws WebParsingError {
    this.image = clavier;
    this.coordinateListener = coordinateListener;
  }

  private static class NullImageObserver implements ImageObserver {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  }


  private class InOutMouseMotionListener extends MouseAdapter implements MouseMotionListener {
    private int lastX = -1;
    private int lastY = -1;

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
      int x = (divider * e.getX()) / (image.getWidth() + 1);
      int y = (divider * e.getY()) / (image.getHeight() + 1);
      if (x >= 0 && x < divider && y >= 0 && y < divider) {
        coordinateListener.click(x, y);
      }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
      if (image != null) {
        int x = (divider * e.getX()) / (image.getWidth() + 1);
        int y = (divider * e.getY()) / (image.getHeight() + 1);
        if ((x != lastX || y != lastY)) {
          lastX = x;
          lastY = y;
          coordinateListener.enter(x, y);
        }
      }
    }
  }

  interface CoordinateListener {
    CoordinateListener NULL = new CoordinateListener() {
      public void enter(int x, int y) {
      }

      public void click(int x, int y) {
      }
    };

    void enter(int x, int y);

    void click(int x, int y);
  }
}
