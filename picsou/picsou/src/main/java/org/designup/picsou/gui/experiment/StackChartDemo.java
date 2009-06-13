package org.designup.picsou.gui.experiment;

import org.designup.picsou.gui.components.charts.StackChart;
import org.designup.picsou.gui.components.charts.StackChartElement;
import org.globsframework.gui.splits.layout.SingleComponentPanels;

import javax.swing.*;
import java.awt.*;
import java.util.SortedSet;
import java.util.TreeSet;

public class StackChartDemo {
  private static final double[] VALUES = {2, 0, 8, 5, -10, 10, 4};

  public static void main(String[] args) {

    SortedSet<StackChartElement> elements = new TreeSet<StackChartElement>();
    elements.add(new StackChartElement("label 1", 100.0, false));
    elements.add(new StackChartElement("label 2", 600.0, false));
    elements.add(new StackChartElement("label 3", 1.0, false));
    elements.add(new StackChartElement("label 4", 1000.0, false));
    elements.add(new StackChartElement("label 5", 300.0, false));
    elements.add(new StackChartElement("label 6 is longer", 200.0, false));

    StackChart chart = new StackChart();
    chart.setValues(elements);

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setSize(new Dimension(400, 500));
    frame.setVisible(true);
  }
}
