package org.designup.picsou.bank.connectors.sg;

import org.designup.picsou.bank.connectors.webcomponents.WebImage;
import org.designup.picsou.bank.connectors.webcomponents.WebMap;
import org.designup.picsou.bank.connectors.webcomponents.WebPanel;
import org.designup.picsou.bank.connectors.webcomponents.WebTextInput;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

class SgKeyboardPanel extends JPanel {
  private InOutMouseMotionListener motionListener = new InOutMouseMotionListener();
  private WebTextInput password;
  private BufferedImage image;
  private JTextField passwordTextField;

  public SgKeyboardPanel(JTextField passwordTextField) {
    this.passwordTextField = passwordTextField;
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
    if (motionListener.drawKeyBorder) {
      g.drawImage(motionListener.imageSurlignage,
                  motionListener.drawBoxX,
                  motionListener.drawBoxY,
                  new NullImageObserver());
    }
  }

  public void setImage(BufferedImage clavier, WebMap map, WebTextInput password, WebPanel zoneClavier) throws WebParsingError {
    this.image = clavier;
    this.password = password;
    motionListener.init(zoneClavier, map);
  }

  private static class NullImageObserver implements ImageObserver {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  }

  private class InOutMouseMotionListener extends MouseAdapter implements MouseMotionListener {
    private WebMap.WebMapArea[][] scare;
    private int lastX = -1;
    private int lastY = -1;
    private WebImage surlignage;
    private boolean drawKeyBorder = false;
    private BufferedImage imageSurlignage;
    private int drawBoxX;
    private int drawBoxY;

    public void init(WebPanel zoneClavier, WebMap map) throws WebParsingError {
      this.surlignage = zoneClavier.getImageById("surlignage");
      imageSurlignage = surlignage.getFirstImage();
      scare = new WebMap.WebMapArea[4][4];
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          WebMap.WebMapArea id = map.getAreaById("touche" + (j + 1) + "" + (i + 1));
          String className = id.getClassName();
          if (className != null && !className.equals("toucheVide")) {
            scare[i][j] = id;
          }
        }
      }
    }

    public void mousePressed(MouseEvent e) {
      if (surlignage == null) {
        return;
      }
      surlignage.mouseDown();
    }

    public void mouseReleased(MouseEvent e) {
      if (surlignage == null) {
        return;
      }
      surlignage.mouseUp();
      passwordTextField.setText(password.getValue());
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
      if (surlignage == null) {
        return;
      }
      int x = (4 * e.getX()) / (image.getWidth() + 1);
      int y = (4 * e.getY()) / (image.getHeight() + 1);
      if ((x != lastX || y != lastY)) {
        if (lastX != -1 && lastY != -1) {
          WebMap.WebMapArea previous = scare[lastX][lastY];
          if (previous != null && drawKeyBorder) {
            previous.mouseOut();
            drawKeyBorder = false;
            repaint();
          }
        }
        if (x < 0 || x >= 4 || y < 0 || y >= 4) {
          drawKeyBorder = false;
          repaint();
          return;
        }
        WebMap.WebMapArea newElement = scare[x][y];
        if (newElement != null) {
          newElement.mouseOver();
          lastX = x;
          lastY = y;
          drawKeyBorder = true;
          drawBoxX = lastX * image.getWidth() / 4;
          drawBoxY = lastY * image.getHeight() / 4;
          repaint();
        }
      }
    }
  }
}
