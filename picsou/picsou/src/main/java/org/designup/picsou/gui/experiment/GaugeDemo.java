package org.designup.picsou.gui.experiment;

import org.designup.picsou.gui.components.Gauge;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;

import javax.swing.*;
import java.awt.*;

public class GaugeDemo {

  private static int row = 0;
  private static GridBagBuilder builder = GridBagBuilder.init();

  public static void main(String[] args) {

    builder.setDefaultInsets(20, 20, 20, 20);

    addGauge(0.0, 19.0);
    addGauge(12.0, 19.0);
    addGauge(19.0, 19.0);
    addGauge(19.01, 19.0);
    addGauge(21.0, 19.0);
    addGauge(-12.0, -19.0);
    addGauge(-19.0, -19.0);

    JFrame frame = new JFrame();
    frame.setContentPane(builder.getPanel());
    frame.setSize(new Dimension(200, 450));
    frame.setVisible(true);
  }

  private static void addGauge(double actual, double target) {
    Gauge gauge = new Gauge();
    gauge.setActualValue(actual);
    gauge.setTargetValue(target);

    builder.add(Box.createGlue(), 0, row++);
    builder.add(gauge, 0, row++, 1, 1, 10.0, 0.1, Fill.HORIZONTAL, Anchor.CENTER);
  }
}
