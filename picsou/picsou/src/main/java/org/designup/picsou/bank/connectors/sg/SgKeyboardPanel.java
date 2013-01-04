package org.designup.picsou.bank.connectors.sg;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import org.designup.picsou.bank.connectors.webcomponents.WebImage;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

class SgKeyboardPanel extends JPanel {
  private InOutMouseMotionListener motionListener = new InOutMouseMotionListener();
  private HtmlInput password;
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
      g.fillRect(0,0,getWidth(), getHeight());
      g.setColor(getForeground().darker());
      g.drawRect(0,0,getWidth(), getHeight());
      return;
    }

    g.setColor(getBackground());
    g.fillRect(0,0,getWidth(), getHeight());
    g.drawImage(image, 0, 0, new NullImageObserver());
    if (motionListener.drawKeyBorder) {
      g.drawImage(motionListener.imageSurlignage,
                  motionListener.drawBoxX,
                  motionListener.drawBoxY,
                  new NullImageObserver());
    }
  }

  public void setImage(BufferedImage clavier, HtmlElement map, HtmlInput password) {
    this.image = clavier;
    this.password = password;
    motionListener.init(map);
  }

  private static class NullImageObserver implements ImageObserver {
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
      return false;
    }
  }

  private class InOutMouseMotionListener extends MouseAdapter implements MouseMotionListener {
    private HtmlElement[][] scare;
    private int lastX = -1;
    private int lastY = -1;
    private HtmlImage surlignage;
    private boolean drawKeyBorder = false;
    private BufferedImage imageSurlignage;
    private int drawBoxX;
    private int drawBoxY;

    public void init(HtmlElement map) {
      this.surlignage = map.getElementById("surlignage");
      imageSurlignage = WebImage.extractFirstImage(surlignage);

      scare = new HtmlElement[4][4];
      for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
          HtmlElement id = map.getElementById("touche" + (j + 1) + "" + (i + 1));
          String className = id.getAttribute("className");
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
      passwordTextField.setText(password.getValueAttribute());
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
          HtmlElement previous = scare[lastX][lastY];
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
        HtmlElement newElement = scare[x][y];
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
