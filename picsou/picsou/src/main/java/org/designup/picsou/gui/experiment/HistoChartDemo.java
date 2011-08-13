package org.designup.picsou.gui.experiment;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffBarLinePainter;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffColors;
import org.designup.picsou.gui.components.charts.histo.diff.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

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

    HistoChart chart = new HistoChart(new HistoChartConfig(true, true, false, true, true, false), directory);

    chart.addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection monthIds, Key objectKey) {
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
