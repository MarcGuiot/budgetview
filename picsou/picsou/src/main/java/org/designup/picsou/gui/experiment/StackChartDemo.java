package org.designup.picsou.gui.experiment;

import org.designup.picsou.gui.components.charts.stack.StackChart;
import org.designup.picsou.gui.components.charts.stack.StackChartDataset;
import org.designup.picsou.gui.components.charts.stack.StackChartColors;
import org.designup.picsou.gui.utils.PicsouColors;
import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;
import java.awt.*;

public class StackChartDemo {
  private static final double[] VALUES = {2, 0, 8, 5, -10, 10, 4};

  public static void main(String[] args) {

    StackChartDataset leftDataset = new StackChartDataset();
    leftDataset.add("item 1", 1700.0);

    StackChartDataset rightDataset = new StackChartDataset();
    rightDataset.add("label 1", 100.0);
    rightDataset.add("label 2", 600.0);
    rightDataset.add("label 3", 1.0);
    rightDataset.add("label 4", 1000.0, true);
    rightDataset.add("label 5", 300.0);
    rightDataset.add("label 6 is longer", 200.0);

    Directory directory = new DefaultDirectory();
    directory.add(PicsouColors.createColorService());
    
    StackChart chart = new StackChart();
    chart.update(leftDataset, rightDataset, new StackChartColors(
      "stack.income.bar",
      "stack.expenses.bar",
      "stack.barText",
      "stack.label",
      "stack.border",
      "stack.selection.border",
      "stack.selection.text",
      directory
    ));

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setSize(new Dimension(400, 500));
    frame.setVisible(true);
  }
}
