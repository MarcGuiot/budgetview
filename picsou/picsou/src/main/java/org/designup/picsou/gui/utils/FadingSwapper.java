package org.designup.picsou.gui.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

public class FadingSwapper {
  private FadingPanel fadingPanel;
  private JFrame frame;

  public static FadingSwapper init(JFrame frame) {
    return new FadingSwapper(frame);
  }

  private FadingSwapper(JFrame frame) {
    this.fadingPanel = new FadingPanel();
    this.frame = frame;
    frame.setGlassPane(fadingPanel);
  }

  public void swapTo(JPanel newPanel) {
    Container pane = frame.getContentPane();
    BufferedImage image = new BufferedImage(pane.getWidth(), pane.getHeight(),
                                            BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.getGraphics();
    pane.paint(g);
    fadingPanel.setImage(image);
    fadingPanel.setVisible(true);
    fadingPanel.repaint();

    frame.setContentPane(newPanel);

    fadingPanel.startFading();
  }

  private static class FadingPanel extends JPanel implements ActionListener {
    private MouseListener mouseEventConsumer = new MouseEventConsumer();
    private BufferedImage image = null;
    private float alpha = 1f;
    private float fadingStep = 0.2f;
    private boolean started;
    private Timer startedTimer;
    private static final int DELAY = 2;

    FadingPanel() {
      setOpaque(false);
    }

    void setImage(BufferedImage image) {
      this.image = image;
    }

    void startFading() {
      addMouseListener(mouseEventConsumer);
      alpha = 1f;
      started = true;
      startedTimer = new Timer(DELAY, this);
      startedTimer.start();
    }

    void stopFading() {
      if (startedTimer != null) {
        startedTimer.stop();
        startedTimer = null;
        started = false;
        setVisible(false);
        removeMouseListener(mouseEventConsumer);
      }
    }

    public void paintComponent(Graphics g) {
      if (image == null) {
        return;
      }

      if (started) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setComposite(computeComposite());
        g2.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
      }
    }

    public void actionPerformed(ActionEvent e) {
      if (alpha <= 0f) {
        alpha = 0f;
        stopFading();
      }
      else {
        alpha -= fadingStep;
        repaint();
      }
    }

    private AlphaComposite computeComposite() {
      if (alpha <= 0f) {
        alpha = 0f;
      }
      return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
    }
  }

  private static class MouseEventConsumer extends MouseAdapter {
  }
}
