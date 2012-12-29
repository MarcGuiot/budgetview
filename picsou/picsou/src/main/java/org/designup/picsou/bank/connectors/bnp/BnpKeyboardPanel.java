package org.designup.picsou.bank.connectors.bnp;

import com.gargoylesoftware.htmlunit.html.HtmlArea;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.List;

class BnpKeyboardPanel extends JPanel {
  private static final int SIZE = 5;
  private InOutMouseMotionListener motionListener = new InOutMouseMotionListener();
  private HtmlInput password;
  private BufferedImage image;
  private JTextField passwordTextField;

  public BnpKeyboardPanel(JTextField passwordTextField) {
    this.passwordTextField = passwordTextField;
    addMouseListener(motionListener);
    addMouseMotionListener(motionListener);
  }

  public void paint(Graphics g) {
    if (image == null) {
      return;
    }
    g.clearRect(0, 0, getWidth(), getHeight());
    g.drawImage(image, 0, 0, new NullImageObserver());
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

    public void init(HtmlElement map) {
      scare = new HtmlElement[SIZE][SIZE];
      int k = 1;
      for (int i = 0; i < scare.length; i++) {
        for (int j = 0; j < scare.length; j++) {
          String s = "0" + k;
          int len = s.length();
          String pos = s.substring(2 - len, len);
          List<HtmlElement> onclick = map.getElementsByAttribute(HtmlArea.TAG_NAME, "onclick", "Javascript:Grille('" + pos + "')");
          scare[i][j] = onclick.get(0);
        }
      }
    }

    public void mouseReleased(MouseEvent e) {
      int x = (SIZE * e.getX()) / (image.getWidth() + 1);
      int y = (SIZE * e.getY()) / (image.getHeight() + 1);
      if (x < 0 || x >= 4 || y < 0 || y >= 4) {
        repaint();
        return;
      }
      HtmlElement newElement = scare[x][y];
      if (newElement != null) {
        try {
          newElement.click();
        }
        catch (IOException e1) {
          e1.printStackTrace();
        }
      }
      passwordTextField.setText(password.getValueAttribute());
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }
  }
}
