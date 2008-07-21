package org.designup.picsou.gui.components;

import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class Gauge extends JPanel {
  private double actual;
  private double target;

  private Color borderColor;
  private Color filledColorTop;
  private Color filledColorBottom;
  private Color emptyColorTop;
  private Color emptyColorBottom;
  private Color warningColorTop;
  private Color warningColorBottom;
  private Color triangleTopColor;
  private Color triangleBottomColor;
  private Color triangleSignColor;

  private int barVerticalMargin = 10;

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    if (target == 0) {
      return;
    }

    float ratio = (float)actual / (float)target;

    int barTop = barVerticalMargin;
    int barBottom = height - barVerticalMargin;
    int barHeight = barBottom - barTop;

    g2.setPaint(new GradientPaint(0, barTop, emptyColorTop, 0, barBottom,  emptyColorBottom));
    g2.fillRoundRect(0, barTop, width, barHeight, 5, 5);

    g2.setColor(borderColor);
    g2.drawRoundRect(0, barTop, width, barHeight, 5, 5);
  }

  public void setActual(double actual) {
    this.actual = actual;
  }

  public void setTarget(double target) {
    this.target = target;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setFilledColorTop(Color filledColorTop) {
    this.filledColorTop = filledColorTop;
  }

  public void setFilledColorBottom(Color filledColorBottom) {
    this.filledColorBottom = filledColorBottom;
  }

  public void setEmptyColorTop(Color emptyColorTop) {
    this.emptyColorTop = emptyColorTop;
  }

  public void setEmptyColorBottom(Color emptyColorBottom) {
    this.emptyColorBottom = emptyColorBottom;
  }

  public static void main(String[] args) {
    Gauge gauge = new Gauge();
    gauge.setActual(12.0);
    gauge.setTarget(19.0);
    gauge.setFilledColorTop(Color.BLUE.brighter());
    gauge.setFilledColorBottom(Color.BLUE.darker());
    gauge.setEmptyColorTop(Color.LIGHT_GRAY.darker());
    gauge.setEmptyColorBottom(Color.LIGHT_GRAY.brighter());
    gauge.setBorderColor(Color.DARK_GRAY);

    gauge.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

    GuiUtils.show(gauge);
  }
}
