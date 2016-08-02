package com.budgetview.desktop.experiment;

import com.budgetview.desktop.components.charts.histo.HistoChart;
import com.budgetview.desktop.components.charts.histo.HistoChartColors;
import com.budgetview.desktop.components.charts.histo.HistoSelection;
import com.budgetview.desktop.components.charts.histo.button.HistoButtonColors;
import com.budgetview.desktop.components.charts.histo.button.HistoButtonDataset;
import com.budgetview.desktop.components.charts.histo.button.HistoButtonPainter;
import com.budgetview.desktop.components.charts.histo.utils.HistoChartListenerAdapter;
import com.budgetview.desktop.utils.ApplicationColors;
import com.budgetview.model.Project;
import com.budgetview.shared.gui.histochart.HistoChartConfig;
import org.globsframework.gui.splits.SplitsEditor;
import org.globsframework.gui.splits.layout.SingleComponentPanels;
import org.globsframework.model.Key;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

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

    dataset.addButton(201006, 201105, "button1", Key.create(Project.TYPE, 1), "tooltip 1", false, true);
    dataset.addButton(201102, 201104, "button2", Key.create(Project.TYPE, 2), "tooltip 2", false, true);
    dataset.addButton(201108, 201110, "button3", Key.create(Project.TYPE, 3), "tooltip 3", false, true);
    dataset.addButton(201106, 201110, "button4", Key.create(Project.TYPE, 4), "tooltip 4", false, true);

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
      "histo.button.disabled.bg.top",
      "histo.button.disabled.bg.bottom",
      "histo.button.disabled.label",
      "histo.button.disabled.border",
      directory
    );

    HistoChart chart = new HistoChart(new HistoChartConfig(true, true, false, false, true, true, false, true, true, false), new HistoChartColors("histo", directory));

    chart.addListener(new HistoChartListenerAdapter() {
      public void processClick(HistoSelection selection, Set<Key> objectKeys) {
        if (objectKeys != null) {
          System.out.println("Click on object: " + objectKeys + " with columns: " + selection);
        }
        else {
          System.out.println("Click on columns: " + selection);
        }
      }
    });

    Font buttonFont = chart.getFont().deriveFont(11.0f);
    FontMetrics buttonFontMetrics = chart.getFontMetrics(buttonFont);
    chart.update(new HistoButtonPainter(dataset, buttonFontMetrics, colors));

    JFrame frame = new JFrame();
    frame.setContentPane(SingleComponentPanels.create(chart, new Insets(20, 20, 20, 20)));
    frame.setBackground(Color.WHITE);
    frame.setSize(new Dimension(600, 300));

    SplitsEditor.show(frame, directory);

    frame.setVisible(true);
  }
}
