package com.budgetview.desktop.components.charts.stack;

import com.budgetview.desktop.components.charts.stack.StackChart;
import com.budgetview.desktop.components.charts.stack.StackChartColors;
import com.budgetview.desktop.components.charts.stack.StackChartDataset;
import com.budgetview.desktop.utils.ApplicationColors;
import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;
import java.awt.*;

public class StackChartDemo {

  private static int currentId = 1;

  public static void main(String[] args) {

    StackChartDataset leftDataset = new StackChartDataset();
    add(leftDataset, "item 1", 2000.0);

    StackChartDataset rightDataset = new StackChartDataset();
    add(rightDataset, "label 1", 100.0);
    add(rightDataset, "label 2", 600.0);
    add(rightDataset, "label 3", 100.0);
    add(rightDataset, "label 4", 1000.0, true);
    add(rightDataset, "label 5", 300.0);
    add(rightDataset, "label 6 is longer", 200.0);

    Directory directory = new DefaultDirectory();
    directory.add(ApplicationColors.createColorService());
    
    StackChart chart = new StackChart();
    chart.update(leftDataset, rightDataset, new StackChartColors(
      "stack.income.bar",
      "stack.expenses.bar",
      "stack.barText",
      "stack.label",
      "stack.border",
      "stack.floor", "stack.selection.border",
      "stack.rollover.text",
      directory
    ));

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setSize(new Dimension(400, 500));
    frame.setVisible(true);
  }

  private static void add(StackChartDataset dataset, final String label, double value) {
    add(dataset, label, value, false);
  }

  private static void add(StackChartDataset dataset, final String label, double value, boolean selected) {
    dataset.add(label, value, Key.create(DummyObject.TYPE, currentId), selected);
  }
}
