package org.designup.picsou.gui.components.charts.pie;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class PieChartPainter extends JPanel {

  protected int originX, originY;
  protected int radius;

  private static double piby2 = Math.PI / 2.0;
  private static double twopi = Math.PI * 2.0;
  private static double d2r   = Math.PI / 180.0; // Degrees to radians.
  private static int xGap = 5;
  private static int inset = 40;

  protected Font textFont = new Font("Serif", Font.PLAIN, 12);
  protected Color textColor = Color.black;
  protected Color colors[] = new Color[] {
    Color.red, Color.blue, Color.yellow, Color.black, Color.green,
    Color.white, Color.gray, Color.cyan, Color.magenta, Color.darkGray
  };
  protected double values[] = new double[0];
  protected String labels[] = new String[0];

  public void setTextFont(Font f) { textFont = f; }
  public Font getTextFont() { return textFont; }

  public void setColor(Color[] clist) { colors = clist; }
  public Color[] getColor() { return colors; }

  public void setColor(int index, Color c) { colors[index] = c; }
  public Color getColor(int index) { return colors[index]; }

  public void setTextColor(Color c) { textColor = c; }
  public Color getTextColor() { return textColor; }

  public void setLabels(String[] l) { labels = l; }
  public void setValues(double[] v) { values = v; }

  public int indexOfEntryAt(MouseEvent me) {
    int x = me.getX() - originX;
    int y = originY - me.getY();  // Upside-down coordinate system.

    // Is (x,y) in the circle?
    if (Math.sqrt(x*x + y*y) > radius) { return -1; }

    double percent = Math.atan2(Math.abs(y), Math.abs(x));
    if (x >= 0) {
      if (y <= 0) { // (IV)
        percent = (piby2 - percent) + 3 * piby2; // (IV)
      }
    }
    else {
      if (y >= 0) { // (II)
        percent = Math.PI - percent;
      }
      else { // (III)
        percent = Math.PI + percent;
      }
    }
    percent /= twopi;
    double t = 0.0;
    if (values != null) {
      for (int i = 0; i < values.length; i++) {
        if (t + values[i] > percent) {
          return i;
        }
        t += values[i];
      }
    }
    return -1;
  }

  public void paint(Graphics g) {

    Dimension size = getSize();
    originX = size.width / 2;
    originY = size.height / 2;
    int diameter = (originX < originY ? size.width - inset
                                      : size.height - inset);
    radius = (diameter / 2) + 1;
    int cornerX = (originX - (diameter / 2));
    int cornerY = (originY - (diameter / 2));

    int startAngle = 0;
    int arcAngle = 0;
    for (int i = 0; i < values.length; i++) {
      arcAngle = (int)(i < values.length - 1 ?
                       Math.round(values[i] * 360) :
                       360 - startAngle);
      g.setColor(colors[i % colors.length]);
      g.fillArc(cornerX, cornerY, diameter, diameter,
                startAngle, arcAngle);
      drawLabel(g, labels[i], startAngle + (arcAngle / 2));
      startAngle += arcAngle;
    }
    g.setColor(Color.black);
    g.drawOval(cornerX, cornerY, diameter, diameter);  // Cap the circle.
  }

  public void drawLabel(Graphics g, String text, double angle) {
    g.setFont(textFont);
    g.setColor(textColor);
    double radians = angle * d2r;
    int x = (int) ((radius + xGap) * Math.cos(radians));
    int y = (int) ((radius + xGap) * Math.sin(radians));
    if (x < 0) {
      x -= SwingUtilities.computeStringWidth(g.getFontMetrics(), text);
    }
    if (y < 0) {
      y -= g.getFontMetrics().getHeight();
    }
    g.drawString(text, x + originX, originY - y);
  }

  public static void main(String[] args) {
    PieChartPainter painter = new PieChartPainter();
    painter.setSize(200, 200);
    painter.setValues(new double[]{0.2, 0.4, 0.3, 0.1});
    painter.setLabels(new String[]{"Un peu de blah blah", "un plus court", "court", "vraiment bien long"});
    GuiUtils.showCentered(painter);
  }
}