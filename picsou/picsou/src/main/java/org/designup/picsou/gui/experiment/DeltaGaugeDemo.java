package org.designup.picsou.gui.experiment;

import org.designup.picsou.gui.components.charts.DeltaGauge;
import org.designup.picsou.gui.description.Formatting;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import java.awt.*;

public class DeltaGaugeDemo {

  public static void main(String[] args) {
    DeltaGaugeDemo demo = new DeltaGaugeDemo();
    demo.init();
    demo.show();
  }

  private GridBagBuilder builder = GridBagBuilder.init();
  private int rowIndex = 0;

  public DeltaGaugeDemo() {
  }

  public void init() {
    add(100.0, 100.0);
    add(100.0, 110.0);
    add(100.0, 150.0);
    add(100.0, 200.0);
    add(null, null);
    add(100.0, null);
    add(null, 100.0);
    add(null, -100.0);
    add(-100.0, -150.0);
    add(-100.0, -130.0);
    add(-100.0, -110.0);
    add(-100.0, -105.0);
    add(-100.0, -100.0);
    add(-100.0, -50.0);
    add(-100.0, -1.0);
    add(-100.0, -1000.0);
    add(-100.0, 0.0);
    add(0.0, 50.0);
    add(0.0, -50.0);
  }

  private void add(Double previousValue, Double newValue) {

    builder.add(new JLabel(Formatting.toString(previousValue)), 0, rowIndex, 1, 1, 1.0, 1.0, Fill.NONE, Anchor.CENTER);
    builder.add(new JLabel(Formatting.toString(newValue)), 1, rowIndex, 1, 1, 1.0, 1.0, Fill.NONE, Anchor.CENTER);

    DeltaGauge gauge = new DeltaGauge();
    gauge.setValues(previousValue, newValue);
    gauge.setPreferredSize(new Dimension(14, 14));
    builder.add(gauge, 2, rowIndex, 1, 1, 1.0, 1.0, Fill.NONE, Anchor.CENTER);
    rowIndex++;
  }

  private void show() {
    JFrame frame = new JFrame();
    frame.setBackground(Color.WHITE);
    frame.add(builder.getPanel());
    frame.setSize(new Dimension(150, 400));
    GuiUtils.showCentered(frame);
  }
}
