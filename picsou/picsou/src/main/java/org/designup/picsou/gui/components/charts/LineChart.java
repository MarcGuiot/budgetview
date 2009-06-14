package org.designup.picsou.gui.components.charts;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.Arrays;

public class LineChart extends JPanel {
  private GeneralPath shape;
  private int shapeWidth;

  public LineChart() {
    setMinimumSize(new Dimension(60, 28));
    setPreferredSize(new Dimension(200, 28));
  }

  public void setValues(double[] values) {
    float[] normalizedValues = normalize(values);

    System.out.println("LineChart.setValues: " + Arrays.toString(normalizedValues));

    shape = new GeneralPath();
    shape.moveTo(0, normalizedValues[0]);
    for (int i = 0; i < normalizedValues.length; i++) {
      shape.lineTo(i, normalizedValues[i]);
    }
    shapeWidth = normalizedValues.length;
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int width = getWidth() - 1;
    int height = getHeight() - 1;

    if (isOpaque()) {
      g2.setColor(getBackground());
      g2.fillRect(0, 0, width, height);
    }

    g2.setColor(Color.BLACK);
    g2.fillRect(0, 0, width, height);

    Rectangle shapeBounds = shape.getBounds();
    AffineTransform scaling =
      AffineTransform.getScaleInstance(width / (float)shapeBounds.width,
                                       height / (float)shapeBounds.height);
    shape.transform(scaling);

//    shapeBounds = shape.getBounds();
//    float middleX = (float)width / 2;
//    float middleY = (float)height / 2;
//    AffineTransform translation =
//      AffineTransform.getTranslateInstance(middleX, middleY);
//    shape.transform(translation);

    g2.setColor(Color.RED);
    g2.draw(shape);

  }

  private float[] normalize(double[] values) {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    for (double value : values) {
      if (value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }
    }

    float[] normalizedValues = new float[values.length];
    if (min == max) {
      Arrays.fill(normalizedValues, 0.0f);
    }
    else {
      for (int i = 0; i < values.length; i++) {
        normalizedValues[i] = (float)Math.abs((values[i] - min) / (max - min));
      }
    }
    return normalizedValues;
  }
}