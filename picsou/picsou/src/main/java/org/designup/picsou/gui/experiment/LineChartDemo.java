package org.designup.picsou.gui.experiment;

import org.designup.picsou.gui.components.charts.LineChart;
import org.globsframework.gui.splits.layout.SingleComponentPanels;

import javax.swing.*;
import java.awt.*;

public class LineChartDemo {

  private static final double[] VALUES = {2, 0, 8, 5, -10, 10, 4};

  public static void main(String[] args) {

    LineChart chart = new LineChart();
    chart.setValues(VALUES);

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setSize(new Dimension(1200, 500));
    frame.setVisible(true);
  }
}