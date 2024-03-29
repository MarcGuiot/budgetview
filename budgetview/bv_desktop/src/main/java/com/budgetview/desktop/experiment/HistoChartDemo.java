package com.budgetview.desktop.experiment;

import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.HistoChartColors;
import com.budgetview.desktop.components.charts.histo.HistoSelection;
import com.budgetview.desktop.components.charts.histo.diff.HistoDiffBarLinePainter;
import com.budgetview.desktop.components.charts.histo.diff.HistoDiffColors;
import com.budgetview.desktop.components.charts.histo.diff.HistoDiffDataset;
import com.budgetview.desktop.components.charts.histo.utils.HistoChartListenerAdapter;
import com.budgetview.desktop.utils.ApplicationColors;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class HistoChartDemo {
  public static void main(String... args) {

    HistoDiffDataset dataset = new HistoDiffDataset(null);
    dataset.add(200801, 3500, 3000, "D", "tooltip", "2008", false, false, false);
    dataset.add(200802, 3800, 3900, "J", "tooltip", "2009", false, false, false);
    dataset.add(200803, 3300, 3000, "F", "tooltip", "2009", false, false, false);
    dataset.add(200804, 3300, 3300, "M", "tooltip", "2009", false, false, false);
    dataset.add(200805, 3700, 4200, "A", "tooltip", "2009", false, false, false);
    dataset.add(200806, 3500, 3000, "M", "tooltip", "2009", false, false, false);
    dataset.add(200807, 6000, 3200, "J", "tooltip", "2009", false, false, false);
    dataset.add(200808, -2100, -3000, "J", "tooltip", "2009", false, true, false);
    dataset.add(200809, 3600, 3000, "A", "tooltip", "2009", false, true, true);
    dataset.add(200810, 3700, 3400, "S", "tooltip", "2009", false, false, true);
    dataset.add(200811, 3800, 3500, "O", "tooltip", "2009", false, false, true);
    dataset.add(200812, 3900, 3400, "N", "tooltip", "2009", false, false, true);
    dataset.add(200901, 2000, 3300, "D", "tooltip", "2009", false, false, true);
    dataset.add(200901, 2300, 3200, "J", "tooltip", "2010", false, false, true);
    dataset.add(200901, 2800, 3100, "F", "tooltip", "2010", false, false, true);
    dataset.add(200901, 3400, 3000, "M", "tooltip", "2010", false, false, true);

    Directory directory = new DefaultDirectory();
    directory.add(ApplicationColors.createColorService());

    HistoDiffColors colors = new HistoDiffColors(
      "histo.income.line",
      "histo.expenses.line",
      directory
    );

    HistoChart chart = new HistoChart(new HistoChartConfig(true, true, false, true, true, true, false, true, true, false), new HistoChartColors("histo", directory));

    chart.addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection monthIds, Set<Key> objectKeys) {
        System.out.println("HistoChartDemo.columnClicked: " + monthIds);
      }
    });

    chart.update(new HistoDiffBarLinePainter(dataset, colors, false));

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setBackground(Color.WHITE);
    frame.setSize(new Dimension(600, 300));
    frame.setVisible(true);
  }
}
