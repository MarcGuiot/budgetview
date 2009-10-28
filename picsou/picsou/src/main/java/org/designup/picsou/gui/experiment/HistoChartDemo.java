package org.designup.picsou.gui.experiment;

import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartListener;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffPainter;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffColors;
import org.designup.picsou.gui.utils.ApplicationColors;

import javax.swing.*;
import java.awt.*;

public class HistoChartDemo {
  public static void main(String... args) {

    HistoDiffDataset dataset = new HistoDiffDataset();
    dataset.add(200801, 3500, 3000, "D", "2008", false, false);
    dataset.add(200802, 3800, 3900, "J", "2009", false, false);
    dataset.add(200803, 3300, 3000, "F", "2009", false, false);
    dataset.add(200804, 3300, 3300, "M", "2009", false, false);
    dataset.add(200805, 3700, 4200, "A", "2009", false, false);
    dataset.add(200806, 3500, 3000, "M", "2009", false, false);
    dataset.add(200807, 6000, 3200, "J", "2009", false, false);
    dataset.add(200808, 2100, 3000, "J", "2009", true, false);
    dataset.add(200809, 3600, 3000, "A", "2009", false, true);
    dataset.add(200810, 3700, 3400, "S", "2009", false, true);
    dataset.add(200811, 3800, 3500, "O", "2009", false, true);
    dataset.add(200812, 3900, 3400, "N", "2009", false, true);
    dataset.add(200901, 2000, 3300, "D", "2009", false, true);
    dataset.add(200901, 2300, 3200, "J", "2010", false, true);
    dataset.add(200901, 2800, 3100, "F", "2010", false, true);
    dataset.add(200901, 3400, 3000, "M", "2010", false, true);

    Directory directory = new DefaultDirectory();
    directory.add(ApplicationColors.createColorService());

    HistoDiffColors colors = new HistoDiffColors(
      "histo.income.line",
      "histo.income.overrun",
      "histo.expenses.line",
      "histo.expenses.overrun",
      "histo.balance.fill",
      directory
    );

    HistoChart chart = new HistoChart(directory);

    chart.setListener(new HistoChartListener() {
      public void columnClicked(int id) {
        System.out.println("HistoChartDemo.columnClicked: " + id);
      }
    });

    chart.update(new HistoDiffPainter(dataset, colors));


    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setBackground(Color.WHITE);
    frame.setSize(new Dimension(600, 300));
    frame.setVisible(true);
  }
}
