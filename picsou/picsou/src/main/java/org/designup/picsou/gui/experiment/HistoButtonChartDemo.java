package org.designup.picsou.gui.experiment;

import org.designup.picsou.gui.components.charts.histo.HistoChart;
import org.designup.picsou.gui.components.charts.histo.HistoChartConfig;
import org.designup.picsou.gui.components.charts.histo.HistoSelection;
import org.designup.picsou.gui.components.charts.histo.button.HistoButtonColors;
import org.designup.picsou.gui.components.charts.histo.button.HistoButtonDataset;
import org.designup.picsou.gui.components.charts.histo.button.HistoButtonPainter;
import org.designup.picsou.gui.components.charts.histo.utils.HistoChartListenerAdapter;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.model.Project;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class HistoButtonChartDemo {
  public static void main(String[] args) {
    HistoButtonDataset dataset = new HistoButtonDataset("");
    dataset.addColumn(201012, "D", "tooltip", "2010", false, false, false);
    dataset.addColumn(201101, "J", "tooltip", "2011", false, false, false);
    dataset.addColumn(201102, "F", "tooltip", "2011", false, false, false);
    dataset.addColumn(201103, "M", "tooltip", "2011", false, false, false);
    dataset.addColumn(201104, "A", "tooltip", "2011", false, false, false);
    dataset.addColumn(201105, "M", "tooltip", "2011", false, false, false);
    dataset.addColumn(201106, "J", "tooltip", "2011", false, false, false);
    dataset.addColumn(201107, "J", "tooltip", "2011", false, true, false);
    dataset.addColumn(201108, "A", "tooltip", "2011", false, true, true);
    dataset.addColumn(201109, "S", "tooltip", "2011", false, true, false);
    dataset.addColumn(201110, "O", "tooltip", "2011", false, true, false);
    dataset.addColumn(201111, "N", "tooltip", "2011", false, true, false);
    dataset.addColumn(201112, "D", "tooltip", "2011", false, true, false);
    dataset.addColumn(201201, "J", "tooltip", "2012", false, true, false);

    dataset.addButton(201006, 201105, "button1", Key.create(Project.TYPE, 1), "tooltip 1");
    dataset.addButton(201102, 201104, "button2", Key.create(Project.TYPE, 2), "tooltip 2");
    dataset.addButton(201108, 201110, "button3", Key.create(Project.TYPE, 3), "tooltip 3");
    dataset.addButton(201106, 201110, "button4", Key.create(Project.TYPE, 4), "tooltip 4");

    Directory directory = new DefaultDirectory();
    directory.add(ApplicationColors.createColorService());

    HistoButtonColors colors = new HistoButtonColors(
      "histo.button.bg.top",
      "histo.button.bg.bottom",
      "histo.button.label",
      "histo.button.label.shadow",
      "histo.button.border",
      "histo.button.rollover.bg.top",
      "histo.button.rollover.bg.bottom",
      "histo.button.rollover.label",
      "histo.button.rollover.border",
      directory
    );

    HistoChart chart = new HistoChart(new HistoChartConfig(true, true, false, false, true, true, true, false), directory);

    chart.addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection selection, Key objectKey) {
        if (objectKey != null) {
          System.out.println("Click on object: " + objectKey + " with columns: " + selection);
        }
        else {
          System.out.println("Click on columns: " + selection);
        }
      }
    });

    chart.update(new HistoButtonPainter(dataset, colors));

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setBackground(Color.WHITE);
    frame.setSize(new Dimension(600, 300));

    SplitsEditor.show(frame, directory);

    frame.setVisible(true);
  }
}
