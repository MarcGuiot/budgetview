package org.designup.picsou.gui.experiment;

import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.directory.DefaultDirectory;
import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffDataset;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffPainter;
import org.designup.picsou.gui.components.charts.histo.painters.HistoDiffColors;
import org.designup.picsou.gui.utils.PicsouColors;

import javax.swing.*;
import java.awt.*;

public class HistoChartDemo {
  public static void main(String... args) {

    HistoDiffDataset dataset = new HistoDiffDataset();
    dataset.add(3500, 3000, "D", false, false);
    dataset.add(3800, 3900, "J", false, false);
    dataset.add(3300, 3000, "F", false, false);
    dataset.add(3300, 3300, "M", false, false);
    dataset.add(3700, 4200, "A", false, false);
    dataset.add(3500, 3000, "M", false, false);
    dataset.add(6000, 3200, "J", false, false);
    dataset.add(2100, 3000, "J", true, false);
    dataset.add(3600, 3000, "A", false, true);
    dataset.add(3700, 3400, "S", false, true);
    dataset.add(3800, 3500, "O", false, true);
    dataset.add(3900, 3000, "N", false, true);
    dataset.add(2000, 3000, "D", false, true);

    Directory directory = new DefaultDirectory();
    directory.add(PicsouColors.createColorService());

    HistoDiffColors colors = new HistoDiffColors(
      "histo.income.line",
      "histo.income.overrun",
      "histo.expenses.line",
      "histo.expenses.overrun",
      "histo.balance.fill",
      "histo.balance.fill.selected",
      directory
    );

    HistoChart chart = new HistoChart(directory);
    chart.update(new HistoDiffPainter(dataset, colors));

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setBackground(Color.WHITE);
    frame.setSize(new Dimension(600, 300));
    frame.setVisible(true);

  }
}
