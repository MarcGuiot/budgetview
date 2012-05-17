package org.designup.picsou.bank.importer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ProgressPanel extends JComponent implements ActionListener {

  private static final int DEFAULT_NUMBER_OF_BARS = 12;
  private static final Dimension DIMENSION = new Dimension(30,30);
  private final static double SCALE = 1.2d;

  private int numBars;

  private Area[] bars;
  private Rectangle barsBounds = null;
  private Rectangle barsScreenBounds = null;
  private AffineTransform centerAndScaleTransform = null;
  private Timer timer = new Timer(1000 / 16, this);
  private Color[] colors = null;
  private int colorOffset = 0;
  private boolean show = false;

  public ProgressPanel() {
    this.numBars = DEFAULT_NUMBER_OF_BARS;

    colors = new Color[numBars * 2];

    bars = buildTicker(numBars);

    barsBounds = new Rectangle();
    for (Area bar : bars) {
      barsBounds = barsBounds.union(bar.getBounds());
    }

    for (int i = 0; i < bars.length; i++) {
      int channel = 224 - 128 / (i + 1);
      colors[i] = new Color(channel, channel, channel);
      colors[numBars + i] = colors[i];
    }

    setOpaque(false);

    setMinimumSize(DIMENSION);
    setPreferredSize(DIMENSION);
  }
  
  public void start() {
    show = true;
    timer.start();
  }

  public void stop() {
    show = false;
    timer.stop();
    repaint();
  }

  public void actionPerformed(ActionEvent e) {
    // rotate colors
    if (colorOffset == (numBars - 1)) {
      colorOffset = 0;
    }
    else {
      colorOffset++;
    }
    // repaint
    if (barsScreenBounds != null) {
      repaint(barsScreenBounds);
    }
    else {
      repaint();
    }
  }

  public void setBounds(int x, int y, int width, int height) {
    super.setBounds(x, y, width, height);
    // update centering transform
    centerAndScaleTransform = new AffineTransform();
    centerAndScaleTransform.translate((double) getWidth() / 2d, (double) getHeight() / 2d);
    centerAndScaleTransform.scale(SCALE, SCALE);
    // calc new bars bounds
    if (barsBounds != null) {
      Area bounds = new Area(barsBounds);
      bounds.transform(centerAndScaleTransform);
      barsScreenBounds = bounds.getBounds();
    }
  }

  protected void paintComponent(Graphics g) {
    if (!show) {
      return;
    }

    // move to center
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.transform(centerAndScaleTransform);

    // draw ticker
    for (int i = 0; i < bars.length; i++) {
      g2.setColor(colors[i + colorOffset]);
      g2.fill(bars[i]);
    }
  }

  private static Area[] buildTicker(int barCount) {
    Area[] ticker = new Area[barCount];
    Point2D.Double center = new Point2D.Double(0, 0);
    double fixedAngle = 2.0 * Math.PI / ((double)barCount);

    for (double i = 0.0; i < (double)barCount; i++) {
      Area primitive = buildPrimitive();

      AffineTransform toCenter = AffineTransform.getTranslateInstance(center.getX(), center.getY());
      AffineTransform toBorder = AffineTransform.getTranslateInstance(2.0, -0.4);
      AffineTransform toCircle = AffineTransform.getRotateInstance(-i * fixedAngle, center.getX(), center.getY());

      AffineTransform toWheel = new AffineTransform();
      toWheel.concatenate(toCenter);
      toWheel.concatenate(toBorder);

      primitive.transform(toWheel);
      primitive.transform(toCircle);

      ticker[(int) i] = primitive;
    }

    return ticker;
  }

  private static Area buildPrimitive() {
    Rectangle2D.Double body = new Rectangle2D.Double(0, 0, 6, 1);
    return new Area(body);
  }
}